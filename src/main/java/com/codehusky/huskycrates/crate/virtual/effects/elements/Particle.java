package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.exception.ConfigParseError;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.script.*;
import java.util.Optional;

public class Particle {
    private ParticleEffect particle;
    private String animationCode;
    private CompiledScript compiled;

    public Particle(ConfigurationNode node){
        System.out.println(node.getNode("type").getString(""));
        System.out.println(ParticleTypes.REDSTONE_DUST.getId());
        Optional<ParticleType> pPT = Sponge.getRegistry().getType(ParticleType.class,node.getNode("type").getString(""));
        if(!pPT.isPresent()){
            throw new ConfigParseError("Invalid particle type!",node.getNode("type").getPath());
        }
        ParticleEffect.Builder builder = ParticleEffect.builder();
        builder.type(pPT.get());
        this.particle = builder.build();
        this.animationCode = node.getNode("animationCode").getString("x=Math.sin(time/5)*0.7;z=Math.cos(time/5)*0.7;");
        try {
            this.compiled = ((Compilable) HuskyCrates.jsengine).compile("(function(time){var x = 0.0; var y = 0.0; var z = 0.0; " + animationCode + "; var result = {x:x,y:y,z:z}; return result;})(time);");
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

            location.getExtent().spawnParticles(particle,location.getPosition().clone().add(0.5,0.5,0.5).add(x,y,z));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
