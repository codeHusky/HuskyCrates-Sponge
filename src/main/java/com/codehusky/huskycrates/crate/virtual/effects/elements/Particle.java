package com.codehusky.huskycrates.crate.virtual.effects.elements;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.effect.particle.ParticleEffect;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Particle {
    private ParticleEffect particle;

    public Particle(ConfigurationNode node){
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        String userInput = "40+2";
        try {
            System.out.println(engine.eval("var time; var x = 0; var y = 0; " + userInput + "; return [x,y];"));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
