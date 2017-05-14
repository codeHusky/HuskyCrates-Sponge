package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolderParser;
import pw.codehusky.huskycrates.crate.views.CSGOCrateView;
import pw.codehusky.huskycrates.crate.views.CrateView;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
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
    public String crateType;
    private float maxProb = 100;
    public boolean invalidCrate = false;
    public VirtualCrate(String id, ConfigurationLoader<CommentedConfigurationNode> config, CommentedConfigurationNode node){
        displayName = node.getNode("name").getString();
        crateType = node.getNode("type").getString();
        List<? extends CommentedConfigurationNode> items = node.getNode("items").getChildrenList();
        ArrayList<Object[]> equality = new ArrayList<>();
        float currentProb = 0;
        itemSet = new ArrayList<>();
        commandSet = new HashMap<>();
        for(CommentedConfigurationNode e : items){
            CrateRewardHolder rewardHolder = CrateRewardHolderParser.fromConfig(e);
            System.out.println(rewardHolder.getDisplayItem().get(Keys.DISPLAY_NAME));
            System.out.println(rewardHolder.getReward().getRewardName());
            /*String name = e.getNode("name").getString("");
            String itemID = e.getNode("id").getString("").toUpperCase();
            int amount = e.getNode("amount").getInt(1);
            if(Sponge.getRegistry().getType(ItemType.class,itemID).isPresent()) {
                ItemStack ourChild = ItemStack.builder()
                        .itemType(Sponge.getRegistry().getType(ItemType.class,itemID).get())
                        .quantity(amount)
                        .build();
                if(name.length() > 0)
                    ourChild.offer(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(name));
                EnchantmentData ed = ourChild.getOrCreate(EnchantmentData.class).get();
                if(false){
                    ourChild.offer(ed);
                }
                //ed.addElement()
                String lore = e.getNode("lore").getString("");
                ArrayList<Text> bb = new ArrayList<>();
                if(lore.length() > 0) {
                    bb.add(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(lore));
                }

                ourChild.offer(Keys.ITEM_LORE,bb);
                ourChild.offer(Keys.HIDE_ENCHANTMENTS,true);
                String potentialCommand = e.getNode("command").getString("");
                if(e.getNode("chance").isVirtual()){
                    Object[] t = {ourChild};
                    if(potentialCommand.length() > 0){
                        Object[] add = {potentialCommand};
                        Object[] g = ArrayUtils.addAll(t,add);
                        equality.add(g);
                    }else {
                        equality.add(t);
                    }
                }else{
                    Object[] t = {e.getNode("chance").getFloat(), ourChild};
                    currentProb += e.getNode("chance").getFloat();
                    if(potentialCommand.length() > 0){
                        Object[] add = {potentialCommand};
                        Object[] g = ArrayUtils.addAll(t,add);
                        //System.out.println((float)g[0]);
                        itemSet.add(g);
                    }else {
                        itemSet.add(t);
                    }
                }

            }*/
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
        try {
            config.save(node.getParent());
        } catch (IOException e) {
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
    public CrateView generateViewForCrate(HuskyCrates plugin,Player plr){
        if(invalidCrate)
            return new NullCrateView(plugin,plr,this);
        if(crateType.equalsIgnoreCase("spinner")){
            return new CSGOCrateView(plugin,plr,this);
        }else{
            invalidCrate = true;
        }
        return new NullCrateView(plugin,plr,this);
    }
}
