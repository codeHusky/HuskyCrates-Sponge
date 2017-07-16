package com.codehusky.huskycrates.crate.db;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.commands.Husky;
import com.codehusky.huskycrates.crate.PhysicalCrate;
import com.codehusky.huskycrates.crate.VirtualCrate;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by lokio on 7/15/2017.
 */
public class DBReader {
    private static Connection dbConnection = null;
    private static DataSource dbSource = null;
    private static void connectDB() throws SQLException {
        if(dbConnection != null) {
            if (!dbConnection.isClosed()) {
                dbConnection.close();
            }
        }
        if(dbSource == null) {
            dbSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:h2:" + HuskyCrates.instance.configDir.toString() + "/data");
        }
        dbConnection = dbSource.getConnection();
    }

    public static void dbInitCheck() throws SQLException {
        connectDB();
        boolean cP = dbConnection.getMetaData().getTables(null,null,"CRATELOCATIONS",null).next();
        boolean cKU = dbConnection.getMetaData().getTables(null,null,"VALIDKEYS",null).next();
        boolean kB = dbConnection.getMetaData().getTables(null,null,"KEYBALANCES",null).next();
        boolean wI = dbConnection.getMetaData().getTables(null,null,"WORLDINFO",null).next();
        if(!cP){
            dbConnection.prepareStatement("CREATE TABLE CRATELOCATIONS (ID INTEGER NOT NULL AUTO_INCREMENT, X DOUBLE, Y DOUBLE, Z DOUBLE, worldID INTEGER, isEntityCrate BOOLEAN, crateID CHARACTER,  PRIMARY KEY(ID))").executeUpdate();
        }
        if(!cKU){
            dbConnection.prepareStatement("CREATE TABLE VALIDKEYS (keyUUID CHARACTER, crateID CHARACTER, amount INTEGER )").executeUpdate();
        }
        if(!kB){
            dbConnection.prepareStatement("CREATE TABLE KEYBALANCES (userUUID CHARACTER, crateID CHARACTER, amount INTEGER)").executeUpdate();
        }
        if(!wI){
            dbConnection.prepareStatement("CREATE TABLE WORLDINFO (ID INTEGER NOT NULL AUTO_INCREMENT,uuid CHARACTER, name CHARACTER,  PRIMARY KEY(ID))").executeUpdate();
        }

        dbConnection.close();
    }

    public static void loadHuskyData() throws SQLException {
        connectDB();
        HashMap<Integer,World> worldIDtoWorld = new HashMap<>();
        ResultSet worldInfo = dbConnection.prepareStatement("SELECT * FROM WORLDINFO").executeQuery();
        while(worldInfo.next()){
            String worldUUID = worldInfo.getString("uuid");
            String worldName = worldInfo.getString("name");
            int id = worldInfo.getInt("ID");
            Optional<World> preWorld = Sponge.getServer().getWorld(UUID.fromString(worldUUID));
            if(!preWorld.isPresent()){
                HuskyCrates.instance.logger.warn("Invalid World UUID (BUG SPONGE DEVS)");
                preWorld = Sponge.getServer().getWorld(worldName);
            }
            if(preWorld.isPresent()) {
                worldIDtoWorld.put(id,preWorld.get());
                HuskyCrates.instance.logger.info("Loaded " + worldName + " successfully.");
            }else{
                HuskyCrates.instance.logger.warn("WorldInfo #" + id + " provides invalid world info. Removing from table.");
                Statement removal = dbConnection.createStatement();
                removal.executeQuery("SELECT  * FROM WORLDINFO WHERE ID=" + id);
                removal.executeUpdate("DELETE FROM WORLDINFO");
                removal.close();
            }
        }
        ResultSet cratePositions = dbConnection.prepareStatement("SELECT * FROM CRATELOCATIONS").executeQuery();
        HuskyCrates.instance.crateUtilities.physicalCrates = new HashMap<>();
        while(cratePositions.next()){
            int id = cratePositions.getInt("ID");
            double x = cratePositions.getDouble("X");
            double y = cratePositions.getDouble("Y");
            double z = cratePositions.getDouble("Z");
            int worldID = cratePositions.getInt("worldID");
            boolean entityCrate = cratePositions.getBoolean("isEntityCrate");
            String crateID = cratePositions.getString("crateID");
            if(worldIDtoWorld.containsKey(worldID)){ //VALID WORLD
                World world = worldIDtoWorld.get(worldID);
                Location<World> loco = new Location<>(world,x,y,z);
                HuskyCrates.instance.crateUtilities.physicalCrates.put(loco,new PhysicalCrate(loco,crateID,HuskyCrates.instance,entityCrate));
                HuskyCrates.instance.logger.info("Loaded " + crateID + " @ " + x + "," + y + "," + z);
            }else{
                HuskyCrates.instance.logger.warn("CrateLocation #" + id + " provides an invalid world ID. Removing from table.");
                Statement removal = dbConnection.createStatement();
                removal.executeQuery("SELECT  * FROM CRATELOCATIONS WHERE ID=" + id);
                removal.executeUpdate("DELETE FROM CRATELOCATIONS");
                removal.close();
            }
        }
        ResultSet crateKeyUUIDs = dbConnection.prepareStatement("SELECT * FROM VALIDKEYS").executeQuery();
        for(VirtualCrate vc : HuskyCrates.instance.crateUtilities.crateTypes.values()){
            vc.pendingKeys = new HashMap<>();
            vc.virtualBalances = new HashMap<>();
        }
        while(crateKeyUUIDs.next()){
            UUID keyUUID = UUID.fromString(crateKeyUUIDs.getString("keyUUID"));
            String crateID = crateKeyUUIDs.getString("crateID");
            int amount = crateKeyUUIDs.getInt("amount");
            if(HuskyCrates.instance.crateUtilities.crateTypes.containsKey(crateID)){
                VirtualCrate vc = HuskyCrates.instance.crateUtilities.crateTypes.get(crateID);
                vc.pendingKeys.put(keyUUID.toString(),amount);
            }else{
                HuskyCrates.instance.logger.warn("ValidKeys " + keyUUID + " provides an invalid crate ID. Removing from table.");
                Statement removal = dbConnection.createStatement();
                removal.executeQuery("SELECT  * FROM CRATELOCATIONS WHERE KEYUUID=" + keyUUID.toString());
                removal.executeUpdate("DELETE FROM CRATELOCATIONS");
                removal.close();
            }
        }
        ResultSet keyBalances = dbConnection.prepareStatement("SELECT * FROM KEYBALANCES").executeQuery();
        while(keyBalances.next()){
            UUID userUUID = UUID.fromString(keyBalances.getString("userUUID"));
            String crateID = keyBalances.getString("crateID");
            int amount = keyBalances.getInt("amount");
            if(HuskyCrates.instance.crateUtilities.getCrateTypes().contains(crateID)){
                HuskyCrates.instance.crateUtilities.getVirtualCrate(crateID).virtualBalances.put(userUUID.toString(),amount);
            }else{
                HuskyCrates.instance.logger.warn("KeyBalances for UUID " + userUUID + " provides an invalid crate ID. Removing from table.");
                Statement removal = dbConnection.createStatement();
                removal.executeQuery("SELECT  * FROM KEYBALANCES WHERE USERUUID=" + userUUID.toString());
                removal.executeUpdate("DELETE FROM KEYBALANCES");
                removal.close();
            }
        }
        /*
            CRATELOCATIONS (ID INTEGER NOT NULL AUTO_INCREMENT, X DOUBLE, Y DOUBLE, Z DOUBLE, worldID INTEGER, isEntityCrate BOOLEAN, crateID CHARACTER,  PRIMARY KEY(ID))
            VALIDKEYS (keyUUID CHARACTER, crateID CHARACTER, amount INTEGER )
            KEYBALANCES (userUUID CHARACTER, crateID CHARACTER, amount INTEGER)
            WORLDINFO (ID INTEGER NOT NULL AUTO_INCREMENT,uuid CHARACTER, name CHARACTER,  PRIMARY KEY(ID))
         */
        dbConnection.close();
    }

    public static void saveHuskyData() throws SQLException {
        connectDB();


        //crate positions
        Statement cratePositionClear = dbConnection.createStatement();
        cratePositionClear.executeQuery("SELECT  * FROM CRATELOCATIONS");
        cratePositionClear.executeUpdate("DELETE FROM CRATELOCATIONS");
        cratePositionClear.execute("ALTER TABLE CRATELOCATIONS ALTER COLUMN ID RESTART WITH 1");
        cratePositionClear.close();

        Statement worldTableClear = dbConnection.createStatement();
        worldTableClear.executeQuery("SELECT  * FROM WORLDINFO");
        worldTableClear.executeUpdate("DELETE FROM WORLDINFO");
        worldTableClear.execute("ALTER TABLE WORLDINFO ALTER COLUMN ID RESTART WITH 1");
        worldTableClear.close();
        int count = 1;
        HashMap<UUID,Integer> worldsInserted = new HashMap<>();
        for(Location<World> location : HuskyCrates.instance.crateUtilities.physicalCrates.keySet()){
            if(!worldsInserted.keySet().contains(location.getExtent().getUniqueId())){
                worldsInserted.put(location.getExtent().getUniqueId(),count);
                count++;
                dbConnection.prepareStatement("INSERT INTO WORLDINFO(uuid,name) VALUES('" + location.getExtent().getUniqueId().toString() + "','"+ location.getExtent().getName() + "')").executeUpdate();
            }
            PhysicalCrate crate = HuskyCrates.instance.crateUtilities.physicalCrates.get(location);
            dbConnection.prepareStatement("INSERT INTO CRATELOCATIONS(X,Y,Z,worldID,isEntityCrate,crateID) VALUES(" + location.getX() + ","  + location.getY() + ","  + location.getZ() + ","  + worldsInserted.get(location.getExtent().getUniqueId()) + ","  + crate.isEntity + ",'"  + crate.vc.id + "')").executeUpdate();
        }

        //crate key uuids

        Statement crateKeyClear = dbConnection.createStatement();
        crateKeyClear.executeQuery("SELECT  * FROM VALIDKEYS");
        crateKeyClear.executeUpdate("DELETE FROM VALIDKEYS");
        crateKeyClear.close();



        for(VirtualCrate crate : HuskyCrates.instance.crateUtilities.crateTypes.values()){
            for(String keyUUID : crate.pendingKeys.keySet()){
                int amount = crate.pendingKeys.get(keyUUID);
                dbConnection.prepareStatement("INSERT INTO VALIDKEYS(keyUUID,crateID,amount) VALUES('" + keyUUID + "','"  + crate.id + "'," + amount + ")").executeUpdate();
                //System.out.println(g);
            }

        }

        // also user vkey balances

        Statement keyBalClear = dbConnection.createStatement();
        keyBalClear.executeQuery("SELECT  * FROM KEYBALANCES");
        keyBalClear.executeUpdate("DELETE FROM KEYBALANCES");
        keyBalClear.close();



        for(String vcID : HuskyCrates.instance.crateUtilities.getCrateTypes()){
            VirtualCrate vc = HuskyCrates.instance.crateUtilities.getVirtualCrate(vcID);
            String crateID = vc.id;
            for(String uuid : vc.virtualBalances.keySet()) {
                int amount = vc.virtualBalances.get(uuid);
                dbConnection.prepareStatement("INSERT INTO KEYBALANCES(userUUID,crateID,amount) VALUES('" + uuid + "','" + crateID + "'," + amount + ")").executeUpdate();
            }
        }


        dbConnection.close();
    }
}
