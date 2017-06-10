package pw.codehusky.huskycrates.crate.config;

import com.google.common.collect.BiMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.lang.SharedLangData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

public class CrateRewardHolderParser {
    public static CrateRewardHolder fromConfig(ConfigurationNode holderNode, VirtualCrate vc){
        if(holderNode.getNode("huskydata").isVirtual()){
            HuskyCrates.instance.logger.error("CANNOT FIND HUSKYDATA: " + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey()+  ") || " + holderNode.getParent().getParent().getKey());
            return null;
        }
        ItemStack dispItem = itemFromNode(holderNode);
        CrateReward reward = new CrateReward(null,"CODE ERROR, CONTACT DEVELOPER",false);
        boolean dispAwardSimilar = false;
        String name;
        boolean single = false;
        SharedLangData langData = HuskyCrates.instance.langData;
        if(!holderNode.getNode("huskydata","lang").isVirtual()){
            langData = new SharedLangData(vc.langData,holderNode.getNode("huskydata","lang"));
        }
        //System.out.println(dispItem.get(Keys.DISPLAY_NAME));
        if(holderNode.getNode("huskydata","reward").getNode("overrideRewardName").isVirtual()){
            //System.out.println("Virtual");
            if(dispItem.get(Keys.DISPLAY_NAME).isPresent()){
                name = dispItem.get(Keys.DISPLAY_NAME).get().toPlain();
            }else{
                name = dispItem.getItem().getName();
            }
        }else{
            //System.out.println("Real");
            name = holderNode.getNode("huskydata","reward").getNode("overrideRewardName").getString("strings, please.");
        }
        if(!holderNode.getNode("huskydata","reward").getNode("treatAsSingle").isVirtual()){
            single = holderNode.getNode("huskydata","reward").getNode("treatAsSingle").getBoolean();
        }
        if(holderNode.getNode("huskydata","reward","type").getString().equalsIgnoreCase("item")) {
            ItemStack rewardItem;
            if (holderNode.getNode("huskydata","reward").getNode("overrideItem").isVirtual()) {
                ItemStack rr = dispItem.copy();
                if(!holderNode.getNode("huskydata","reward").getNode("overrideCount").isVirtual()){

                    rr.setQuantity(holderNode.getNode("huskydata","reward").getNode("overrideCount").getInt());

                }
                reward=new CrateReward(rr,name,single);
                dispAwardSimilar = true;
            }else{
                reward=new CrateReward(itemFromNode(holderNode.getNode("huskydata","reward","overrideItem")),name,single);
            }
        }else if(holderNode.getNode("huskydata","reward","type").getString().equalsIgnoreCase("command")){
            reward = new CrateReward(holderNode.getNode("huskydata","reward","command").getString("/say You didn't set a command or something..."),name,single);
        }else{
            HuskyCrates.instance.logger.error("CHECK REWARD TYPE: " + holderNode.getNode("huskydata","reward","type").getString() +"@" + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey() + ") || "+ holderNode.getParent().getParent().getKey());
        }

        return new CrateRewardHolder(dispItem,reward,holderNode.getNode("huskydata","weight").getDouble(1),dispAwardSimilar,langData);
    }
    private static ItemStack itemFromNode(ConfigurationNode itemRoot){
        try {

            if(itemRoot.getNode("id").isVirtual() ){
                HuskyCrates.instance.logger.error("NO ITEM ID: " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                return ItemStack.of(ItemTypes.NONE,1);
            }
            ItemType type;
            try {
                type = itemRoot.getNode("id").getValue(TypeToken.of(ItemType.class));
            }catch(Exception e){
                HuskyCrates.instance.logger.error("INVALID ITEM ID: \"" + itemRoot.getNode("id").getString("NOT A STRING") + "\" || "+ itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                return ItemStack.of(ItemTypes.NONE,1);
            }
            ItemStack item = ItemStack.builder()
                    .itemType(type)
                    .quantity(itemRoot.getNode("count").getInt(1))
                    //.add())
                    .build();

            if(!itemRoot.getNode("name").isVirtual()){
                item.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(itemRoot.getNode("name").getString()));
            }
            if(!itemRoot.getNode("variant").isVirtual()) {
                //if(Sponge.getRegistry().getType(TreeType.class,itemRoot.getNode("variant").getString()).isPresent()) {
                //System.out.println(item.offer(Keys.TREE_TYPE,getTreeType(itemRoot.getNode("variant").getString("oak"))));
                //System.out.println(itemRoot.getNode("variant").getValue());
                //}
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
                ArrayList<ItemEnchantment> enchantments = new ArrayList<>();
                for (Object key : itemRoot.getNode("enchants").getChildrenMap().keySet()) {
                    int level = itemRoot.getNode("enchants").getChildrenMap().get(key).getInt();
                    String enchantID = (String) key;
                    Optional<Enchantment> pEnchant = Sponge.getRegistry().<Enchantment>getType(Enchantment.class,enchantID);
                    if(!pEnchant.isPresent()){
                        HuskyCrates.instance.logger.error("INVALID ENCHANT ID: \"" + key + "\" || "+ itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                        return ItemStack.of(ItemTypes.NONE,1);
                    }
                    Enchantment enc = pEnchant.get(); // STRINGS ONLY!
                    ItemEnchantment itemEnchantment = new ItemEnchantment(enc, level);
                    enchantments.add(itemEnchantment);
                }
                item.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
            }
            if(!itemRoot.getNode("damage").isVirtual()){
                //HuskyCrates.instance.logger.info("damage override called");
                item = ItemStack.builder()
                        .fromContainer(item.toContainer().set(DataQuery.of("UnsafeDamage"),itemRoot.getNode("damage").getInt(0))) //OVERRIDE DAMAGE VAL! :)
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
