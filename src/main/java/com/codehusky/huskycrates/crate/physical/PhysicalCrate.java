package com.codehusky.huskycrates.crate.physical;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Stores metadata about a crate at a given location.
 */
public class PhysicalCrate {
    private Location<World> location;
    private String representedCrateID;
}
