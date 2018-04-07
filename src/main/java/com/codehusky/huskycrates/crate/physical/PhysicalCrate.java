package com.codehusky.huskycrates.crate.physical;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.exception.InvalidCrateIDError;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Stores metadata about a crate at a given location.
 */
public class PhysicalCrate {
    private Location<World> location;
    private boolean isEntity;
    private String representedCrateID;

    private EffectInstance idleEffect;
    private EffectInstance rejectEffect;
    private EffectInstance winEffect;
    private EffectInstance openEffect;

    private HologramInstance hologramInstance;

    public PhysicalCrate(Location<World> location, String representedCrateID, boolean isEntity){
        this.location = location;
        if(HuskyCrates.registry.isCrate(representedCrateID)) {
            this.representedCrateID = representedCrateID;
        }else{
            throw new InvalidCrateIDError("No crate with id " + representedCrateID);
        }
        this.isEntity = isEntity;

        System.out.println(representedCrateID);

        Crate crate = HuskyCrates.registry.getCrate(this.representedCrateID);
        if(location == null) return;
        if(crate.getIdleEffect() != null)
            idleEffect = new EffectInstance(crate.getIdleEffect(),location);

        if(crate.getRejectEffect() != null)
            rejectEffect = new EffectInstance(crate.getRejectEffect(),location);

        if(crate.getWinEffect() != null)
            winEffect = new EffectInstance(crate.getWinEffect(),location);

        if(crate.getOpenEffect() != null)
            openEffect = new EffectInstance(crate.getOpenEffect(),location);

        if(crate.getHologram() != null)
            hologramInstance = new HologramInstance(crate.getHologram(),location,isEntity);
    }

    public EffectInstance getIdleEffect() {
        return idleEffect;
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

    public void cleanup(){
        if(hologramInstance != null){
            hologramInstance.cleanup();
        }
    }
}
