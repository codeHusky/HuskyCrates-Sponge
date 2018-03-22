package com.codehusky.huskycrates.crate.physical;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.Crate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Stores metadata about a crate at a given location.
 */
public class PhysicalCrate {
    private Location<World> location;
    private boolean isEntity;
    private String representedCrateID;

    public PhysicalCrate(Location<World> location, String representedCrateID, boolean isEntity){
        this.location = location;
        if(HuskyCrates.registry.isCrate(representedCrateID)) {
            this.representedCrateID = representedCrateID;
        }else{

        }
        this.isEntity = isEntity;
    }

    public PhysicalCrate(Location<World> location, String representedCrateID){
        this(location,representedCrateID,false);
    }

    public Crate getCrate(){
        return HuskyCrates.registry.getCrate(representedCrateID);
    }

    public Location<World> getLocation() {
        return location;
    }

    public boolean isEntity() {
        return isEntity;
    }
}
