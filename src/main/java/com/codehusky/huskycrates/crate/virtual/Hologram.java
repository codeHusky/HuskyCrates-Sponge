package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.exception.ConfigParseError;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.List;

public class Hologram {
    private List<String> lines;
    private double yOffset;
    private double entityYOffset;

    public Hologram(ConfigurationNode node){
        try {
            this.lines = node.getNode("lines").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            throw new ConfigParseError("Hologram lines must be strings!",node.getNode("lines").getPath());
        }

        this.yOffset = node.getNode("yOffset").getDouble(0.2);
        this.entityYOffset = node.getNode("entityYOffset").getDouble(this.yOffset);
    }

    public List<String> getLines() {
        return lines;
    }

    public double getYOffset() {
        return yOffset;
    }

    public double getEntityYOffset() {
        return entityYOffset;
    }
}
