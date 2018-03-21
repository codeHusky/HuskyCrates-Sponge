package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.effects.ActionEffects;
import com.codehusky.huskycrates.crate.virtual.effects.IdleEffects;
import com.codehusky.huskycrates.exceptions.ConfigParseError;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Crate {
    private String id;
    private String name;

    private Hologram hologram;

    private IdleEffects idleEffects;
    private ActionEffects actionEffects;

    private List<Slot> slots;

    private Boolean scrambleSlots;

    private BlockType defaultBlock;

    private Boolean useLocalKey;
    private Key localKey;

    private HashMap<String, Integer> acceptedKeys;

    public Crate(ConfigurationNode node){
        slots = new ArrayList<>();
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString();

        if(node.getNode("slots").isVirtual()){
            throw new ConfigParseError("Crates must have associated slots!", node.getNode("slots").getPath());
        }else{
            for(ConfigurationNode slot : node.getNode("slots").getChildrenList()){
                Slot thisSlot = new Slot(slot);
                slots.add(thisSlot);
            }
        }

        this.scrambleSlots = node.getNode("scrambleSlots").getBoolean(false);

        this.useLocalKey = node.getNode("useLocalKey").getBoolean(false);

        ConfigurationNode aKeyNode = node.getNode("acceptedKeys");
        if(!aKeyNode.isVirtual()) {
            if (aKeyNode.hasListChildren()) {

            } else if (aKeyNode.hasMapChildren()) {

            } else {
                throw new ConfigParseError("Invalid key format specified. Odd.",aKeyNode.getPath());
            }
        }

        if(this.useLocalKey){
            if(node.getNode("localKey").isVirtual()){
                this.localKey = new Key("LOCALKEY_" + this.id, new Item(node.getNode("localKey")));
            }else{
                this.localKey = new Key("LOCALKEY_" + this.id, new Item("&8" + this.name + " Key", ItemTypes.NETHER_STAR, null, 1, null, null, null, null));
            }
        }else if(aKeyNode.isVirtual()){
            throw new ConfigParseError("Crate has no accepted keys!",node.getPath());
        }
    }

    public int selectSlot

    public boolean testKey(ItemStack stack){
        if(useLocalKey){
            if(localKey.testKey(stack)) return true;
        }
        for(String thisid : acceptedKeys.keySet()){
            Key potential = HuskyCrates.registry.getKey(thisid);
            if(potential.testKey(stack)) return true;
        }
        return false;
    }

    public boolean hasLocalKey(){
        return useLocalKey;
    }

    public Key getLocalKey(){
        return localKey;
    }
}
