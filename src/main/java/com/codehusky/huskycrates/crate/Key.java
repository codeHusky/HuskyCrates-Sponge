package com.codehusky.huskycrates.crate;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Keys are objects that contain an identifier (for the key) and the display item for such key, if that applies.
 * Keys can also be virtual, meaning they have no real-world item, allowing for quest-like features.
 */
public class Key {
    private String id;
    private Boolean isVirtual;
    private Item displayItem;

    public Key(ConfigurationNode node){
        this.id = node.getKey().toString();
        this.isVirtual = node.getNode("isVirtual").getBoolean(false);
        if(!this.isVirtual){
            this.displayItem = new Item(node.getNode("displayItem"));
        }
    }

    public String getId() {
        return id;
    }

    public Item getDisplayItem() {
        return displayItem;
    }

    public ItemStack getKeyItemStack() {
        return ItemStack.builder()
                .fromContainer(
                        displayItem.toItemStack()
                                .toContainer()
                                .set(DataQuery.of("UnsafeData","HCKEYID"),this.id)
                ).build();
    }


}
