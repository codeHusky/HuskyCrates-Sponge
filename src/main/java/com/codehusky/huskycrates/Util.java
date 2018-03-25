package com.codehusky.huskycrates;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

public class Util {
    /**
     * Garbage method I have to do since API7 decided to eat the inventory api.
     * @param inventory
     * @return an inventory that i actually want
     */
    public static Inventory getHotbarFirst(Inventory inventory) {
        return inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .union(inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));
    }
}
