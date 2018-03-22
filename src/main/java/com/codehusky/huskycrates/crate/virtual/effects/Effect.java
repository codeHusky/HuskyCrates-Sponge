package com.codehusky.huskycrates.crate.virtual.effects;

import com.codehusky.huskycrates.crate.virtual.effects.elements.Particle;
import com.codehusky.huskycrates.crate.virtual.effects.elements.Sound;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;

public class Effect {
    private long duration;
    private boolean loop;
    private boolean resetOnTimeout;
    private boolean finished = false;
    private boolean clientSide;
    private ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<Sound> sounds = new ArrayList<>();

    private boolean disabled;

    public Effect(ConfigurationNode node){
        this.disabled = node.getNode("disabled").getBoolean(false);
        if(this.disabled) return;
        this.duration = node.getNode("duration").getLong(0);
        this.loop = node.getNode("loop").getBoolean(true);
        this.resetOnTimeout = node.getNode("resetOnTimeout").getBoolean(false);
        this.clientSide = node.getNode("clientSide").getBoolean(false);
        for(ConfigurationNode particleNode : node.getNode("particles").getChildrenList()){

            particles.add(new Particle(particleNode));
        }
    }

    public long tick(long ticks, Location<World> location){
        if(this.disabled || this.finished) return ticks;


        for(Particle particle : particles){
            particle.run(ticks,location);
        }

        if(duration > 0 && (resetOnTimeout || !loop) && ticks >= duration){
            if(!loop){
                finished = true;
            }else{
                ticks = 0;
            }
        }else {
            ticks += 1;
        }
        return ticks;
    }

    public void reset(){
        finished = false;
        duration = 0;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public boolean isFinished() {
        return finished;
    }
}
