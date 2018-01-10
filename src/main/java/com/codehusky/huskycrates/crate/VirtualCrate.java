package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.crate.db.DBReader;
import com.codehusky.huskycrates.crate.views.*;
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.crate.config.CrateConfigParser;
import com.codehusky.huskycrates.lang.LangData;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

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
    public int crateBlockDamage = 0;
    public String crateType;
    private float maxProb = 0;
    private HashMap<String,Object> options = new HashMap<>();
    public HashMap<String,Integer> pendingKeys = new HashMap<>();
    public HashMap<String, Integer> virtualBalances = new HashMap<>();
    public HashMap<UUID, LocalDateTime> lastUsed = new HashMap<>();
    private ItemType keyType;
    private Integer keyDamage= null;
    private LangData langData;
    public boolean isGUI;
    public boolean freeCrate = false;
    public boolean showRewardsOnLeft = false;
    public boolean scrambleRewards = false;
    public boolean showProbability = true;
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
            langData = new LangData(HuskyCrates.instance.langData,node.getNode("lang"));
        }else{
            langData = HuskyCrates.instance.langData;
        }
        if(!node.getNode("options").isVirtual()){
            ConfigurationNode gops = node.getNode("options");
            freeCrate = gops.getNode("freeCrate").getBoolean(false);
            if(freeCrate){
                options.put("freeCrateDelay",gops.getNode("freeCrateDelay").getInt(0)); // IN SECONDS
            }
            if(!gops.getNode("crateBlockDamage").isVirtual()){
                crateBlockDamage = gops.getNode("crateBlockDamage").getInt(0);
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
            if(!gops.getNode("showRewardsOnLeft").isVirtual()){
                showRewardsOnLeft = gops.getNode("showRewardsOnLeft").getBoolean(false);
            }
            if(!gops.getNode("showProbability").isVirtual()){
                showProbability = gops.getNode("showProbability").getBoolean(true);
            }
            if(!gops.getNode("scrambleRewards").isVirtual()){
                scrambleRewards = gops.getNode("scrambleRewards").getBoolean(false);
            }
        }
        HuskyCrates.instance.validCrateBlocks.add(crateBlockType);
        List<? extends CommentedConfigurationNode> items = node.getNode("items").getChildrenList();
        float currentProb = 0;
        itemSet = new ArrayList<>();
        commandSet = new HashMap<>();
        ///System.out.println("???");
        for(CommentedConfigurationNode e : items){
            CrateReward rewardHolder = null;
//            System.out.println(e.getNode("formatversion").getValue());
            if(!e.getNode("formatversion").isVirtual()){
                HuskyCrates.instance.logger.info("Removing legacy formatversion field from item.");
                e.getNode("formatversion").setValue(null);
            }
            rewardHolder = CrateConfigParser.fromConfig(e,this);
            if(rewardHolder == null)
                continue;

            Object[] t = {rewardHolder.getChance(), rewardHolder};
            currentProb += rewardHolder.getChance();
            itemSet.add(t);



        }

        maxProb = currentProb;


        try {
            CommentedConfigurationNode root = config.load();
            //System.out.println(root.getNode("keys"));
            if(!root.getNode("keys").isVirtual()) {
                HuskyCrates.instance.logger.warn("Legacy key data detected. As long as you have placed crates, we'll convert.");
                if (root.getNode("keys", id).hasListChildren()) {
                    HuskyCrates.initError();
                    HuskyCrates.instance.logger.error("Please manually transfer your keys from the crate " + id + " to use the new format.");
                } else {
                    for (Object key : root.getNode("keys", id).getChildrenMap().keySet()) {
                        CommentedConfigurationNode nn = root.getNode("keys", id).getChildrenMap().get(key);
                        if (nn.getInt(0) == 0) {
                            nn.setValue(null);
                        } else {
                            pendingKeys.put(key.toString(), nn.getInt(1));
                        }
                    }
                }
                root.removeChild("keys");
                config.save(root);
                HuskyCrates.instance.logger.info("Loaded " + pendingKeys.size() + " " + id + " key UUIDs (LEGACY METHOD)");
            }

        } catch (IOException  e) {
            HuskyCrates.initError();
            HuskyCrates.instance.logger.error("Failed to load key UUIDs. Keys will not work!");
            e.printStackTrace();
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
    public CrateView generateViewForCrate(HuskyCrates plugin, Player plr){
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
        String keyUUID = registerKey(quantity);
        if(keyUUID == null){
            HuskyCrates.instance.logger.error("Throwing NullPointerException: Key failed to register.");
            throw new NullPointerException();
        }
        return ItemStack.builder().fromContainer(key.toContainer().set(DataQuery.of("UnsafeData","crateID"),id).set(DataQuery.of("UnsafeData","keyUUID"),keyUUID)).build();//

    }

    /***
     * Generates a registered single-use key uuid.
     * @since 1.6.0-PRE2
     * @param quantity The amount of keys to register under the uuid.
     * @return Registered key UUID
     */
    private String registerKey(int quantity) {
        String keyUUID = UUID.randomUUID().toString();
        pendingKeys.put(keyUUID,quantity);
        try {
            DBReader.saveHuskyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keyUUID;
    }

    public boolean keyIsValid(String uuid){
        return pendingKeys.containsKey(uuid);
    }

    public boolean expireKey(String uuid){
        if(pendingKeys.containsKey(uuid)){
            int count = pendingKeys.get(uuid);
            try {
                if(count == 1) {
                    pendingKeys.remove(uuid);
                }else{
                    pendingKeys.put(uuid,pendingKeys.get(uuid)-1);
                }
                DBReader.saveHuskyData();
            } catch (SQLException e) {
                HuskyCrates.instance.logger.error("User attempted to use key with uuid " + uuid + " that has not been expired. Do not enforce dupe rules on user.");
                e.printStackTrace();
                pendingKeys.put(uuid,count);
                return false;
            }
            return true;
        }else{
            return false;
        }
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
        stacky = ItemStack.builder()
                .fromContainer(stacky.toContainer().set(DataQuery.of("UnsafeDamage"),crateBlockDamage)) //OVERRIDE DAMAGE VAL! :)
                .build();
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

    /* The mess of Virtual Key methods */
    public int getVirtualKeyBalance(User player) {
        if(virtualBalances.containsKey(player.getUniqueId().toString())) {
            return virtualBalances.get(player.getUniqueId().toString());
        }
        return 0;
    }
    public void takeVirtualKeys(User player,int count){
        if(virtualBalances.containsKey(player.getUniqueId().toString())) {
            virtualBalances.put(player.getUniqueId().toString(),virtualBalances.get(player.getUniqueId().toString())-count);
        }
        try {
            DBReader.saveHuskyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void takeVirtualKey(User player){
        takeVirtualKeys(player,1);
    }
    public void giveVirtualKeys(User player, int count){
        if(virtualBalances.containsKey(player.getUniqueId().toString())) {
            virtualBalances.put(player.getUniqueId().toString(),virtualBalances.get(player.getUniqueId().toString())+count);
        } else{
            virtualBalances.put(player.getUniqueId().toString(),count);
        }
        try {
            DBReader.saveHuskyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void givePlayersVirtualKeys(Collection<Player> players, int count){

        for(Player player : players) {
            if(virtualBalances.containsKey(player.getUniqueId().toString())) {
                virtualBalances.put(player.getUniqueId().toString(),virtualBalances.get(player.getUniqueId().toString())+count);
            }else{
                virtualBalances.put(player.getUniqueId().toString(),count);
            }
        }
        try {
            DBReader.saveHuskyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void setVirtualKeys(User player, int count) {
        virtualBalances.put(player.getUniqueId().toString(),count);
        try {
            DBReader.saveHuskyData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
