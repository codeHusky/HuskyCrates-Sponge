package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by lokio on 12/28/2016.
 */
public class CSGOCrateView implements CrateView {
    private HuskyCrates plugin;

    Integer offset = null;
    private ArrayList<Object[]> items;
    private Inventory disp;
    private Task updater;
    private Player ourplr;
    public CSGOCrateView(HuskyCrates plugin,Player runner, VirtualCrate virtualCrate){
        ourplr = runner;
        this.plugin = plugin;
        //temp static item list
        /*
        waitCurrent++;

        if (waitCurrent == Math.round(updateMax) && updateMax < waitStopper) {
            offset++;
            waitCurrent = 0;
            updateMax *= dampening;
            updateInv(-1);
            ourplr.playSound(SoundTypes.UI_BUTTON_CLICK,ourplr.getLocation().getPosition(),0.25);
            ticks++;
        }else if(Math.round(updateMax) == waitStopper){
            ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
            updateMax = 100;
            waitCurrent = 0;
            System.out.println(ticks);
            System.out.println(offsetBase);
        }
         */

        items = virtualCrate.getItemSet();
        //offsetBase = (int)Math.floor(gg);
        float random = new Random().nextFloat()*100;
        float cummProb = 0;
        int offsetAdjust = 2;
        int target = 0;
        for(int i = 0; i < items.size(); i++){
            cummProb += (float)items.get(i)[0];
            if(random <= cummProb && offset == null){
                offset = i;
                target = i;
            }
        }

        System.out.println(((ItemStack)items.get(target)[1]).get(Keys.DISPLAY_NAME));
        disp = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .listener(ClickInventoryEvent.class,evt -> evt.setCancelled(true))
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(Text.of(TextColors.BLACK,"Mining Crate")))
                .build(plugin);
        updateInv(0);
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        updater = taskBuilder.execute(this::updateTick).intervalTicks(1).submit(plugin);


    }
    private void updateInv(int state) {
        ItemStack border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR,DyeColors.BLACK).build();
        border.offer(Keys.DISPLAY_NAME,Text.of(""));
        ItemStack selector = ItemStack.of(ItemTypes.REDSTONE_TORCH,1);
        selector.offer(Keys.DISPLAY_NAME,Text.of(""));
        int slotnum = 0;
        for(Inventory e : disp.slots()){
            if(state == 0 && (slotnum == 4 || slotnum == 22 )){
                e.set(selector);
            }else if(slotnum > 9 && slotnum < 17 && (slotnum == 13 || state != 2)){
                int itemNum = Math.abs(((slotnum - 10) + offset) % items.size());
                e.set((ItemStack)items.get(itemNum)[1]);
                if(slotnum == 13) {
                    giveToPlayer = ((ItemStack)items.get(itemNum)[1]).copy();
                }
            }else if(slotnum != 13){
                if(state == 2 ){
                    e.set(confettiBorder());
                }else if(state == 0){
                    e.set(border);
                }
            }
            slotnum++;
        }
        if(!ourplr.isViewingInventory()){
            ourplr.openInventory(disp,plugin.genericCause);
        }
    }
    private ItemStack confettiBorder(){
        DyeColor[] colors = {DyeColors.BLUE,DyeColors.CYAN,DyeColors.GREEN,DyeColors.LIGHT_BLUE,DyeColors.LIME,DyeColors.MAGENTA,DyeColors.ORANGE,DyeColors.PINK,DyeColors.PURPLE,DyeColors.RED,DyeColors.YELLOW};
        ItemStack g =ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR,colors[(int)Math.floor(Math.random() * colors.length)])
                .build();
        g.offer(Keys.DISPLAY_NAME,Text.of("You won an item!"));
        return g;
    }
    private ItemStack giveToPlayer;
    float updateMax = 1;
    int waitCurrent = 0;
    private double dampening = 1.05;
    private int revCount = 10;
    private int clicks = 0;
    private void updateTick() {
        waitCurrent++;
        int revolutions = (int) Math.floor(clicks / items.size());
        if (waitCurrent == Math.round(updateMax) && revolutions < revCount) {
            offset++;
            waitCurrent = 0;
            updateMax *= dampening;
            updateInv(-1);
            ourplr.playSound(SoundTypes.UI_BUTTON_CLICK,ourplr.getLocation().getPosition(),0.25);
            clicks++;
        }else if(revolutions == revCount && updateMax != 100){
            ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
            updateMax = 100;
            waitCurrent = 0;
            System.out.println(revolutions);
            System.out.println(revCount);
        }else{
            if (waitCurrent == Math.round(updateMax)) {
                updater.cancel();
                ourplr.closeInventory(plugin.genericCause);
                String name = giveToPlayer.createSnapshot().getType().getTranslation().get();
                ourplr.sendMessage(Text.of("You won ", TextColors.YELLOW, giveToPlayer.getQuantity() + " " + name, TextColors.RESET, " from a ",TextColors.BLUE,"Diamond Crate",TextColors.RESET,"!"));
                ourplr.getInventory().offer(giveToPlayer);
                ourplr.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP,ourplr.getLocation().getPosition(),1);

            }else if(waitCurrent % 5 == 0){
                updateInv(2);
            }
        }

    }
    public Inventory getInventory() {
        return disp;
    }
}
