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
    private Boolean canLaunchCrate;
    private String crateIDToLaunch;
    private Item item;

    public Key(ConfigurationNode node){
        this.id = node.getKey().toString();
        this.name = node.getNode("name").getString(id);
        this.isVirtual = node.getNode("isVirtual").getBoolean(false);
        if(!this.isVirtual){
            this.item = new Item(node.getNode("item"));
        }
        this.canLaunchCrate = !isVirtual && node.getNode("canLaunchCrate").getBoolean(false);
        this.crateIDToLaunch = node.getNode("crateIDToLaunch").getString();
    }

    public Key(String id){
        this.id = id;
        this.isVirtual = true;
        this.canLaunchCrate = false;
    }

    public Key(String id, Item item, boolean canLaunchCrate){
        this.id = id;
        this.item = item;
        this.isVirtual = false;
        this.canLaunchCrate = canLaunchCrate;
        this.crateIDToLaunch = (this.canLaunchCrate)?id.replace("LOCALKEY_",""):null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        if(name == null){
            return item.getName();
        }
        return name;
    }

    public Boolean isVirtual() {
        return isVirtual;
    }

    public Boolean canLaunchCrate() {
        return canLaunchCrate;
    }

    public Crate crateToLaunch() {
        if(canLaunchCrate && HuskyCrates.registry.isCrate(crateIDToLaunch)){
            return HuskyCrates.registry.getCrate(crateIDToLaunch);
        }
        return null;
    }

    public Item getItem() {
        return item;
    }

    public ItemStack getKeyItemStack() {
        return this.getKeyItemStack(1);
    }
    public ItemStack getKeyItemStack(int amount) {
        if(isVirtual) return null;
        DataContainer container = item.toItemStack()
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
        if(isVirtual) return false;
        return this.id.equals(extractKeyId(stack)) &&
                stack.getType().equals(item.getItemType());
    }


}
