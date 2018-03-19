package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.crate.effects.ActionEffects;
import com.codehusky.huskycrates.crate.effects.IdleEffects;
import org.spongepowered.api.block.BlockType;

import java.util.List;

public class Crate {
    private String identifier;
    private Hologram hologram;
    private String name;
    private IdleEffects idleEffects;
    private ActionEffects actionEffects;
    private List<Item> items;

    private BlockType defaultBlock;
}
