package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.codehusky.huskycrates.exception.ConfigParseError;
import com.codehusky.huskycrates.exception.InjectionDataError;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import com.sun.istack.internal.NotNull;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Optional;

public class Particle {
    private ParticleEffect particle;
    private ParticlePattern pattern;
    private int amount;
    private int quantity;
    private Vector3d offset;
    private Vector3d position;

    private ArrayList<ParticleEffect> palette = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> art = new ArrayList<>();

    public Particle(@NotNull ParticleEffect particle, int amount, int quantity, Vector3d offset, Vector3d position, Boolean animateColor, @NotNull ParticlePattern pattern) {
        this.particle = particle;
        this.amount = amount;
        this.quantity = quantity;
        this.offset = offset;
        this.position = (position == null)?new Vector3d(0.5,0.5,0.5):position;
        this.pattern = pattern;
        if(this.pattern == null){
            throw new InjectionDataError("Must provide animation instructions for a Particle.");
        }

    }

    public Particle(ConfigurationNode node){
        Optional<ParticleType> pPT = Sponge.getRegistry().getType(ParticleType.class,node.getNode("type").getString(""));
        if(!pPT.isPresent()){
            throw new ConfigParseError("Invalid particle type!",node.getNode("type").getPath());
        }

        this.amount = node.getNode("amount").getInt(1);

        this.quantity = node.getNode("quantity").getInt(1);

        if(!node.getNode("offset").isVirtual() && node.getNode("offset").hasListChildren()){
            this.offset = new Vector3d(node.getNode("offset",0).getDouble(0),node.getNode("offset",1).getDouble(0),node.getNode("offset",2).getDouble(0));
        }

        if(!node.getNode("position").isVirtual() && node.getNode("position").hasListChildren()){
            this.position = new Vector3d(node.getNode("position",0).getDouble(0),node.getNode("position",1).getDouble(0),node.getNode("position",2).getDouble(0));
        }else{
            this.position = new Vector3d(0.5,0.5,0.5);
        }

        if(!node.getNode("palette").isVirtual()){
            for(ConfigurationNode colorNode : node.getNode("palette").getChildrenList()){
                ParticleEffect.Builder builder = ParticleEffect.builder();
                builder.type(pPT.get());
                builder.quantity(quantity);
                if(offset != null){
                    builder.offset(offset);
                }
                Color clr = Color.ofRgb(colorNode.getNode(0).getInt(0),colorNode.getNode(1).getInt(0),colorNode.getNode(2).getInt(0));
                builder.option(ParticleOptions.COLOR,clr);
                this.palette.add(builder.build());
            }
            if(node.getNode("art").isVirtual()){
                throw new ConfigParseError("Palette is defined but there is no art to go along with it.",node.getNode("art").getPath());
            }else{

                ArrayList potentialArt;
                try {
                    potentialArt = node.getNode("art").getValue(TypeToken.of(ArrayList.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ConfigParseError("Art is in invalid format.",node.getNode("art").getPath());
                }

                this.art = potentialArt;

            }
        }else {
            ParticleEffect.Builder builder = ParticleEffect.builder();
            builder.type(pPT.get());
            if(!node.getNode("color").isVirtual() && node.getNode("color").hasListChildren()){
                builder.option(ParticleOptions.COLOR,Color.ofRgb(node.getNode("color").getNode(0).getInt(0),node.getNode("color").getNode(1).getInt(0),node.getNode("color").getNode(2).getInt(0)));
            }
            builder.quantity(quantity);
            if(offset != null){
                builder.offset(offset);
            }
            this.particle = builder.build();
        }
        if(node.getNode("animationPreset").isVirtual()) {
            String code = node.getNode("animationCode").getString("x=Math.sin(time/5)*0.7;z=Math.cos(time/5)*0.7;");
            boolean animateColor = node.getNode("animateColor").getBoolean(false);
            this.pattern = new JavaScriptParticlePattern(code, !animateColor);
        }else{
            PresetParticlePattern.ParticlePreset particlePreset;
            PresetParticlePattern.ColorPreset colorPreset;
            try {
                particlePreset = node.getNode("animationPreset").getValue(
                        TypeToken.of(PresetParticlePattern.ParticlePreset.class));
            } catch (ObjectMappingException ex) {
                throw new ConfigParseError("Invalid animation preset specified!", node.getNode("animationPreset").getPath());
            }
            if (particlePreset == null) {
                throw new ConfigParseError("Invalid animation preset specified!", node.getNode("animationPreset").getPath());
            }
            try {
                colorPreset = node.getNode("colorAnimationPreset").getValue(
                        TypeToken.of(PresetParticlePattern.ColorPreset.class));
            } catch (ObjectMappingException ex) {
                throw new ConfigParseError("Invalid color preset specified!", node.getNode("colorAnimationPreset").getPath());
            }
            this.pattern = new PresetParticlePattern(particlePreset, colorPreset);
        }

    }

    public Particle(ParticleEffect effect, Vector3d position){
        this.particle = effect;
        this.amount = 1;
        this.position = position;
    }

    public void run(long tick, Location<World> location, Player player) {
        for(int i = 0; i < amount; i++) {
            ParticleEffect ourParticle = particle;
            Pair<Vector3d, Optional<Color>> pair = this.pattern.getPositionForFrame(tick, i + 1);
            Vector3d position = pair.getLeft();
            Optional<Color> color = pair.getRight();
            if (color.isPresent()) {
                ourParticle = ParticleEffect.builder()
                        .from(ourParticle)
                        .option(ParticleOptions.COLOR, color.get())
                        .build();
            }
            Vector3d particlePos = location.getPosition().clone().add(this.position).add(position);
            if(player != null){
                player.spawnParticles(ourParticle,particlePos);
            }else {
                location.getExtent().spawnParticles(ourParticle, particlePos);
            }
        }
    }
}
