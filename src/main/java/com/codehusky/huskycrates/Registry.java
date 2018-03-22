package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.exception.DoubleRegistrationError;
import javafx.util.Pair;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
        crates.clear();
        keys.clear();
        physicalCrates.clear();
        dirtyPhysicalCrates.clear();
    }
}
