package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.crate.effects.ActionEffects;
import com.codehusky.huskycrates.crate.effects.IdleEffects;
import com.codehusky.huskycrates.exceptions.ConfigParseError;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;

import java.util.ArrayList;
import java.util.List;

public class Crate {
    private String id;
    private String name;

    private Hologram hologram;

    private IdleEffects idleEffects;
    private ActionEffects actionEffects;

    private List<Item> slots;

    private BlockType defaultBlock;

    private Boolean useLocalKey;
    private Key localKey;

    public Crate(ConfigurationNode node){
        slots = new ArrayList<>();
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString();

        if(node.getNode("slots").isVirtual()){
            throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
        }

        this.useLocalKey = node.getNode("useLocalKey")
    }
}
