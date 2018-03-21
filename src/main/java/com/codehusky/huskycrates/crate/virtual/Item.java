package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.exception.ConfigParseError;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Items are basically just ItemStacks, except they haven't been constructed yet. These
 * @see org.spongepowered.api.item.inventory.ItemStack
 */
public class Item {
    private String name;
    private ItemType itemType;
    private List<String> lore;
    private Integer count;
    private Integer damage; // or metadata or meta
    private Integer durability; // amount of durability left on tool, weapon, armor, etc.

    private List<Enchantment> enchantments;
    private LinkedHashMap nbt;

    public Item(String name, ItemType itemType, List<String> lore, Integer count, Integer damage, Integer durability, List<Enchantment> enchantments, LinkedHashMap nbt){
        this.name = name;
        this.itemType = itemType;
        this.lore = lore;
        this.count = count;
        this.damage = damage;
        this.durability = durability;
        this.enchantments = enchantments;
        this.nbt = nbt;
    }

    public Item(ConfigurationNode node){
        if(!node.getNode("id").isVirtual()){
            try {
                this.itemType = node.getNode("id").getValue(TypeToken.of(ItemType.class));
            } catch (ObjectMappingException e) {
                throw new ConfigParseError("Supplied Item ID is not valid!",node.getNode("id").getPath());
            }
        }else{
            throw new ConfigParseError("Item ID was not specified in an item!",node.getNode("id").getPath());
        }

        this.name = node.getNode("name").getString();
        this.count = node.getNode("count").getInt(1);
        this.damage = node.getNode("damage").getInt();
        this.durability = node.getNode("durability").getInt();

        if(!node.getNode("lore").isVirtual()){
            try {
                this.lore = node.getNode("lore").getList(TypeToken.of(String.class));
            } catch (ObjectMappingException e) {
                e.printStackTrace();
                throw new ConfigParseError("Invalid lore virtual specified! Reason is printed above.",node.getNode("lore").getPath());
            }
        }

        if(!node.getNode("enchantments").isVirtual()){
            node.getNode("enchantments").getChildrenMap().forEach((enchantID, level) -> {
                if(!Sponge.getRegistry().getType(EnchantmentType.class,enchantID.toString()).isPresent()){
                    throw new ConfigParseError("Invalid Enchantment specified!",node.getNode("enchantments",enchantID).getPath());
                }
                this.enchantments.add(Enchantment.of(Sponge.getRegistry().getType(EnchantmentType.class,enchantID.toString()).get(),level.getInt(0)));
            });
        }

        if(!node.getNode("nbt").isVirtual() && node.getNode("nbt").getValue() instanceof LinkedHashMap) {
            this.nbt = (LinkedHashMap) node.getNode("nbt").getValue();
        }
    }

    public ItemStack toItemStack(){
        ItemStack stack = ItemStack.of(this.itemType, this.count);

        if(this.name != null){
            stack.offer(Keys.DISPLAY_NAME,TextSerializers.FORMATTING_CODE.deserialize(this.name));
        }

        if(this.durability != null){
            stack.offer(Keys.ITEM_DURABILITY,this.durability);
        }
        if(this.lore != null){
            ArrayList<Text> realLore = new ArrayList<>();
            for(String line : this.lore){
                realLore.add(TextSerializers.FORMATTING_CODE.deserialize(line));
            }
            stack.offer(Keys.ITEM_LORE,realLore);
        }
        if(this.enchantments != null){
            stack.offer(Keys.ITEM_ENCHANTMENTS,enchantments);
        }
        if(this.nbt != null){
            DataContainer container = stack.toContainer();
            if(container.get(DataQuery.of("UnsafeData")).isPresent()) {
                LinkedHashMap<Object,Object> real = ((LinkedHashMap) container.getMap(DataQuery.of("UnsafeData")).get());
                this.nbt.putAll(real);
            }
            container.set(DataQuery.of("UnsafeData"),this.nbt);
            stack = ItemStack.builder()
                    .fromContainer(
                            container
                    ).build();
        }
        if(this.damage != null){
            stack = ItemStack.builder()
                        .fromContainer(
                            stack.toContainer().set(DataQuery.of("UnsafeDamage"),this.damage)
                        ).build();
        }

        return stack;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getDamage() {
        return damage;
    }

    public Integer getDurability() {
        return durability;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public LinkedHashMap getNBT() {
        return nbt;
    }

    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getName() {
        return name;
    }
}
