package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.exception.ConfigParseError;
import com.google.common.reflect.TypeToken;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.script.*;
import java.util.ArrayList;
import java.util.Optional;

public class Particle {
    private ParticleEffect particle;
    private String animationCode;
    private Boolean animateColor;
    private CompiledScript compiled;

    private ArrayList<ParticleEffect> palette = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> art = new ArrayList();


    public Particle(ConfigurationNode node){
        Optional<ParticleType> pPT = Sponge.getRegistry().getType(ParticleType.class,node.getNode("type").getString(""));
        if(!pPT.isPresent()){
            throw new ConfigParseError("Invalid particle type!",node.getNode("type").getPath());
        }


        if(!node.getNode("palette").isVirtual()){
            for(ConfigurationNode colorNode : node.getNode("palette").getChildrenList()){
                ParticleEffect.Builder builder = ParticleEffect.builder();
                builder.type(pPT.get());
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
            this.particle = builder.build();
        }

        this.animationCode = node.getNode("animationCode").getString("x=Math.sin(time/5)*0.7;z=Math.cos(time/5)*0.7;");
        this.animateColor = node.getNode("animateColor").getBoolean(false);
        try {
            this.compiled = ((Compilable) HuskyCrates.jsengine).compile("(function(time){var x = 0.0; var y = 0.0; var z = 0.0; var r = 0; var g = 0; var b = 0; " + animationCode + "; var result = {x:x,y:y,z:z,r:Math.round(r),g:Math.round(g),b:Math.round(b)}; return result;})(time);");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void run(long tick, Location<World> location){
        try {

            SimpleScriptContext sc = new SimpleScriptContext();
            sc.setBindings(HuskyCrates.jsengine.createBindings(), ScriptContext.GLOBAL_SCOPE);
            Bindings bindings = sc.getBindings(ScriptContext.GLOBAL_SCOPE);
            bindings.put("time",tick);
            sc.setBindings(bindings,ScriptContext.GLOBAL_SCOPE);
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) compiled.eval(sc);

            double x = (double) scriptObjectMirror.get("x");
            double y = (double) scriptObjectMirror.get("y");
            double z = (double) scriptObjectMirror.get("z");

            int r = Math.max(0,Math.min(255,((Double) scriptObjectMirror.get("r")).intValue()));
            int g = Math.max(0,Math.min(255,((Double) scriptObjectMirror.get("g")).intValue()));
            int b = Math.max(0,Math.min(255,((Double) scriptObjectMirror.get("b")).intValue()));

            System.out.println(r + "," + g + "," + b);

            ParticleEffect animatedParticle = null;

            if(this.animateColor) {
                animatedParticle = ParticleEffect.builder()
                        .from(particle)
                        .option(ParticleOptions.COLOR,Color.ofRgb(r,g,b))
                        .build();
            }

            location.getExtent().spawnParticles((this.animateColor)?animatedParticle:particle,location.getPosition().clone().add(0.5,0.5,0.5).add(x,y,z));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
