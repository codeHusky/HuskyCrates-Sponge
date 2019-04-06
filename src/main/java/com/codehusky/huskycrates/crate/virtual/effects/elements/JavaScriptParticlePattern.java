package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.codehusky.huskycrates.HuskyCrates;
import com.flowpowered.math.vector.Vector3d;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.util.Color;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class JavaScriptParticlePattern implements ParticlePattern {

    private final @Nonnull CompiledScript compiled;
    private final boolean ignoreColor;

    public JavaScriptParticlePattern(@Nonnull String animationCode) throws ScriptException {
        this(animationCode, false);
    }

    public JavaScriptParticlePattern(@Nonnull String animationCode, boolean ignoreColor) throws ScriptException {
        this.compiled = ((Compilable) HuskyCrates.jsengine).compile("function HSVtoRGB(h,s,v){var r,g,b,i,f,p,q,t;if(arguments.length===1){s=h.s,v=h.v,h=h.h}i=Math.floor(h*6);f=h*6-i;p=v*(1-s);q=v*(1-f*s);t=v*(1-(1-f)*s);switch(i%6){case 0:r=v,g=t,b=p;break;case 1:r=q,g=v,b=p;break;case 2:r=p,g=v,b=t;break;case 3:r=p,g=q,b=v;break;case 4:r=t,g=p,b=v;break;case 5:r=v,g=p,b=q;break}return{r:Math.round(r*255),g:Math.round(g*255),b:Math.round(b*255)}} (function(time, num){var x = 0.0; var y = 0.0; var z = 0.0; var h; var s; var v; var r = 0; var g = 0; var b = 0; " + animationCode + "; if(h&&s&&v){var hsv = HSVtoRGB(h/255,s/255,v/255); r=hsv.r;g=hsv.g;b=hsv.b;} var result = {x:x,y:y,z:z,r:Math.round(r),g:Math.round(g),b:Math.round(b)}; return result;})(time, num);");
        this.ignoreColor = ignoreColor;
    }

    @Override
    @Nonnull
    public Pair<Vector3d, Optional<Color>> getPositionForFrame(long tick, int particleNum) {
        try {
            SimpleScriptContext sc = new SimpleScriptContext();
            sc.setBindings(HuskyCrates.jsengine.createBindings(), ScriptContext.GLOBAL_SCOPE);
            Bindings bindings = sc.getBindings(ScriptContext.GLOBAL_SCOPE);
            bindings.put("time", tick);
            bindings.put("num", particleNum + 1);
            sc.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) compiled.eval(sc);

            double x = Double.valueOf("" + scriptObjectMirror.get("x"));
            double y = Double.valueOf("" + scriptObjectMirror.get("y"));
            double z = Double.valueOf("" + scriptObjectMirror.get("z"));
            Color color;
            if (this.ignoreColor) {
                color = null;
            } else {
                int r = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("r")).intValue()));
                int g = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("g")).intValue()));
                int b = Math.max(0, Math.min(255, ((Double) scriptObjectMirror.get("b")).intValue()));
                color = Color.ofRgb(r, g, b);
            }
            return Pair.of(new Vector3d(x, y, z), Optional.ofNullable(color));
        } catch (ScriptException ex) {
            ex.printStackTrace();
            return Pair.of(new Vector3d(0, 0, 0), Optional.empty());
        }
    }
}
