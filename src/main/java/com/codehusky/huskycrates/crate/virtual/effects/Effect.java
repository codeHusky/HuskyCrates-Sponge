package com.codehusky.huskycrates.crate.virtual.effects;

import com.codehusky.huskycrates.crate.virtual.effects.elements.Particle;
import com.codehusky.huskycrates.crate.virtual.effects.elements.Sound;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
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

    public int getParticleCount() {
        return particles.size();
    }

    public Effect(boolean disabled, long duration, boolean loop, boolean resetOnTimeout, boolean clientSide, ArrayList<Particle> particles){
        this.disabled = disabled;
        this.duration = duration;
        this.loop = loop;
        this.resetOnTimeout = resetOnTimeout;
        this.clientSide = clientSide;
        this.particles = particles;
    }

    public Effect clone() {
        return new Effect(this.disabled,this.duration,this.loop,this.resetOnTimeout,this.clientSide,(ArrayList<Particle>)this.particles.clone());
    }

    public boolean isDisabled() {
        return disabled;
    }

    public long tick(long ticks, Location<World> location, Player player){
        if(this.disabled || this.finished) return ticks;



        for(Particle particle : particles){
            particle.run(ticks,location,player);
        }

        if(ticks >= (duration-1) && (resetOnTimeout || !loop)){
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

    public long tick(long ticks, Location<World> location){
        return this.tick(ticks,location,null);
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
