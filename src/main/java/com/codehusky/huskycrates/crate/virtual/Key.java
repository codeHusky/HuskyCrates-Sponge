package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

/**
 * Keys are objects that contain an identifier (for the key) and the display item for such key, if that applies.
 * Keys can also be virtual, meaning they have no real-world item, allowing for quest-like features.
 */
public class Key {
    private String id;
    private String name;
    private Boolean isVirtual;
    private Item displayItem;

    public Key(ConfigurationNode node){
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString(id);
        this.isVirtual = node.getNode("isVirtual").getBoolean(false);
        if(!this.isVirtual){
            this.displayItem = new Item(node.getNode("displayItem"));
        }
    }

    public Key(String id){
        this.id = id;
        this.isVirtual = true;
    }

    public Key(String id, Item displayItem){
        this.id = id;
        this.displayItem = displayItem;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Item getDisplayItem() {
        return displayItem;
    }

    public ItemStack getKeyItemStack() {
        return this.getKeyItemStack(1);
    }
    public ItemStack getKeyItemStack(int amount) {
        DataContainer container = displayItem.toItemStack()
                .toContainer()
                .set(DataQuery.of("UnsafeData","HCKEYID"),this.id);
        if(HuskyCrates.KEY_SECURITY){
            container = container.set(DataQuery.of("UnsafeData","HCKEYUUID"),HuskyCrates.registry.generateSecureKey(id,amount).toString());
        }
        return ItemStack.builder()
                .fromContainer(container)
                .quantity(amount)
                .build();
    }

    public static String extractKeyId(ItemStack stack){
        try {
            return stack.toContainer().get(DataQuery.of("UnsafeData", "HCKEYID")).get().toString();
        }catch (Exception e){
            return null;
        }
    }

    public static UUID extractKeyUUID(ItemStack stack){
        if(HuskyCrates.KEY_SECURITY) {
            Optional<Object> potentialUUID = stack.toContainer().get(DataQuery.of("UnsafeData", "HCKEYUUID"));
            if(potentialUUID.isPresent()) {
                try {
                    return UUID.fromString(potentialUUID.get().toString());
                }catch (Exception e){
                    System.out.println(potentialUUID.get());
                    return null;
                }
            }
        }
        return null;
    }

    public boolean testKey(ItemStack stack){
        UUID keyUUID = extractKeyUUID(stack);
        return this.id.equals(extractKeyId(stack)) &&
                stack.getType().equals(displayItem.getItemType()) &&
                (!HuskyCrates.KEY_SECURITY ||
                        HuskyCrates.KEY_SECURITY &&
                                keyUUID != null &&
                                HuskyCrates.registry.isSecureKey(id,keyUUID)
                );
    }


}
