package pw.codehusky.huskycrates.crate.config;

import com.google.common.collect.BiMap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.lang.SharedLangData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CrateRewardHolderParser {
    public static CrateRewardHolder fromConfig(ConfigurationNode holderNode, VirtualCrate vc){
        if(holderNode.getNode("id").isVirtual() || holderNode.getNode("huskydata").isVirtual()){
            HuskyCrates.instance.logger.error("CHECK ITEM: " + holderNode.getNode("name").getString("(no name)") + " (item #" + holderNode.getKey()+  ") || " + holderNode.getParent().getParent().getKey());
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
    public static ConfigurationNode toConfig(CrateRewardHolder holder){ //pretty much just for conversion
        ConfigurationNode toOverwrite = HoconConfigurationLoader.builder().build().createEmptyNode();
        toOverwrite.setValue(itemToNode(holder.getDisplayItem()));
        ConfigurationNode hd = toOverwrite.getNode("huskydata");
        if(holder.getReward().getReward() instanceof String){
            hd.getNode("reward","overrideRewardName").setValue(holder.getReward().getRewardName());
            hd.getNode("reward","command").setValue(holder.getReward().getReward());
            hd.getNode("reward","type").setValue("command");
        }else {
            if (holder.isDispRewardSimilar()) {
                if (holder.getDisplayItem().getQuantity() != ((ItemStack) holder.getReward().getReward()).getQuantity()) {
                    // we are overriding the count, but not the item! :)
                    hd.getNode("reward", "overrideCount").setValue(((ItemStack) holder.getReward().getReward()).getQuantity());
                }
            } else {
                hd.getNode("reward", "overrideItem").setValue(itemToNode((ItemStack) holder.getReward().getReward()));
            }

            String checkAgainst;
            if (holder.getDisplayItem().get(Keys.DISPLAY_NAME).isPresent()) {
                checkAgainst = holder.getDisplayItem().get(Keys.DISPLAY_NAME).get().toPlain();
            } else {
                checkAgainst = holder.getDisplayItem().getItem().getName();
            }

            if (!checkAgainst.equals(holder.getReward().getRewardName())) {
                hd.getNode("reward", "overrideRewardName").setValue(holder.getReward().getRewardName());
            }
            hd.getNode("reward","type").setValue("item");
        }
        hd.getNode("weight").setValue(holder.getChance());
        return toOverwrite;
    }
    public static CrateRewardHolder v0_to_v1(ItemStack stack, String command, float chance){
        CrateReward rew;
        String name;
        if(stack.get(Keys.DISPLAY_NAME).isPresent()){
            name = stack.get(Keys.DISPLAY_NAME).get().toPlain();
        }else{
            name = stack.getItem().getName();
        }
        if(command.equals("")) {
            rew = new CrateReward(stack,name,false);
        }else{
            rew = new CrateReward(command,name,false);
        }
        return new CrateRewardHolder(stack,rew,chance,command.equals(""),HuskyCrates.instance.langData);
    }
    private static ItemStack itemFromNode(ConfigurationNode itemRoot){
        try {

            if(itemRoot.getNode("id").isVirtual() ){
                HuskyCrates.instance.logger.error("CHECK ITEM: " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                return ItemStack.empty();
            }
            ItemType type;
            try {
                type = itemRoot.getNode("id").getValue(TypeToken.of(ItemType.class));
            }catch(ObjectMappingException e){
                HuskyCrates.instance.logger.error("CHECK ITEM ID: " + itemRoot.getNode("name").getString("(no name)") + " (item #" + itemRoot.getKey()+  ") || " + itemRoot.getParent().getParent().getKey());
                return ItemStack.empty();
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
                    Enchantment enc = getEnchantment(enchantID); // STRINGS ONLY!
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
    private static ConfigurationNode itemToNode(ItemStack stack){
        ConfigurationNode node = HoconConfigurationLoader.builder().build().createEmptyNode();
        if(stack.get(Keys.DISPLAY_NAME).isPresent()){
            node.getNode("name").setValue(TextSerializers.FORMATTING_CODE.serialize(stack.get(Keys.DISPLAY_NAME).get()));
        }else{
            node.getNode("name").setValue(stack.getItem().getName());
        }
        node.getNode("id").setValue(stack.getItem().getId());
        if(stack.get(Keys.ITEM_LORE).isPresent()){
            ArrayList<Text> lore = (ArrayList<Text>) stack.get(Keys.ITEM_LORE).get();
            if(lore.size() > 0) {
                ArrayList<String> loreStrings = new ArrayList<>();
                for (Text e : lore) {
                    loreStrings.add(TextSerializers.FORMATTING_CODE.serialize(e));
                }
                node.getNode("lore").setValue(loreStrings);
            }
        }
        node.getNode("count").setValue(stack.getQuantity());
        if(stack.get(Keys.ITEM_ENCHANTMENTS).isPresent()){
            List<ItemEnchantment> encs = stack.get(Keys.ITEM_ENCHANTMENTS).get();
            for(ItemEnchantment e: encs){
                node.getNode("enchants",e.getEnchantment().getId()).setValue(e.getLevel());
            }
        }
        return node;
    }
    private static TreeType getTreeType(String id){
        switch(id){
            case "oak":
                return TreeTypes.OAK;
            case "spruce":
                return TreeTypes.SPRUCE;
            case "birch":
                return TreeTypes.BIRCH;
            case "jungle":
                return TreeTypes.JUNGLE;
            case "acacia":
                return TreeTypes.ACACIA;
            case "dark_oak":
                return TreeTypes.DARK_OAK;
        }
        return null;
    }
    private static Enchantment getEnchantment(String id){
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
    }
}
