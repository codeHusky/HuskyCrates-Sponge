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
    private int amount;

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
        if(node.getNode("animationPreset").isVirtual()) {
            this.animationCode = node.getNode("animationCode").getString("x=Math.sin(time/5)*0.7;z=Math.cos(time/5)*0.7;");
            this.animateColor = node.getNode("animateColor").getBoolean(false);
        }else{
            switch (node.getNode("animationPreset").getString("").toLowerCase()){
                case "orbit":
                    this.animationCode = "x=Math.sin(time/5)*0.7; y=Math.sin(time/3)*0.2; z=Math.cos(time/5)*0.7;";
                    break;
                default:
                    throw new ConfigParseError("Invalid animation preset specified!",node.getNode("animationPreset").getPath());
            }
            switch (node.getNode("colorAnimationPreset").getString("").toLowerCase()){
                case "rainbow":
                    this.animateColor = true;
                    this.animationCode += "; h=((time*5)%255) + 1; s=255; v=255;";
                    break;
                default:
                    this.animateColor=false;
                    break;
            }
        }
        this.amount = node.getNode("amount").getInt(1);

        try {
            this.compiled = ((Compilable) HuskyCrates.jsengine).compile("function HSVtoRGB(h,s,v){var r,g,b,i,f,p,q,t;if(arguments.length===1){s=h.s,v=h.v,h=h.h}i=Math.floor(h*6);f=h*6-i;p=v*(1-s);q=v*(1-f*s);t=v*(1-(1-f)*s);switch(i%6){case 0:r=v,g=t,b=p;break;case 1:r=q,g=v,b=p;break;case 2:r=p,g=v,b=t;break;case 3:r=p,g=q,b=v;break;case 4:r=t,g=p,b=v;break;case 5:r=v,g=p,b=q;break}return{r:Math.round(r*255),g:Math.round(g*255),b:Math.round(b*255)}} (function(time, num){var x = 0.0; var y = 0.0; var z = 0.0; var h; var s; var v; var r = 0; var g = 0; var b = 0; " + animationCode + "; if(h&&s&&v){var hsv = HSVtoRGB(h/255,s/255,v/255); r=hsv.r;g=hsv.g;b=hsv.b;} var result = {x:x,y:y,z:z,r:Math.round(r),g:Math.round(g),b:Math.round(b)}; return result;})(time, num);");
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void run(long tick, Location<World> location){
        try {
            for(int i = 0; i < amount; i++) {
                SimpleScriptContext sc = new SimpleScriptContext();
                sc.setBindings(HuskyCrates.jsengine.createBindings(), ScriptContext.GLOBAL_SCOPE);
                Bindings bindings = sc.getBindings(ScriptContext.GLOBAL_SCOPE);
                bindings.put("time", tick);
                bindings.put("num", i+1);
                sc.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) compiled.eval(sc);

                double x = (double) scriptObjectMirror.get("x");
                double y = (double) scriptObjectMirror.get("y");
                double z = (double) scriptObjectMirror.get("z");

                int r = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("r")).intValue()));
                int g = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("g")).intValue()));
                int b = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("b")).intValue()));

                ParticleEffect animatedParticle = null;

                if (this.animateColor) {
                    animatedParticle = ParticleEffect.builder()
                            .from(particle)
                            .option(ParticleOptions.COLOR, Color.ofRgb(r, g, b))
                            .build();
                }

                location.getExtent().spawnParticles((this.animateColor) ? animatedParticle : particle, location.getPosition().clone().add(0.5, 0.5, 0.5).add(x, y, z));
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
