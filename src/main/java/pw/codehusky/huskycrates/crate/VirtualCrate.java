package pw.codehusky.huskycrates.crate;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolderParser;
import pw.codehusky.huskycrates.crate.views.CSGOCrateView;
import pw.codehusky.huskycrates.crate.views.CrateView;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

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
        System.out.println("???");
        for(CommentedConfigurationNode e : items){
            CrateRewardHolder rewardHolder = null;
            System.out.println(e.getNode("formatversion").getValue());
            if(e.getNode("formatversion").isVirtual()){
                System.out.println("??");
                //Old, deprecated format. Convert!!
                String name = e.getNode("name").getString("");
                String itemID = e.getNode("id").getString("").toUpperCase();
                int amount = e.getNode("amount").getInt(1);
                ItemStack ourChild = null;
                try {
                    ourChild = ItemStack.builder()
                            .itemType(e.getNode("id").getValue(TypeToken.of(ItemType.class)))
                            .quantity(amount)
                            .build();
                } catch (ObjectMappingException e1) {
                    e1.printStackTrace();
                }
                if (name.length() > 0)
                    ourChild.offer(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(name));
                //ed.addElement()
                String lore = e.getNode("lore").getString("");
                ArrayList<Text> bb = new ArrayList<>();
                if (lore.length() > 0) {
                    bb.add(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(lore));
                }

                ourChild.offer(Keys.ITEM_LORE, bb);
                ourChild.offer(Keys.HIDE_ENCHANTMENTS, true);
                String potentialCommand = e.getNode("command").getString("");
                float chance = e.getNode("chance").getFloat(1);
                e.setValue(CrateRewardHolderParser.toConfig(CrateRewardHolderParser.v0_to_v1(ourChild,potentialCommand,chance)));
                e.getNode("formatversion").setValue(1);
            }
            rewardHolder = CrateRewardHolderParser.fromConfig(e);


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
