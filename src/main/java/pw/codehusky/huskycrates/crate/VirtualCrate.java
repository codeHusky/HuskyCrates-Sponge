package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.CSGOCrateView;
import pw.codehusky.huskycrates.crate.views.CrateView;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lokio on 12/29/2016.
 */
@SuppressWarnings("deprecation")
public class VirtualCrate {
    private ArrayList<Object[]> itemSet;
    public String displayName;
    public String crateType;
    public boolean invalidCrate = false;
    public VirtualCrate(String id, CommentedConfigurationNode node){
        displayName = node.getNode("name").getString();
        crateType = node.getNode("type").getString();
        List<? extends CommentedConfigurationNode> items = node.getNode("items").getChildrenList();
        ArrayList<ItemStack> equality = new ArrayList<>();
        float currentProb = 0;
        itemSet = new ArrayList<>();
        for(CommentedConfigurationNode e : items){

            String name = e.getNode("name").getString("");
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
                //ed.addElement()
                String lore = e.getNode("lore").getString("");
                if(lore.length() > 0) {
                    ArrayList<Text> bb = new ArrayList<>();
                    bb.add(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(lore));
                    ourChild.offer(Keys.ITEM_LORE,bb);
                }
                ourChild.offer(ed);
                if(e.getNode("chance").isVirtual()){
                    equality.add(ourChild);
                }else{
                    Object[] t = {e.getNode("chance").getFloat(),ourChild};
                    currentProb += e.getNode("chance").getFloat();
                    itemSet.add(t);
                }
            }
        }
        if(currentProb >= 100){
            if(equality.size() > 0){
                System.out.println("You have an invalid chance configuration! " + id);
            }
        }else{
            int remaining =(int) (100 - currentProb);
            float equalProb = (float)remaining / (float)equality.size();
            for(ItemStack item : equality){
                Object[] t = {equalProb,item};
                currentProb += equalProb;
                itemSet.add(t);
            }
        }
        //Self resolving crate
    }
    public ArrayList<Object[]> getItemSet(){
        return itemSet;
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
