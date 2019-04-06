package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.flowpowered.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.util.Color;

import java.util.Optional;

import javax.annotation.Nonnull;

public class PresetParticlePattern implements ParticlePattern {

    private final @Nonnull ParticlePreset particlePreset;
    private final ColorPreset colorPreset;

    public PresetParticlePattern(@Nonnull ParticlePreset particlePreset, ColorPreset colorPreset) {
        this.particlePreset = particlePreset;
        this.colorPreset = colorPreset;
    }

    @Override
    public Pair<Vector3d, Optional<Color>> getPositionForFrame(long tick, int particleNum) {
        Vector3d position = null;
        switch (this.particlePreset) {
            case ORBIT:
                position = new Vector3d(Math.sin(tick / 4.) * .7,
                        Math.sin(tick / 4.) * .2,
                        Math.cos(tick / 4.) * .7);
                break;
            case COUNTERORBIT:
                position = new Vector3d(Math.sin(tick / 4.) * .7,
                        Math.sin(tick / 4. + 10) * .2 - .1,
                        Math.cos(tick / 4.) * .7);
        }
        Color color;
        if (this.colorPreset == ColorPreset.RAINBOW) {
            java.awt.Color clr = java.awt.Color.getHSBColor((((tick * 5.0f) % 255.0f) + 1.0f) / 255f, 1.0f, 1.0f);
            color = Color.of(clr);
        } else {
            color = null;
        }
        return Pair.of(position, Optional.ofNullable(color));
    }

    public enum ParticlePreset {
        ORBIT, COUNTERORBIT
    }

    public enum ColorPreset {
        RAINBOW
    }

}
