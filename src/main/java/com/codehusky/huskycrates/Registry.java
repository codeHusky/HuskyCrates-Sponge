package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.exceptions.DoubleRegistrationError;

import java.util.HashMap;

/**
 * This class is intended to contain Crate and Keys, Crate locations w/ virtual,
 */
public class Registry {
    private HashMap<String, Key> keys;
    private HashMap<String, Crate> crates;

    public Key getKey(String id){
        //handling local keys.
        if(id.indexOf("LOCALKEY_") == 0){
            return crates.get(id.replace("LOCALKEY_","")).getLocalKey();
        }
        return keys.get(id);
    }

    public Crate getCrate(String id){
        return crates.get(id);
    }

    public HashMap<String, Key> getKeys() {
        return keys;
    }

    public HashMap<String, Crate> getCrates() {
        return crates;
    }

    public void registerCrate(Crate crate){
        if(getCrate(crate.getId()) != null) throw new DoubleRegistrationError("Crate with id " + crate.getId() + " already is registered");
        crates.put(crate.getId(),crate);
    }

    public void registerKey(Key key){
        if(getKey(key.getId()) != null) throw new DoubleRegistrationError("Key with id " + key.getId() + " already is registered");
        keys.put(key.getId(),key);
    }
}
