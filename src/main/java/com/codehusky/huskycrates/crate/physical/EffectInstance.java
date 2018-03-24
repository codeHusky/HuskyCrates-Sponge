package com.codehusky.huskycrates.crate.physical;

import com.codehusky.huskycrates.crate.virtual.effects.Effect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EffectInstance {
    private long ticks = 0;
    private Effect effect;
    private Location<World> location;
    private Player player;

    public EffectInstance(Effect effect, Location<World> location){
        this.effect = effect.clone(); // avoid messing with other instances of the effect...
        this.location = location;
    }

    public EffectInstance(Effect effect, Location<World> location, Player player){
        this(effect,location);
        this.player = player;
    }

    public void tick() {
        ticks = effect.tick(ticks,location,player);
    }

    public void resetEffect(){
        effect.reset();
        ticks = 0;
    }

    public Effect getEffect() {
        return effect;
    }
}
