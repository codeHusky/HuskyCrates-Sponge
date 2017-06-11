package pw.codehusky.huskycrates.crate;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolderParser;
import pw.codehusky.huskycrates.crate.views.*;
import pw.codehusky.huskycrates.lang.LangData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lokio on 12/29/2016.
 */
@SuppressWarnings("deprecation")
public class VirtualCrate {
    private ArrayList<Object[]> itemSet;
    private HashMap<ItemStack, String> commandSet;
    public String displayName;
    public String id;
    public BlockType crateBlockType;
    public ItemType crateBlockItemType;
    public String crateType;
    private float maxProb = 100;
    private HashMap<String,Object> options = new HashMap<>();
    private ItemType keyType;
    private Integer keyDamage= null;
    private LangData langData;
    public boolean isGUI;
    public boolean freeCrate = false;
    public boolean virtualKeys = false;
    public VirtualCrate(String id, ConfigurationLoader<CommentedConfigurationNode> config, CommentedConfigurationNode node){
        this.id = id;
        displayName = node.getNode("name").getString();
        if(node.getNode("type").isVirtual()){
            node.getNode("type").setValue("Spinner");
        }
        crateBlockType = BlockTypes.CHEST;
        crateBlockItemType = ItemTypes.CHEST;
        keyType = ItemTypes.NETHER_STAR;//default
        crateType = node.getNode("type").getString("null");

        isGUI = !crateType.equalsIgnoreCase("instant");

        if(crateType.equalsIgnoreCase("spinner")){
            if(!node.getNode("spinnerOptions").isVirtual()){
                ConfigurationNode ops = node.getNode("spinnerOptions");
                if(!ops.getNode("dampening").isVirtual()){
                    options.put("dampening",ops.getNode("dampening").getDouble(1.05));
                }
                if(!ops.getNode("maxClicks").isVirtual()){
                    options.put("maxClicks",ops.getNode("maxClicks").getInt(45));
                }
                if(!ops.getNode("minClickModifier").isVirtual() || !ops.getNode("maxClickModifier").isVirtual()){
                    options.put("minClickModifier",ops.getNode("minClickModifier").getInt(0));
                    options.put("maxClickModifier",ops.getNode("maxClickModifier").getInt(0));
                }
            }
        }
        if(!node.getNode("lang").isVirtual()){
            langData = new LangData(node.getNode("lang"));
        }else{
            langData = HuskyCrates.instance.langData;
        }
        if(!node.getNode("options").isVirtual()){
            ConfigurationNode gops = node.getNode("options");
            virtualKeys = gops.getNode("useVirtualKeys").getBoolean(false);
            freeCrate = gops.getNode("freeCrate").getBoolean(false);
            if(freeCrate){
                options.put("freeCrateDelay",gops.getNode("freeCrateDelay").getInt(0)); // IN SECONDS
            }
            if(!gops.getNode("crateBlockID").isVirtual()){
                try {
                    crateBlockItemType = gops.getNode("crateBlockID").getValue(TypeToken.of(ItemType.class));
                    crateBlockType = crateBlockItemType.getBlock().get();
                } catch (ObjectMappingException e) {
                    HuskyCrates.instance.logger.error("Invalid crate block ID in options.");
                    e.printStackTrace();
                }
            }
            if(!gops.getNode("particle1").isVirtual()){
                HashMap<String,Integer> color = new HashMap<>();
                if(!gops.getNode("particle1","color").isVirtual()){
                    List<Integer> arr;
                    try {
                        arr = gops.getNode("particle1","color").getList(TypeToken.of(Integer.class));
                        if(arr.size() == 3) {
                            options.put("clr1",
                                    Color.ofRgb(
                                            arr.get(0),
                                            arr.get(1),
                                            arr.get(2)));
                        }else{
                            HuskyCrates.instance.logger.warn("Invalid color for particle1! crate:" + id);
                        }
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }

                }

            }
            if(!gops.getNode("particle2").isVirtual()){
                HashMap<String,Integer> color = new HashMap<>();
                if(!gops.getNode("particle2","color").isVirtual()){
                    List<Integer> arr;
                    try {
                        arr = gops.getNode("particle2","color").getList(TypeToken.of(Integer.class));
                        if(arr.size() == 3) {
                            options.put("clr2",
                                    Color.ofRgb(
                                            arr.get(0),
                                            arr.get(1),
                                            arr.get(2)));
                        }else{
                            HuskyCrates.instance.logger.warn("Invalid color for particle2! crate:" + id);
                        }
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(!gops.getNode("keyID").isVirtual()){
                try {
                    keyType = gops.getNode("keyID").getValue(TypeToken.of(ItemType.class));
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }
            if(!gops.getNode("damage").isVirtual()){
                keyDamage = gops.getNode("damage").getInt(0);
            }
        }
        HuskyCrates.instance.validCrateBlocks.add(crateBlockType);
        List<? extends CommentedConfigurationNode> items = node.getNode("items").getChildrenList();
        ArrayList<Object[]> equality = new ArrayList<>();
        float currentProb = 0;
        itemSet = new ArrayList<>();
        commandSet = new HashMap<>();
        ///System.out.println("???");
        for(CommentedConfigurationNode e : items){
            CrateRewardHolder rewardHolder = null;
//            System.out.println(e.getNode("formatversion").getValue());
            if(e.getNode("formatversion").isVirtual()){
               HuskyCrates.instance.logger.error("Out of date item in " + id + "! Please update your config with HuskyCrates Config Updater!");
               continue;
            }
            rewardHolder = CrateRewardHolderParser.fromConfig(e,this);
            if(rewardHolder == null)
                continue;

            Object[] t = {rewardHolder.getChance(), rewardHolder};
            currentProb += rewardHolder.getChance();
            itemSet.add(t);



        }
        if(equality.size() > 0){
            int remaining =(int) (100 - currentProb);
            float equalProb = (float)remaining / (float)equality.size();
            for(Object[] item : equality){
                Object[] hj = {equalProb};
                Object[] fin = ArrayUtils.addAll(hj,item);
                currentProb += equalProb;
                //System.out.println((float)fin[0]);
                itemSet.add(fin);
            }
        }else{
            maxProb = currentProb;
        }
        if(currentProb != maxProb){
            System.out.println("You have too big of a chance! " + id + " (" + currentProb + ")");
            System.out.println("This only fires if you have assumed probability. If you remove assumed chance, this error will be fixed.");
            System.out.println("If everything looks right in your config, contact @codeHusky on Sponge Forums.");
        }

        //Self resolving crate
    }
    public ArrayList<Object[]> getItemSet(){
        return itemSet;
    }
    public HashMap<ItemStack,String> getCommandSet(){
        return commandSet;
    }
    public float getMaxProb(){
        return maxProb;
    }
    public CrateView generateViewForCrate(HuskyCrates plugin,Player plr){
        if(crateType.equalsIgnoreCase("spinner")){
            return new SpinnerCrateView(plugin,plr,this);
        }else if(crateType.equalsIgnoreCase("roulette")){
            return new RouletteCrateView(plugin,plr,this);
        }else if(crateType.equalsIgnoreCase("instant")){
            return new InstantView(plugin,plr,this);
        }else if(crateType.equalsIgnoreCase("simple")){
            return new InstantGUIView(plugin,plr,this);
        }
        return new NullCrateView(plugin,plr,this);
    }



    /***
     * Retrieve the crate item
     * @since 0.10.2
     * @param quantity the quantity of keys you want.
     * @return the ItemStack with the keys.
     */
    public ItemStack getCrateKey(int quantity){
        ItemStack key = ItemStack.builder()
                .itemType(keyType)
                .quantity(quantity)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(displayName + " Key")).build();
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(TextColors.WHITE, "A key for a ", TextSerializers.FORMATTING_CODE.deserialize(displayName), TextColors.WHITE, "."));
        itemLore.add(Text.of(TextColors.DARK_GRAY, "HuskyCrates"));
        key.offer(Keys.ITEM_LORE, itemLore);
        if(keyDamage != null){
            key = ItemStack.builder().fromContainer(key.toContainer().set(DataQuery.of("UnsafeDamage"),keyDamage)).build();
        }

        return ItemStack.builder().fromContainer(key.toContainer().set(DataQuery.of("UnsafeData","crateID"),id)).build();//

    }
    /***
     * Retrieve the crate item
     * @since 1.2.1
     * @return the ItemStack with the keys.
     */
    public ItemStack getCrateWand(){
        ItemStack key = ItemStack.builder()
                .itemType(ItemTypes.BLAZE_ROD)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(displayName + " Wand")).build();
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(TextColors.WHITE, "A wand for a ", TextSerializers.FORMATTING_CODE.deserialize(displayName), TextColors.WHITE, "."));
        itemLore.add(Text.of(TextColors.DARK_GRAY, "HuskyCrates"));
        key.offer(Keys.ITEM_LORE, itemLore);
        if(keyDamage != null){
            key = ItemStack.builder().fromContainer(key.toContainer().set(DataQuery.of("UnsafeDamage"),keyDamage)).build();
        }

        return ItemStack.builder().fromContainer(key.toContainer().set(DataQuery.of("UnsafeData","crateID"),id)).build();//

    }

    public ItemType getKeyType() {
        return keyType;
    }

    /***
     * Retrieve the crate chest.
     * @since 0.10.2
     * @param quantity the quantity of chests you want.
     * @return the ItemStack with the chest.
     */
    public ItemStack getCrateItem(int quantity) {
        ItemStack stacky = ItemStack.builder()
                .itemType(crateBlockItemType)
                .quantity(quantity)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(displayName)).build();
        ArrayList<Text> itemLore = new ArrayList<>();
        itemLore.add(Text.of(TextColors.WHITE, "A placeable ", TextSerializers.FORMATTING_CODE.deserialize(displayName), TextColors.WHITE, " crate."));
        itemLore.add(Text.of(TextColors.DARK_GRAY, "HuskyCrates"));
        stacky.offer(Keys.ITEM_LORE, itemLore);
        return ItemStack.builder().fromContainer(stacky.toContainer().set(DataQuery.of("UnsafeData","crateID"),id)).build();
    }

    public HashMap<String, Object> getOptions() {
        return options;
    }

    public LangData getLangData() {
        return langData;
    }
}
