package com.codehusky.huskycrates.crate.physical;

import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EffectInstance {
    private long ticks = 0;
    private Effect effect;
    private Location<World> location;
    public EffectInstance(Effect effect, Location<World> location){
        this.effect = effect;
        this.location = location;
    }

    public void tick() {
        ticks = effect.tick(ticks,location);
    }

    public void resetEffect(){
        effect.reset();
        ticks = 0;
    }

}
