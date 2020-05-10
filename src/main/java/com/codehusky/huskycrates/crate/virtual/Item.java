package com.codehusky.huskycrates.crate.virtual;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.exception.ConfigParseError;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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

import java.util.*;

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
    private Boolean unbreakable; // never loss durability
    private Boolean hideAttributes; //Hide attributes like damage (sword example)
    private Boolean hideEnchantments; //Hide enchantments, this is for create effect enchant without showing the enchantment
    private Boolean hideUnbreakable; //Hide unbreakable value
    //Mainly put for rol/mmorpg servers
    private Boolean hideCanDestroy; //For hand items. Example -> Pickaxe only can break stone.
    private Boolean hideCanPlace; //For hand items. Example -> ladder only can place into iron_blocks
    private Boolean hideMiscellaneous; //JD sponge is down. unknown

    private List<Enchantment> enchantments;
    private Map nbt;

    public static Item fromItemStack(ItemStack stack){
        String name = TextSerializers.FORMATTING_CODE.serialize(stack.get(Keys.DISPLAY_NAME).orElse(Text.EMPTY));
        if(name.length() == 0) name = null;
        List<String> lore = new ArrayList<>();
        if(stack.get(Keys.ITEM_LORE).isPresent()){
            stack.get(Keys.ITEM_LORE).get().forEach(text -> {
                lore.add(TextSerializers.FORMATTING_CODE.serialize(text));
            });
        }
        return new Item(
                name,
                stack.getType(),
                lore,
                stack.getQuantity(),
                stack.toContainer().getInt(DataQuery.of("UnsafeDamage")).orElse(null),
                stack.get(Keys.ITEM_DURABILITY).orElse(null),
                stack.get(Keys.ITEM_ENCHANTMENTS).orElse(null),
                stack.get(Keys.UNBREAKABLE).orElse(null),
                stack.get(Keys.HIDE_ATTRIBUTES).orElse(null),
                stack.get(Keys.HIDE_CAN_DESTROY).orElse(null),
                stack.get(Keys.HIDE_CAN_PLACE).orElse(null),
                stack.get(Keys.HIDE_ENCHANTMENTS).orElse(null),
                stack.get(Keys.HIDE_MISCELLANEOUS).orElse(null),
                stack.get(Keys.HIDE_UNBREAKABLE).orElse(null),
                stack.toContainer().getMap(DataQuery.of("UnsafeData")).orElse(null)
                );
    }
    //TODO: builder pattern
    public Item(String name, ItemType itemType, List<String> lore, Integer count, Integer damage, Integer durability, List<Enchantment> enchantments, Boolean unbreakable,Boolean hideAttributes, Boolean hideCanDestroy, Boolean hideCanPlace, Boolean hideEnchantments, Boolean hideMiscellaneous, Boolean hideUnbreakable, Map nbt){
        this.name = name;
        if(name != null && name.length() == 0) this.name = null;
        this.itemType = itemType;
        this.lore = lore;
        this.count = count;
        this.damage = damage;
        this.durability = durability;
        this.enchantments = enchantments;
        this.unbreakable = unbreakable;
        this.hideAttributes= hideAttributes;
        this.hideCanDestroy=hideCanDestroy;
        this.hideCanPlace=hideCanPlace;
        this.hideEnchantments=hideEnchantments;
        this.hideMiscellaneous=hideMiscellaneous;
        this.hideUnbreakable=hideUnbreakable;
        this.nbt = nbt;
        if(this.nbt instanceof ImmutableMap){
            this.nbt = Maps.newHashMap(this.nbt);
        }
    }

    public Item(ConfigurationNode node){
        if(!node.getNode("id").isVirtual()){
            try {
                this.itemType = node.getNode("id").getValue(TypeToken.of(ItemType.class));
            } catch (ObjectMappingException e) {
                HuskyCrates.instance.logger.error("Printing out ItemID ObjectMappingException below!");
                e.printStackTrace();
                throw new ConfigParseError("Supplied Item ID is not valid!",node.getNode("id").getPath());
            }
        }else{
            throw new ConfigParseError("Item ID was not specified in an item!",node.getNode("id").getPath());
        }

        this.name = node.getNode("name").getString();
        if(this.name != null && this.name.length() == 0) this.name = null;
        this.count = node.getNode("count").getInt(1);
        this.damage = node.getNode("damage").getInt(node.getNode("meta").getInt(node.getNode("metadata").getInt()));
        this.durability = node.getNode("durability").getInt();
        this.unbreakable=node.getNode("unbreakable").getBoolean();
        this.hideAttributes=node.getNode("hide_attributes").getBoolean();
        this.hideCanDestroy=node.getNode("hide_can_destroy").getBoolean();
        this.hideCanPlace=node.getNode("hide_can_place").getBoolean();
        this.hideEnchantments=node.getNode("hide_enchantments").getBoolean();
        this.hideMiscellaneous=node.getNode("hide_miscellaneous").getBoolean();
        this.hideUnbreakable=node.getNode("hide_unbreakable").getBoolean();

        if(!node.getNode("lore").isVirtual()){
            try {
                this.lore = node.getNode("lore").getList(TypeToken.of(String.class));
            } catch (ObjectMappingException e) {
                e.printStackTrace();
                throw new ConfigParseError("Invalid lore virtual specified! Reason is printed above.",node.getNode("lore").getPath());
            }
        }
        this.enchantments = new ArrayList<>();
        if(!node.getNode("enchantments").isVirtual()){
            node.getNode("enchantments").getChildrenMap().forEach((enchantID, level) -> {
                Optional<EnchantmentType> enchantType = Sponge.getRegistry().getType(EnchantmentType.class,enchantID.toString());
                if(!enchantType.isPresent()){
                    throw new ConfigParseError("Invalid Enchantment specified!",node.getNode("enchantments",enchantID).getPath());
                }
                if(!(level.getValue() instanceof Integer)){
                    throw new ConfigParseError("Invalid Type for Enchantment Level!",node.getNode("enchantments",enchantID).getPath());
                }

                this.enchantments.add(Enchantment.builder()
                        .type(enchantType.get())
                        .level((Integer)level.getValue())
                        .build());
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
            if(this.hideEnchantments != null){
                stack.offer(Keys.HIDE_ENCHANTMENTS,hideEnchantments);
            }
        }
        if(this.unbreakable != null){
            stack.offer(Keys.UNBREAKABLE,false);
            if(this.hideUnbreakable != null){
                stack.offer(Keys.HIDE_UNBREAKABLE,hideUnbreakable);
            }
        }
        if(this.hideAttributes != null){
            stack.offer(Keys.HIDE_ATTRIBUTES,hideUnbreakable);
        }
        if(this.hideMiscellaneous != null){
            stack.offer(Keys.HIDE_MISCELLANEOUS,hideMiscellaneous);
        }
        if(this.hideCanDestroy != null){
            stack.offer(Keys.HIDE_CAN_DESTROY,hideCanDestroy);
        }
        if(this.hideCanPlace != null){
            stack.offer(Keys.HIDE_CAN_PLACE,hideCanPlace);
        }
        if(this.nbt != null){
            DataContainer container = stack.toContainer();
            if(container.get(DataQuery.of("UnsafeData")).isPresent()) {
                Map real = (container.getMap(DataQuery.of("UnsafeData")).get());
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

    public Map getNBT() {
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
