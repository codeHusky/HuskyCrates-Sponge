package com.codehusky.huskyui.components.inventory;

import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * An Element is simply an ItemStack relating to a Page state.
 * Elements do nothing by themselves, acting like a static, unmovable object.
 */
public class Element {
    private ItemStack displayItem = ItemStack.of(ItemTypes.AIR,1);
    public void setDisplayItem(ItemStack item) {
        displayItem = item;
    }
    public ItemStack getDisplayItem() {
        return displayItem;
    }
}
