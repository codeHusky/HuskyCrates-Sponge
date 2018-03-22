package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.exception.DoubleRegistrationError;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;

/**
 * This class is intended to contain Crate and Keys, Crate locations w/ virtual,
 */
public class Registry {
    private HashMap<String, Key> keys = new HashMap<>();
    private HashMap<String, Crate> crates = new HashMap<>();

    private HashMap<Location<World>, PhysicalCrate> physicalCrates = new HashMap<>();

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
    }

    public void clearRegistry(){
        crates.clear();
        keys.clear();
        physicalCrates.clear();
    }
}
