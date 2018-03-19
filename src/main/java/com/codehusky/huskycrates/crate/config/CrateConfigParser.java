package com.codehusky.huskycrates.crate.config;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.lang.LangData;
import com.google.common.collect.BiMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

public class CrateConfigParser {
    public static CrateReward fromConfig(ConfigurationNode holderNode, VirtualCrate vc){
        if(holderNode.getNode("huskydata").isVirtual()){
            HuskyCrates.instance.logger.error("CANNOT FIND HUSKYDATA: " + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey()+  ") || " + holderNode.getParent().getParent().getKey());
            return null;
        }

        ItemStack dispItem = itemFromNode(holderNode);
        String name;
        boolean single = false;
        boolean announce = false;
        LangData langData = vc.getLangData();
        if(!holderNode.getNode("huskydata","lang").isVirtual()){
            langData = new LangData(vc.getLangData(),holderNode.getNode("huskydata","lang"));
        }
        //System.out.println(dispItem.get(Keys.DISPLAY_NAME));
        if (!holderNode.getNode("huskydata", "reward", "announce").isVirtual()) {
            //announce = holderNode.getNode("huskydata", "reward", "announce").getBoolean(false);
            holderNode.getNode("huskydata", "announce").setValue(holderNode.getNode("huskydata", "reward", "announce").getValue());
            holderNode.getNode("huskydata", "reward", "announce").setValue(null);
        }
        if (!holderNode.getNode("huskydata", "reward", "treatAsSingle").isVirtual()) {
            //single = holderNode.getNode("huskydata", "reward", "treatAsSingle").getBoolean(false);
            holderNode.getNode("huskydata", "treatAsSingle").setValue(holderNode.getNode("huskydata", "reward", "treatAsSingle").getValue());
            holderNode.getNode("huskydata", "reward", "treatAsSingle").setValue(null);
        }
        if(!holderNode.getNode("huskydata","reward","overrideRewardName").isVirtual()) {
            String premove = holderNode.getNode("huskydata","reward","overrideRewardName").getString("failure?");
            holderNode.getNode("huskydata","reward","overrideRewardName").setValue(null);
            holderNode.getNode("huskydata","overrideRewardName").setValue(premove);
        }
        if(holderNode.getNode("huskydata","overrideRewardName").isVirtual()){
            //System.out.println("Virtual");
            if (dispItem.get(Keys.DISPLAY_NAME).isPresent()) {
                name = TextSerializers.FORMATTING_CODE.serialize(dispItem.get(Keys.DISPLAY_NAME).get());
            } else {
                name = dispItem.getType().getName();
            }
        }else{
            //System.out.println("Real");
            name = holderNode.getNode("huskydata","overrideRewardName").getString("strings, please.");
        }
        if (!holderNode.getNode("huskydata", "announce").isVirtual()) {
            announce = holderNode.getNode("huskydata", "announce").getBoolean(false);
        }
        if (!holderNode.getNode("huskydata", "treatAsSingle").isVirtual()) {
            single = holderNode.getNode("huskydata", "treatAsSingle").getBoolean(false);
        }
        if(!holderNode.getNode("huskydata","reward").isVirtual()){
            holderNode.getNode("huskydata","rewards").getAppendedNode().setValue(holderNode.getNode("huskydata","reward"));
            holderNode.getNode("huskydata","reward").setValue(null);
        }

        ArrayList<Object> rewards = new ArrayList<>();
        for(ConfigurationNode rewardNode : holderNode.getNode("huskydata","rewards").getChildrenList()) {
            if(rewardNode.getNode("type").isVirtual()){
                HuskyCrates.instance.logger.error("CANNOT FIND REWARD TYPE: " + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey()+  ") (reward #" + rewardNode.getKey()+  ") || " + holderNode.getParent().getParent().getKey());
                continue;
            }
            if (rewardNode.getNode("type").getString().equalsIgnoreCase("item")) {
                //ItemStack rewardItem;
                if (rewardNode.getNode("overrideItem").isVirtual()) {
                    ItemStack rr = dispItem.copy();
                    if (!rewardNode.getNode("overrideCount").isVirtual()) {

                        rr.setQuantity(rewardNode.getNode("overrideCount").getInt());

                    }
                    rewards.add(rr);
                } else {
                    rewards.add(itemFromNode(rewardNode.getNode("overrideItem")));
                }
            } else if (rewardNode.getNode("type").getString().equalsIgnoreCase("command")) {
                rewards.add(rewardNode.getNode("command").getString("/say You didn't set a command or something..."));
            } else {
                HuskyCrates.instance.logger.error("INVALID REWARD TYPE: " + rewardNode.getNode("type").getString() + "@" + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey() + ") || " + holderNode.getParent().getParent().getKey());
            }

        }
        if (!holderNode.getNode("huskydata","announce").isVirtual()) {
            announce = holderNode.getNode("huskydata","announce").getBoolean(false);
        }
        if(rewards.size() == 0){
            rewards.add("/say No rewards were loaded in the " + vc.id + " crate!");
        }
        return new CrateReward(dispItem,rewards,name,holderNode.getNode("huskydata","weight").getDouble(1),langData,announce,single);
    }
    private static ItemStack itemFromNode(ConfigurationNode itemRoot){
        try {

            if(itemRoot.getNode("id").isVirtual() ){
                HuskyCrates.instance.logger.error("NO ITEM ID: " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                return ItemStack.of(ItemTypes.NONE,1);
            }
            ItemType type;
            Integer dmg = null;
            try {
                type = itemRoot.getNode("id").getValue(TypeToken.of(ItemType.class));
            }catch(Exception e){
                String id = itemRoot.getNode("id").getString();
                String[] parts = id.split(":");
                try {
                    if (parts.length == 3) {
                        id = parts[0] + ":" + parts[1];
                        dmg = Integer.parseInt(parts[2]);
                    } else if (parts.length == 2) {
                        id = parts[0];
                        dmg = Integer.parseInt(parts[1]);
                    }
                }catch(Exception ee){
                    HuskyCrates.instance.logger.error("INVALID ITEM ID: \"" + itemRoot.getNode("id").getString("NOT A STRING") + "\" || " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey() + ") || " + itemRoot.getParent().getParent().getKey());
                    return ItemStack.of(ItemTypes.NONE, 1);
                }
                Optional<ItemType> optType = Sponge.getRegistry().getType(ItemType.class,id);
                if(optType.isPresent()) {
                    type = optType.get();
                }else {
                    HuskyCrates.instance.logger.error("INVALID ITEM ID: \"" + itemRoot.getNode("id").getString("NOT A STRING") + "\" || " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey() + ") || " + itemRoot.getParent().getParent().getKey());
                    return ItemStack.of(ItemTypes.NONE, 1);
                }
            }
            ItemStack item = ItemStack.builder()
                    .itemType(type)
                    .quantity(itemRoot.getNode("count").getInt(1))
                    //.add())
                    .build();

            if(!itemRoot.getNode("name").isVirtual()){
                item.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(itemRoot.getNode("name").getString()));
            }
            if(!itemRoot.getNode("lore").isVirtual()) {
                ArrayList<Text> lore = new ArrayList<>();
                for (String ll : itemRoot.getNode("lore").getList(TypeToken.of(String.class))) {
                    lore.add(TextSerializers.FORMATTING_CODE.deserialize(ll));
                }
                item.offer(Keys.ITEM_LORE, lore);
            }
            if(!itemRoot.getNode("name").isVirtual()) {
                item.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(itemRoot.getNode("name").getString()));
            }
            if(!itemRoot.getNode("enchants").isVirtual()) {
                ArrayList<Enchantment> enchantments = new ArrayList<>();
                for (Object key : itemRoot.getNode("enchants").getChildrenMap().keySet()) {
                    int level = itemRoot.getNode("enchants").getChildrenMap().get(key).getInt();
                    String enchantID = (String) key;
                    Optional<EnchantmentType> pEnchantType = Sponge.getRegistry().getType(EnchantmentType.class, enchantID);
                    if(!pEnchantType.isPresent()){
                        HuskyCrates.instance.logger.error("INVALID ENCHANT ID: \"" + key + "\" || "+ itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                        return ItemStack.of(ItemTypes.NONE,1);
                    }
                    Enchantment pEnchant = Enchantment.of(pEnchantType.get(), level);                                        
                    enchantments.add(pEnchant);
                }
                item.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
            }
            if(!itemRoot.getNode("damage").isVirtual()){
                //HuskyCrates.instance.logger.info("damage override called");
                item = ItemStack.builder()
                        .fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"),itemRoot.getNode("damage").getInt(0))) //OVERRIDE DAMAGE VAL! :)
                        .build();
            }else if(dmg != null){
                item = ItemStack.builder()
                        .fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"),dmg)) //OVERRIDE DAMAGE VAL! :)
                        .build();
            }

            if(!itemRoot.getNode("nbt").isVirtual()){
                //nbt overrrides
                LinkedHashMap items = (LinkedHashMap) itemRoot.getNode("nbt").getValue();
                if(item.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
                    BiMap real = ((BiMap) item.toContainer().getMap(DataQuery.of("UnsafeData")).get());
                    items.putAll(real);
                }
                //System.out.println(item.toContainer().get(DataQuery.of("UnsafeData")).get().getClass());
                item = ItemStack.builder()
                        .fromContainer(item.toContainer().set(DataQuery.of("UnsafeData"),items))
                        .build();
            }

            //item.offer(Keys.PICKUP_DELAY,itemRoot.getNode("pickupdelay").getInt())
            return item;
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*private static Enchantment getEnchantment(String id){
        switch(id){
            case "protection":
                return Enchantments.PROTECTION;
            case "fire_protection":
                return Enchantments.FIRE_PROTECTION;
            case "feather_falling":
                return Enchantments.FEATHER_FALLING;
            case "blast_protection":
                return Enchantments.BLAST_PROTECTION;
            case "projectile_protection":
                return Enchantments.PROJECTILE_PROTECTION;
            case "respiration":
                return Enchantments.RESPIRATION;
            case "aqua_affinity":
                return Enchantments.AQUA_AFFINITY;
            case "thorns":
                return Enchantments.THORNS;
            case "depth_strider":
                return Enchantments.DEPTH_STRIDER;
            case "frost_walker":
                return Enchantments.FROST_WALKER;
            case "binding_curse":
                return Enchantments.BINDING_CURSE;
            case "sharpness":
                return Enchantments.SHARPNESS;
            case "smite":
                return Enchantments.SMITE;
            case "bane_of_arthropods":
                return Enchantments.BANE_OF_ARTHROPODS;
            case "knockback":
                return Enchantments.KNOCKBACK;
            case "fire_aspect":
                return Enchantments.FIRE_ASPECT;
            case "looting":
                return Enchantments.LOOTING;
            case "sweeping":
                return Enchantments.SWEEPING;
            case "efficiency":
                return Enchantments.EFFICIENCY;
            case "silk_touch":
                return Enchantments.SILK_TOUCH;
            case "unbreaking":
                return Enchantments.UNBREAKING;
            case "fortune":
                return Enchantments.FORTUNE;
            case "power":
                return Enchantments.POWER;
            case "punch":
                return Enchantments.PUNCH;
            case "flame":
                return Enchantments.FLAME;
            case "infinity":
                return Enchantments.INFINITY;
            case "luck_of_the_sea":
                return Enchantments.LUCK_OF_THE_SEA;
            case "lure":
                return Enchantments.LURE;
            case "mending":
                return Enchantments.MENDING;
            case "vanishing_curse":
                return Enchantments.VANISHING_CURSE;
        }
        return null; //you done goof
    }
    private static Enchantment getEnchantment(int id){
        return null;
    }*/
}
