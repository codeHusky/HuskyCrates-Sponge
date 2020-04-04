package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.physical.EffectInstance;
import com.codehusky.huskycrates.crate.physical.HologramInstance;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.crate.virtual.Slot;
import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import com.codehusky.huskycrates.exception.DoubleRegistrationError;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * This class is intended to contain Crate and Keys, Crate locations w/ virtual,
 */
public class Registry {
    private HashMap<String, Key> keys = new HashMap<>();
    private HashMap<String, Crate> crates = new HashMap<>();

    private HashMap<UUID, HashMap<String, Integer>> virtualKeys = new HashMap<>();
    private Map<UUID, HashSet<String>> dirtyVirtualKeys = Maps.newConcurrentMap();

    private HashMap<UUID, Map.Entry<String,Integer>> keysInCirculation = new HashMap<>();
    private Set<UUID> dirtyKeysInCirculation = Sets.newConcurrentHashSet();

    private HashMap<UUID, HashMap<String, Long>> lastCrateUse = new HashMap<>();
    private Map<UUID, HashSet<String>> dirtyLastCrateUse = Maps.newConcurrentMap();

    private HashMap<Location<World>, PhysicalCrate> physicalCrates = new HashMap<>();

    private Set<Location<World>> dirtyPhysicalCrates = Sets.newConcurrentHashSet();

    private ArrayList<EffectInstance> effects = new ArrayList<>();

    public String stats() {
        int items = 0;
        for(Crate crate: crates.values()){
            items+= crate.getSlots().size();
        }
        return "keys: " + keys.size() + "\ncrates: " + crates.size() + "\nslot sum: " + items + "\nphysicalCrates: " + physicalCrates.size() + "\nrunningEffects: " + effects.size();
    }

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

    public boolean isSecureKey(String keyID, UUID uuid){
        return keysInCirculation.containsKey(uuid) && keysInCirculation.get(uuid).getKey().equals(keyID) && keysInCirculation.get(uuid).getValue() > 0;
    }

    public UUID generateSecureKey(String keyID){
        return this.generateSecureKey(keyID,1);
    }

    public UUID generateSecureKey(String keyID, int amount){
        if(!isKey(keyID)) return null;

        UUID uuid = UUID.randomUUID();
        keysInCirculation.put(uuid,new AbstractMap.SimpleEntry<>(keyID,amount));
        dirtyKeysInCirculation.add(uuid);
        return uuid;
    }

    public boolean validateSecureKey(ItemStack stack, int amount){
        String keyID = Key.extractKeyId(stack);
        UUID keyUUID = Key.extractKeyUUID(stack);
        if(!isKey(keyID)) return false;

        if(keysInCirculation.containsKey(keyUUID)){
            if(keysInCirculation.get(keyUUID).getKey().equals(keyID)){
                if(keysInCirculation.get(keyUUID).getValue() >= amount){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean consumeSecureKey(ItemStack stack, int amount){
        String keyID = Key.extractKeyId(stack);
        UUID keyUUID = Key.extractKeyUUID(stack);
        if(!isKey(keyID)) return false;

        if(keysInCirculation.containsKey(keyUUID)){
            if(keysInCirculation.get(keyUUID).getKey().equals(keyID)){
                if(keysInCirculation.get(keyUUID).getValue() >= amount){
                    if(keysInCirculation.get(keyUUID).getValue() == amount){
                        keysInCirculation.remove(keyUUID);
                    }else {
                        keysInCirculation.put(keyUUID,new AbstractMap.SimpleEntry<>(keyID, keysInCirculation.get(keyUUID).getValue() - amount));
                    }

                    dirtyKeysInCirculation.add(keyUUID);
                    return true;
                }
            }
        }
        return false;
    }

    public void runEffect(Effect effect, Location<World> location){
        effects.add(new EffectInstance(effect,location));
    }

    public void runClientEffect(Effect effect, Location<World> location, Player player){
        effects.add(new EffectInstance(effect,location,player));
    }

    public ArrayList<EffectInstance> getEffects() {
        return effects;
    }

    public void removeEffect(EffectInstance instance){
        effects.remove(instance);
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

    public HashMap<String, Key> getAllKeys() {
        HashMap<String, Key> lkeys = getKeys();
        lkeys.putAll(getLocalKeys());
        return lkeys;
    }

    public HashMap<String, Key> getLocalKeys() {
        HashMap<String, Key> lkeys = new HashMap<>();
        for(Crate crate : crates.values()){
            if(crate.hasLocalKey()){
                lkeys.put(crate.getLocalKey().getId(),crate.getLocalKey());
            }
        }
        return lkeys;
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

    public Long getLastUse(String crateID, UUID playerUUID){
       if(lastCrateUse.containsKey(playerUUID)){
           if(lastCrateUse.get(playerUUID).containsKey(crateID)){
               return lastCrateUse.get(playerUUID).get(crateID);
           }
       }
       return null;
    }

    public boolean addVirtualKeys(UUID playerUUID, String keyID, Integer amount){
        if(isKey(keyID)){
            HashMap<String, Integer> balances = virtualKeys.getOrDefault(playerUUID,new HashMap<>());
            balances.put(keyID,(balances.containsKey(keyID))?amount + balances.get(keyID):amount);
            virtualKeys.put(playerUUID,balances);

            HashSet<String> ud = dirtyVirtualKeys.getOrDefault(playerUUID,new HashSet<>());
            ud.add(keyID);
            dirtyVirtualKeys.put(playerUUID,ud);
            return true;
        }
        return false;
    }

    public boolean removeVirtualKeys(UUID playerUUID, String keyID, Integer amount){
        return addVirtualKeys(playerUUID,keyID,-amount);
    }

    public boolean setVirtualKeysNoDirty(UUID playerUUID, String keyID, Integer amount){
        if(isKey(keyID)){
            HashMap<String, Integer> balances = virtualKeys.getOrDefault(playerUUID,new HashMap<>());
            balances.put(keyID,amount);
            virtualKeys.put(playerUUID,balances);

            return true;
        }
        return false;
    }

    public boolean setVirtualKeys(UUID playerUUID, String keyID, Integer amount){
        if(isKey(keyID)){
            HashMap<String, Integer> balances = virtualKeys.getOrDefault(playerUUID,new HashMap<>());
            balances.put(keyID,amount);
            virtualKeys.put(playerUUID,balances);

            HashSet<String> ud = dirtyVirtualKeys.getOrDefault(playerUUID,new HashSet<>());
            ud.add(keyID);
            dirtyVirtualKeys.put(playerUUID,ud);
            return true;
        }
        return false;
    }

    public Integer getVirtualKeyBalance(UUID playerUUID, String keyID){
        if(virtualKeys.containsKey(playerUUID)){
            if(virtualKeys.get(playerUUID).containsKey(keyID)){
                return virtualKeys.get(playerUUID).get(keyID);
            }
        }
        return 0;
    }

    public HashMap<String, Integer> getVirtualKeyBalances(UUID playerUUID){
        if(virtualKeys.containsKey(playerUUID)){
            return virtualKeys.get(playerUUID);
        }
        return new HashMap<>();
    }

    public void updateLastUse(String crateID, UUID playerUUID){
        HashMap<String, Long> userData = new HashMap<>();
        if(lastCrateUse.containsKey(playerUUID)){
            userData = lastCrateUse.get(playerUUID);
        }
        userData.put(crateID,System.currentTimeMillis());
        lastCrateUse.put(playerUUID,userData);
        HashSet<String> modified = dirtyLastCrateUse.get(playerUUID);
        if(modified == null) modified = new HashSet<>();
        modified.add(crateID);
        dirtyLastCrateUse.put(playerUUID,modified);
    }

    /**
     * methods for use in database management
     * @return
     */
    public Set<Location<World>> getDirtyPhysicalCrates() {
        return dirtyPhysicalCrates;
    }

    public boolean cleanPhysicalCrate(Location<World> location){
        return dirtyPhysicalCrates.remove(location);
    }

    public void cleanAll() {
        dirtyPhysicalCrates.clear();
        dirtyVirtualKeys.clear();
        dirtyKeysInCirculation.clear();
        dirtyLastCrateUse.clear();
    }

    public void clearRegistry(){
        clearConfigRegistry();
        clearDBRegistry();
        effects.forEach(effect -> {
            effect.resetEffect();
        });
        effects.clear();
    }

    public void clearConfigRegistry() {
        crates.clear();
        keys.clear();
    }

    public void clearDBRegistry() {
        physicalCrates.forEach((location, physicalCrate) -> {
            physicalCrate.cleanup();
        });
        physicalCrates.clear();
        dirtyPhysicalCrates.clear();

        keysInCirculation.clear();
        dirtyKeysInCirculation.clear();

        virtualKeys.clear();
        dirtyVirtualKeys.clear();

        lastCrateUse.clear();
        dirtyLastCrateUse.clear();
    }

    public void injectSlot(String crateID, Slot slot) {
        crates.get(crateID).injectSlot(slot);
    }

    public void postInjection() {
        crates.forEach((id, crate) -> {
            crate.postInjectionChecks();
        });
    }
    private Connection getConnection() {
        DataSource dbSource = null;
        try {
            dbSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:h2:" + HuskyCrates.instance.configDir.resolve("storage/data"));
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
                connection.prepareStatement("CREATE TABLE KEYBALANCES (userUUID CHARACTER, keyID CHARACTER, amount INTEGER)").executeUpdate();
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

    private Connection getVirtualKeySQLConnection() {
        DataSource dbSource = null;
        try {
            dbSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource(HuskyCrates.instance, Objects.requireNonNull(Util.getJDBC()));
            Connection connection = dbSource.getConnection();
            boolean kB = connection.getMetaData().getTables(null,null,"KEYBALANCES",null).next();
            if(!kB){
                connection.prepareStatement("CREATE TABLE KEYBALANCES (userUUID CHAR(36), keyID CHAR(100), amount INT)").executeUpdate();
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Connection getAppropriateVirtualKeyConnection() {
        if(HuskyCrates.instance.virtualKeyDB){
            return getVirtualKeySQLConnection();
        }
        else{
            return getConnection();
        }
    }

    public void loadFromDatabase(){
        Connection connection = getConnection();
        Connection virtualKeyConnection = getAppropriateVirtualKeyConnection();

        if(connection == null || virtualKeyConnection == null) {
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
                        HologramInstance.cleanup(loco);
                    }else {
                        this.registerPhysicalCrate(new PhysicalCrate(loco, crateID, entityCrate));
                        HuskyCrates.instance.logger.info("Loaded " + crateID + " @ " + x + "," + y + "," + z + ((entityCrate) ? " (ENTITY CRATE)" : ""));
                    }
                }else{
                    if(!this.isCrate(crateID) && Sponge.getServer().getWorld(worldUUID).isPresent()){
                        World world = Sponge.getServer().getWorld(worldUUID).get();
                        Location<World> loco = new Location<>(world,x,y,z);
                        HologramInstance.cleanup(loco);
                    }
                    HuskyCrates.instance.logger.warn("CrateLocation #" + id + " provides an invalid world UUID or invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM CRATELOCATIONS WHERE ID='" + id + "'");
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
                if(this.isCrate(crateID) || this.isKey(crateID)){
                    this.keysInCirculation.put(keyUUID,new AbstractMap.SimpleEntry<>(crateID,amount));
                }else{
                    HuskyCrates.instance.logger.warn("ValidKeys " + keyUUID + " provides an invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM VALIDKEYS WHERE KEYUUID='" + keyUUID.toString() + "'");
                    removal.executeUpdate("DELETE FROM VALIDKEYS");
                    removal.close();
                }
            }

            ResultSet keyBalances = virtualKeyConnection.prepareStatement("SELECT * FROM KEYBALANCES").executeQuery();
            //HuskyCrates.instance.logger.info("wasNull: " + keyBalances.wasNull());
            //HuskyCrates.instance.logger.info("isClosed: " + keyBalances.isClosed());
            while(keyBalances.next()){
                //HuskyCrates.instance.logger.info("keyBalances thing!");
                UUID userUUID = UUID.fromString(keyBalances.getString("userUUID"));
                String keyID = keyBalances.getString("keyID");
                int amount = keyBalances.getInt("amount");
                if(this.isKey(keyID)){
                    HashMap<String,Integer> t = new HashMap<>();
                    if(this.virtualKeys.containsKey(userUUID)){
                        t = this.virtualKeys.get(userUUID);
                    }
                    t.put(keyID,amount);
                    this.virtualKeys.put(userUUID, t);
                }else{
                    HuskyCrates.instance.logger.warn("A Key Balance for UUID " + userUUID + " provides an invalid key ID. Removing from table.");
                    Statement removal = virtualKeyConnection.createStatement();
                    removal.executeQuery("SELECT  * FROM KEYBALANCES WHERE USERUUID='" + userUUID.toString() + "'");
                    removal.executeUpdate("DELETE FROM KEYBALANCES");
                    removal.close();
                }
            }

            ResultSet lastUses = connection.prepareStatement("SELECT * FROM LASTUSED").executeQuery();
            //HuskyCrates.instance.logger.info("wasNull: " + keyBalances.wasNull());
            //HuskyCrates.instance.logger.info("isClosed: " + keyBalances.isClosed());
            /*
                    TABLE LASTUSED (userUUID CHARACTER, crateID CHARACTER, lastUsed BIGINT)
                     */
            while(lastUses.next()){
                //HuskyCrates.instance.logger.info("keyBalances thing!");
                UUID userUUID = UUID.fromString(lastUses.getString("userUUID"));
                String crateID = lastUses.getString("crateID");
                long lastUsed = lastUses.getLong("lastUsed");
                if(this.isCrate(crateID)){
                    HashMap<String,Long> plu;
                    if(lastCrateUse.containsKey(userUUID)){
                        plu = lastCrateUse.get(userUUID);
                    }else {
                        plu = new HashMap<>();
                    }
                    plu.put(crateID,lastUsed);
                    lastCrateUse.put(userUUID,plu);
                }else{
                    HuskyCrates.instance.logger.warn("KeyBalances for UUID " + userUUID + " provides an invalid crate ID. Removing from table.");
                    Statement removal = connection.createStatement();
                    removal.executeQuery("SELECT  * FROM LASTUSED WHERE userUUID='" + userUUID.toString() + "' AND crateID='" + crateID + "'");
                    removal.executeUpdate("DELETE FROM LASTUSED");
                    removal.close();
                }
            }
            connection.close();
            virtualKeyConnection.close();
            cleanAll();
            HuskyCrates.instance.logger.info("End Database Load.");
        }catch (SQLException e){
            e.printStackTrace();
            HuskyCrates.instance.logger.error("SQL LOAD QUERY FAILURE");
            try {
                connection.close();
                virtualKeyConnection.close();
            } catch (SQLException ignored){}
        }
    }

    public void pushDirtyVirtualKeys() {
        Connection virtualKeyConnection = getAppropriateVirtualKeyConnection();
        if(virtualKeyConnection == null) {
            HuskyCrates.instance.logger.error("SQL DIRTY PUSH FAILURE");
            return;
        }
        try{
            for(Map.Entry<UUID, HashSet<String>> entry : dirtyVirtualKeys.entrySet()){
                UUID playerUUID = entry.getKey();
                for(String keyID : entry.getValue()){
                    //create or update
                    PreparedStatement statement = virtualKeyConnection.prepareStatement("SELECT * FROM KEYBALANCES WHERE userUUID = ? AND keyID = ?");
                    statement.setString(1,playerUUID.toString());
                    statement.setString(2,keyID);
                    ResultSet results = statement.executeQuery();
                    boolean exists = results.next();
                    if(!exists){
                        PreparedStatement insertStatement = virtualKeyConnection.prepareStatement("INSERT INTO KEYBALANCES(userUUID,keyID,amount) VALUES(?,?,?)");
                        insertStatement.setString(1,playerUUID.toString());
                        insertStatement.setString(2,keyID);
                        insertStatement.setInt(3,virtualKeys.get(playerUUID).get(keyID));

                        insertStatement.executeUpdate();
                    }else{
                        PreparedStatement uState = virtualKeyConnection.prepareStatement("UPDATE KEYBALANCES SET amount = ? WHERE userUUID = ? AND keyID = ?");
                        uState.setInt(1,virtualKeys.get(playerUUID).get(keyID));
                        uState.setString(2,playerUUID.toString());
                        uState.setString(3,keyID);
                        uState.executeUpdate();
                    }
                }
                dirtyVirtualKeys.remove(entry);
            }
        virtualKeyConnection.close();
        }catch (SQLException e){
            e.printStackTrace();
            HuskyCrates.instance.logger.error("SQL DIRTY PUSH UPDATE FAILURE");
        try {
            virtualKeyConnection.close();
        } catch (SQLException ignored){}
        }
    }

    public void pushDirty() {
        if(dirtyKeysInCirculation.isEmpty() &&
                dirtyVirtualKeys.isEmpty() &&
                dirtyPhysicalCrates.isEmpty()) return;

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
                dirtyPhysicalCrates.remove(dcl);
            }

            pushDirtyVirtualKeys();

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
                }else {
                    //removal
                    PreparedStatement delState = connection.prepareStatement("DELETE FROM VALIDKEYS WHERE keyUUID = ?");
                    delState.setString(1, keyUUID.toString());
                    delState.executeUpdate();
                }
                dirtyKeysInCirculation.remove(keyUUID);
            }

            for(Map.Entry<UUID, HashSet<String>> entry : dirtyLastCrateUse.entrySet()){
                UUID playerUUID = entry.getKey();
                for(String crateID : entry.getValue()){
                    /*
                    TABLE LASTUSED (userUUID CHARACTER, crateID CHARACTER, lastUsed BIGINT)
                     */
                    PreparedStatement statement = connection.prepareStatement("SELECT * FROM LASTUSED  WHERE userUUID = ? AND crateID = ?");
                    statement.setString(1,playerUUID.toString());
                    statement.setString(2,crateID);
                    ResultSet results = statement.executeQuery();
                    boolean exists = results.next();
                    if(exists){
                        PreparedStatement uState = connection.prepareStatement("UPDATE LASTUSED  SET lastUsed = ? WHERE userUUID = ? AND crateID = ?");
                        uState.setLong(1,lastCrateUse.get(playerUUID).get(crateID));
                        uState.setString(2,playerUUID.toString());
                        uState.setString(3,crateID);
                        uState.executeUpdate();
                    }else {
                        PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO LASTUSED(userUUID,crateID,lastUsed) VALUES(?,?,?)");
                        insertStatement.setString(1,playerUUID.toString());
                        insertStatement.setString(2,crateID);
                        insertStatement.setLong(3,lastCrateUse.get(playerUUID).get(crateID));

                        insertStatement.executeUpdate();
                    }
                }
                dirtyLastCrateUse.remove(entry);
            }

            connection.close();
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
