package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.crate.config.CrateReward;
import pw.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;

import java.util.Random;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class SpinnerCrateView extends CrateView {
    private Inventory disp;
    private Task updater;
    private int clicks = 0;
    private double dampening = 1.05;
    private int maxClicks = 45; // maximum times the spinner "clicks" in one spin
    public SpinnerCrateView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.vc = virtualCrate;
        ourplr = runner;
        this.plugin = plugin;

        items = virtualCrate.getItemSet();

        if(virtualCrate.getOptions().containsKey("dampening")) {
            dampening = (double) virtualCrate.getOptions().get("dampening");
            //HuskyCrates.instance.logger.info("dampening override: " + dampening);
        }
        if(virtualCrate.getOptions().containsKey("maxClicks")) {
            maxClicks = (int) virtualCrate.getOptions().get("maxClicks");
            //HuskyCrates.instance.logger.info("maxClicks override: " + maxClicks);
        }
        if(virtualCrate.getOptions().containsKey("minClickModifier") || virtualCrate.getOptions().containsKey("maxClickModifier")){
            int min = (int)virtualCrate.getOptions().get("minClickModifier");
            int max = (int)virtualCrate.getOptions().get("maxClickModifier");
            Random rand = new Random();
            maxClicks += Math.round((max*rand.nextDouble())+(min*rand.nextDouble()));
        }
        try {
            clicks = itemIndexSelected() - (maxClicks % items.size());
        }catch (RandomItemSelectionFailureException e){
            System.out.println("--------------------------------");
            System.out.println("--------------------------------");
            System.out.println("ERROR WHEN INITING RANDOM ITEM FOR " + vc.displayName);
            System.out.println("--------------------------------");
            System.out.println("--------------------------------");
        }
        disp = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                    }
                    //System.out.println(evt.getClass());
                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName)))
                .build(plugin);
        updateInv(0);
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        updater = taskBuilder.execute(this::updateTick).intervalTicks(1).submit(plugin);


    }
    private void updateInv(int state) {
        ItemStack border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR,DyeColors.BLACK).build();
        border.offer(Keys.DISPLAY_NAME,Text.of(TextStyles.RESET,"HuskyCrates"));
        //border.offer(Keys.ITEM_LORE,lore);
        ItemStack selector = ItemStack.of(ItemTypes.REDSTONE_TORCH,1);
        selector.offer(Keys.DISPLAY_NAME,Text.of(TextStyles.RESET,"HuskyCrates"));
        //selector.offer(Keys.ITEM_LORE,lore);
        int slotnum = 0;
        for(Inventory e : disp.slots()){
            if(state == 0 && (slotnum == 4 || slotnum == 22 )){

                e.set(selector);
            }else if(slotnum > 9 && slotnum < 17 && state != 2){
                //int itemNum = items.size() - 1 - Math.abs(((slotnum - 10) + (clicks)) % items.size());

                int itemNum = Math.abs(clicks + (slotnum - 9) - 4 ) % items.size();
                e.set(((CrateReward)items.get(itemNum)[1]).getDisplayItem());
                if(slotnum == 13) {
                    //System.out.println(itemNum);
                    giveToPlayer = (CrateReward)items.get(itemNum)[1];
                }
            }else if(slotnum != 13){
                if(state == 2 ){
                    e.set(confettiBorder());
                }else if(state == 0){
                    e.set(border);
                }
            }else if(state == 2){
                int itemNum = Math.abs(clicks + (slotnum - 9) - 4) % items.size();
                /*HuskyCrates.instance.logger.warn("result: " + (itemNum + 1));
                HuskyCrates.instance.logger.warn("fail: " + (planned != itemNum));
                HuskyCrates.instance.logger.warn("difference: " + (planned - itemNum));*/
                giveToPlayer = (CrateReward)items.get(itemNum)[1];
                e.set(giveToPlayer.getDisplayItem());
            }
            slotnum++;
        }
        if(!ourplr.isViewingInventory()){
            ourplr.openInventory(disp,plugin.genericCause);
        }
    }
    private ItemStack confettiBorder(){
        DyeColor[] colors = {DyeColors.BLUE,DyeColors.CYAN,DyeColors.LIGHT_BLUE,DyeColors.LIME,DyeColors.MAGENTA,DyeColors.ORANGE,DyeColors.PINK,DyeColors.PURPLE,DyeColors.RED,DyeColors.YELLOW};
        ItemStack g =ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR,colors[(int)Math.floor(Math.random() * colors.length)])
                .build();
        g.offer(Keys.DISPLAY_NAME,Text.of(TextStyles.RESET,"You won an item!"));
        return g;
    }
    private CrateReward giveToPlayer;
    private double updateMax = 1;
    private int waitCurrent = 0;

    private int tickerState = 0;
    private int trueclicks = 0;
    private void updateTick() {
        //revDampening = 1.15;
        waitCurrent++;
        //int revolutions = (int) Math.floor(clicks / items.size());
        //once clicks is greater than offset we stop the spinner
        if (waitCurrent == Math.round(updateMax) &&
                trueclicks < maxClicks &&
                tickerState == 0) {
            //System.out.println(clicks + " : " + offset);

            waitCurrent = 0;
            updateMax *= dampening;
            updateInv(-1);
            ourplr.playSound(SoundTypes.UI_BUTTON_CLICK,ourplr.getLocation().getPosition(),0.25);
            clicks++;
            trueclicks++;
            //HuskyCrates.instance.logger.info(maxClicks + " : " + trueclicks);

        }else if(trueclicks
                >=
                maxClicks &&
                tickerState == 0){
            ourplr.openInventory(disp,plugin.genericCause);
            tickerState = 1;
            ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
            updateMax = 100;
            waitCurrent = 0;
        }else if(tickerState == 1){
            if (waitCurrent == Math.round(updateMax)) {
                updater.cancel();
                ourplr.closeInventory(plugin.genericCause);
                handleReward(giveToPlayer);
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
