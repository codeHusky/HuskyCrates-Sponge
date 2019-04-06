package com.codehusky.huskycrates.crate.virtual.effects.elements;

import com.flowpowered.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.util.Color;

import java.util.Optional;

import javax.annotation.Nonnull;

public interface ParticlePattern {

    @Nonnull Pair<Vector3d, Optional<Color>> getPositionForFrame(long tick, int particleNum);

}
