package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.exception.DoubleRegistrationError;
import javafx.util.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * This class is intended to contain Crate and Keys, Crate locations w/ virtual,
 */
public class Registry {
    private HashMap<String, Key> keys = new HashMap<>();
    private HashMap<String, Crate> crates = new HashMap<>();

    private HashMap<UUID, HashMap<String, Integer>> virtualKeys = new HashMap<>();
    private HashMap<UUID, HashSet<String>> dirtyVirtualKeys = new HashMap<>();

    private HashMap<UUID, Pair<String,Integer>> keysInCirculation = new HashMap<>();
    private HashSet<UUID> dirtyKeysInCirculation = new HashSet<>();

    private HashMap<Location<World>, PhysicalCrate> physicalCrates = new HashMap<>();

    private HashSet<Location<World>> dirtyPhysicalCrates = new HashSet<>();

    public Key getKey(String id){
        //handling local keys.
        if(!isKey(id)) return null;

        if(id.indexOf("LOCALKEY_") == 0){
            return crates.get(id.replace("LOCALKEY_","")).getLocalKey();
        }
        return keys.get(id);
    }

    public Crate getCrate(String id){
        if(!isCrate(id)) return null;
        return crates.get(id);
    }

    public PhysicalCrate getPhysicalCrate(Location<World> location){
        if(!isPhysicalCrate(location)) return null;
        return physicalCrates.get(location);
    }

    public HashMap<Location<World>, PhysicalCrate> getPhysicalCrates() {
        return physicalCrates;
    }

    public boolean isSecureKey(String crateID, UUID uuid){
        return keysInCirculation.containsKey(uuid) && keysInCirculation.get(uuid).getKey().equals(crateID) && keysInCirculation.get(uuid).getValue() > 0;
    }

    public UUID generateSecureKey(String crateID){
        return this.generateSecureKey(crateID,1);
    }

    public UUID generateSecureKey(String crateID, int amount){
        UUID uuid = UUID.randomUUID();
        keysInCirculation.put(uuid,new Pair<>(crateID,amount));
        dirtyKeysInCirculation.add(uuid);
        return uuid;
    }

    public boolean isKey(String id){
        if(id.indexOf("LOCALKEY_") == 0){
            if(crates.containsKey(id.replace("LOCALKEY_","")))
                return crates.get(id.replace("LOCALKEY_","")).hasLocalKey();
        }
        return keys.containsKey(id);
    }

    public boolean isCrate(String id){
        return crates.containsKey(id);
    }

    public boolean isPhysicalCrate(Location<World> location){
        return physicalCrates.containsKey(location);
    }

    public HashMap<String, Key> getKeys() {
        return keys;
    }

    public HashMap<String, Crate> getCrates() {
        return crates;
    }

    public void registerCrate(Crate crate){
        if(isCrate(crate.getId())) throw new DoubleRegistrationError("Crate with id " + crate.getId() + " already is registered");
        crates.put(crate.getId(),crate);
    }

    public void registerKey(Key key){
        if(isKey(key.getId())) throw new DoubleRegistrationError("Key with id " + key.getId() + " already is registered");
        keys.put(key.getId(),key);
    }

    public void registerPhysicalCrate(PhysicalCrate physicalCrate){
        if(physicalCrates.containsKey(physicalCrate.getLocation())) throw new DoubleRegistrationError("Crate is already located at " + physicalCrate.getLocation().toString());
        physicalCrates.put(physicalCrate.getLocation(),physicalCrate);
        dirtyPhysicalCrates.add(physicalCrate.getLocation());
    }

    public void unregisterPhysicalCrate(Location<World> location){
        physicalCrates.remove(location);
        dirtyPhysicalCrates.add(location);
    }

    /**
     * methods for use in database management
     * @return
     */
    public HashSet<Location<World>> getDirtyPhysicalCrates() {
        return dirtyPhysicalCrates;
    }

    public boolean cleanPhysicalCrate(Location<World> location){
        return dirtyPhysicalCrates.remove(location);
    }


    public void clearRegistry(){
        clearConfigRegistry();
        clearDBRegistry();
    }

    public void clearConfigRegistry() {
        crates.clear();
        keys.clear();
    }

    public void clearDBRegistry() {
        physicalCrates.clear();
        dirtyPhysicalCrates.clear();

        keysInCirculation.clear();
        dirtyKeysInCirculation.clear();

        virtualKeys.clear();
        dirtyVirtualKeys.clear();
    }

    private Connection getConnection() {

        DataSource dbSource = null;
        try {
            dbSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:h2:" + HuskyCrates.instance.configDir.resolve("data"));
            Connection connection = dbSource.getConnection();
            boolean cP = connection.getMetaData().getTables(null,null,"CRATELOCATIONS",null).next();
            boolean cKU = connection.getMetaData().getTables(null,null,"VALIDKEYS",null).next();
            boolean kB = connection.getMetaData().getTables(null,null,"KEYBALANCES",null).next();
            boolean cD = connection.getMetaData().getTables(null,null,"LASTUSED",null).next();
            //TODO: TABLE PREFIX
            if(!cP){
                connection.prepareStatement("CREATE TABLE CRATELOCATIONS (ID INTEGER NOT NULL AUTO_INCREMENT, X DOUBLE, Y DOUBLE, Z DOUBLE, worldUUID CHARACTER, isEntityCrate BOOLEAN, crateID CHARACTER,  PRIMARY KEY(ID))").executeUpdate();
            }
            if(!cKU){
                connection.prepareStatement("CREATE TABLE VALIDKEYS (keyUUID CHARACTER, crateID CHARACTER, amount INTEGER )").executeUpdate();
            }
            if(!kB){
                connection.prepareStatement("CREATE TABLE KEYBALANCES (userUUID CHARACTER, crateID CHARACTER, amount INTEGER)").executeUpdate();
            }
            if(!cD){
                connection.prepareStatement("CREATE TABLE LASTUSED (userUUID CHARACTER, crateID CHARACTER, lastUsed BIGINT)").executeUpdate();
            }
            return  connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadFromDatabase(){
        Connection connection = getConnection();
        if(connection == null) {
            HuskyCrates.instance.logger.error("SQL LOAD FAILURE");
            return;
        }
        HuskyCrates.instance.logger.info("Begin Database Load...");
        try {
            HuskyCrates.registry.clearDBRegistry();

            ResultSet physicalCrates = connection.prepareStatement("SELECT * FROM CRATELOCATIONS").executeQuery();
            while(physicalCrates.next()){
                //HuskyCrates.instance.logger.info("cratePositions thing!");
                int id = physicalCrates.getInt("ID");
                double x = physicalCrates.getDouble("X");
                double y = physicalCrates.getDouble("Y");
                double z = physicalCrates.getDouble("Z");
                UUID worldUUID = UUID.fromString(physicalCrates.getString("worldUUID"));
                boolean entityCrate = physicalCrates.getBoolean("isEntityCrate");
                String crateID = physicalCrates.getString("crateID");


                if(Sponge.getServer().getWorld(worldUUID).isPresent() && this.isCrate(crateID)){ //VALID WORLD
                    World world = Sponge.getServer().getWorld(worldUUID).get();
                    Location<World> loco = new Location<>(world,x,y,z);
                    if(loco.getBlock().getType().equals(BlockTypes.AIR)){
                        this.dirtyPhysicalCrates.add(loco);
                        HuskyCrates.instance.logger.warn("CrateLocation #" + id + " provides a location where there is not a block. Flagging for removal.");
                    }else {
                        this.registerPhysicalCrate(new PhysicalCrate(loco, crateID, entityCrate));
                        HuskyCrates.instance.logger.info("Loaded " + crateID + " @ " + x + "," + y + "," + z + ((entityCrate) ? " (ENTITY CRATE)" : ""));
                    }
                }else{
                    HuskyCrates.instance.logger.warn("CrateLocation #" + id + " provides an invalid world UUID or invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM CRATELOCATIONS WHERE ID=" + id);
                    removal.executeUpdate("DELETE FROM CRATELOCATIONS");
                    removal.close();
                }
            }

            //TODO: last used
            ResultSet crateKeyUUIDs = connection.prepareStatement("SELECT * FROM VALIDKEYS").executeQuery();
            while(crateKeyUUIDs.next()){
                //HuskyCrates.instance.logger.info("crateKeyUUIDs thing!");
                UUID keyUUID = UUID.fromString(crateKeyUUIDs.getString("keyUUID"));
                String crateID = crateKeyUUIDs.getString("crateID");
                int amount = crateKeyUUIDs.getInt("amount");
                if(this.isCrate(crateID)){
                    this.keysInCirculation.put(keyUUID,new Pair<>(crateID,amount));
                }else{
                    HuskyCrates.instance.logger.warn("ValidKeys " + keyUUID + " provides an invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM CRATELOCATIONS WHERE KEYUUID=" + keyUUID.toString());
                    removal.executeUpdate("DELETE FROM CRATELOCATIONS");
                    removal.close();
                }
            }

            ResultSet keyBalances = connection.prepareStatement("SELECT * FROM KEYBALANCES").executeQuery();
            //HuskyCrates.instance.logger.info("wasNull: " + keyBalances.wasNull());
            //HuskyCrates.instance.logger.info("isClosed: " + keyBalances.isClosed());
            while(keyBalances.next()){
                //HuskyCrates.instance.logger.info("keyBalances thing!");
                UUID userUUID = UUID.fromString(keyBalances.getString("userUUID"));
                String crateID = keyBalances.getString("crateID");
                int amount = keyBalances.getInt("amount");
                if(this.isCrate(crateID)){
                    HashMap<String,Integer> t = new HashMap<>();
                    if(this.virtualKeys.containsKey(userUUID)){
                        t = this.virtualKeys.get(userUUID);
                    }
                    t.put(crateID,amount);
                    this.virtualKeys.put(userUUID, t);
                }else{
                    HuskyCrates.instance.logger.warn("KeyBalances for UUID " + userUUID + " provides an invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM KEYBALANCES WHERE USERUUID=" + userUUID.toString());
                    removal.executeUpdate("DELETE FROM KEYBALANCES");
                    removal.close();
                }
            }
            connection.close();
            HuskyCrates.instance.logger.info("End Database Load.");
        }catch (SQLException e){
            e.printStackTrace();
            HuskyCrates.instance.logger.error("SQL LOAD QUERY FAILURE");
            try {
                connection.close();
            } catch (SQLException ignored){}
        }
    }

    public void pushDirty() {
        Connection connection = getConnection();
        if(connection == null) {
            HuskyCrates.instance.logger.error("SQL DIRTY PUSH FAILURE");
            return;
        }
        HuskyCrates.instance.logger.info("Begin Dirty Data Push...");
        try {
            for (Location<World> dcl : dirtyPhysicalCrates) {
                if(physicalCrates.containsKey(dcl)){
                    //create or update
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM CRATELOCATIONS WHERE worldUUID = ? AND X = ? AND Y = ? AND Z = ? AND isEntityCrate = ?");
                    statement.setString(1, dcl.getExtent().getUniqueId().toString());
                    statement.setDouble(2, dcl.getX());
                    statement.setDouble(3, dcl.getY());
                    statement.setDouble(4, dcl.getZ());
                    statement.setBoolean(5, physicalCrates.get(dcl).isEntity());
                    ResultSet results = statement.executeQuery();
                    boolean exists = results.next();
                    if(!exists){
                        PreparedStatement updateStatement = connection.prepareStatement("INSERT INTO CRATELOCATIONS(X,Y,Z,worldUUID,isEntityCrate,crateID) VALUES(?,?,?,?,?,?)");
                        updateStatement.setDouble(1, dcl.getX());
                        updateStatement.setDouble(2, dcl.getY());
                        updateStatement.setDouble(3, dcl.getZ());
                        updateStatement.setString(4, dcl.getExtent().getUniqueId().toString());
                        updateStatement.setBoolean(5, physicalCrates.get(dcl).isEntity());
                        updateStatement.setString(6, physicalCrates.get(dcl).getCrate().getId());

                        updateStatement.executeUpdate();

                    }
                }else{
                    //remove
                    PreparedStatement delState = connection.prepareStatement("DELETE FROM CRATELOCATIONS WHERE worldUUID = ? AND X = ? AND Y = ? AND Z = ?");
                    delState.setString(1,dcl.getExtent().getUniqueId().toString());
                    delState.setDouble(2,dcl.getX());
                    delState.setDouble(3,dcl.getY());
                    delState.setDouble(4,dcl.getZ());
                    delState.executeUpdate();
                }
            }
            dirtyPhysicalCrates.clear();

            for(UUID playerUUID: dirtyVirtualKeys.keySet()){
                for(String crateID : dirtyVirtualKeys.get(playerUUID)){
                    //create or update
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM KEYBALANCES WHERE userUUID = ? AND crateID = ?");
                    statement.setString(1,playerUUID.toString());
                    statement.setString(2,crateID);
                    ResultSet results = statement.executeQuery();
                    boolean exists = results.next();
                    if(!exists){
                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO KEYBALANCES(userUUID,crateID,amount) VALUES(?,?,?)");
                        insertStatement.setString(1,playerUUID.toString());
                        insertStatement.setString(2,crateID);
                        insertStatement.setInt(3,virtualKeys.get(playerUUID).get(crateID));

                        insertStatement.executeUpdate();
                    }else{
                        PreparedStatement uState = connection.prepareStatement("UPDATE KEYBALANCES SET amount = ? WHERE userUUID = ? AND crateID = ?");
                        uState.setInt(1,virtualKeys.get(playerUUID).get(crateID));
                        uState.setString(2,playerUUID.toString());
                        uState.setString(3,crateID);
                        uState.executeUpdate();
                    }
                }
            }
            dirtyVirtualKeys.clear();

            for(UUID keyUUID: dirtyKeysInCirculation){
                if(keysInCirculation.containsKey(keyUUID)){
                    //create or update
                    int amount = keysInCirculation.get(keyUUID).getValue();
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM VALIDKEYS WHERE keyUUID = ?");
                    statement.setString(1,keyUUID.toString());
                    ResultSet results = statement.executeQuery();
                    boolean exists = results.next();
                    if(exists){
                        PreparedStatement uState = connection.prepareStatement("UPDATE VALIDKEYS SET amount = ? WHERE keyUUID = ?");
                        uState.setInt(1,amount);
                        uState.setString(2,keyUUID.toString());
                        uState.executeUpdate();
                    }else {
                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO VALIDKEYS(keyUUID,crateID,amount) VALUES(?,?,?)");
                        insertStatement.setString(1,keyUUID.toString());
                        insertStatement.setString(2,keysInCirculation.get(keyUUID).getKey());
                        insertStatement.setInt(3,amount);

                        insertStatement.executeUpdate();
                    }
                }else{
                    //removal
                    PreparedStatement delState = connection.prepareStatement("DELETE FROM VALIDKEYS WHERE keyUUID = ?");
                    delState.setString(1,keyUUID.toString());
                    delState.executeUpdate();
                }
            }
            dirtyKeysInCirculation.clear();
            connection.close();
            //TODO: last used
            HuskyCrates.instance.logger.info("End Dirty Data Push.");
        }catch (SQLException e){
            e.printStackTrace();
            HuskyCrates.instance.logger.error("SQL DIRTY PUSH UPDATE FAILURE");
            try {
                connection.close();
            } catch (SQLException ignored){}
        }
    }
}
