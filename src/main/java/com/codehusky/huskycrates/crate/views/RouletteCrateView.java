package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by lokio on 12/29/2016.
 */
public class RouletteCrateView extends CrateView {
    private Inventory disp;
    private Task updater;
    private boolean stopped = false;
    private CrateReward holder;
    private boolean firedEnd = false;
    private boolean outOfTime = false;
    public RouletteCrateView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.plugin = plugin;
        vc = virtualCrate;
        ourplr = runner;
        items = vc.getItemSet();
        if(virtualCrate.scrambleRewards){
            scrambleRewards();
        }
        disp = Inventory.builder()
                .of(InventoryArchetypes.DISPENSER)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                        if(!stopped && evt instanceof ClickInventoryEvent){
                            ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
                        }
                        stopped = true;
                    }

                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName)))
                .build(plugin);
        updateInv(0);
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        updater = taskBuilder.execute(this::updateTick).intervalTicks(1).submit(plugin);
    }
    private int tickCount = 0;
    private void updateInv(int state){
        int secRemain = (10 - Math.round(tickCount / 20));
        if(secRemain < 0)
            stopped = true;
        int slotNum = 0;
        for(Inventory e : disp.slots()){
            double speed = 3;
            double confettiSpeed = 2;
            if(slotNum != 4) {
                if(stopped) {
                    if (tickCount == 0 || Math.round(tickCount / confettiSpeed) > Math.round((tickCount - 1) / confettiSpeed)) {
                        e.set(confetti());
                    } else {
                        e.set(e.peek().get());
                    }
                }else{
                    ItemStack border;
                    if(Math.floor(slotNum / 3) != 1){
                        border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR,DyeColors.BLACK).build();
                    }else{
                        border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR,DyeColors.GRAY).build();
                    }
                    border.offer(Keys.DISPLAY_NAME,Text.of(TextStyles.RESET,"HuskyCrates"));
                    ArrayList<Text> itemLore = new ArrayList<>();
                    itemLore.add(Text.of(TextColors.DARK_GRAY, "Click anywhere to stop!"));
                    itemLore.add(Text.of(TextColors.DARK_GRAY, "Seconds remaining: " + secRemain));
                    border.offer(Keys.ITEM_LORE, itemLore);
                    e.set(border);
                }
            }else if(!stopped&&(tickCount == 0 || Math.round(tickCount/speed) > Math.round((tickCount-1)/speed))){
                try {
                    int i = itemIndexSelected();
                    e.set(((CrateReward)items.get(i)[1]).getDisplayItem());
                    holder = (CrateReward)items.get(i)[1];
                    ourplr.playSound(SoundTypes.UI_BUTTON_CLICK,ourplr.getLocation().getPosition(),0.25);
                } catch (RandomItemSelectionFailureException e1) {
                    plugin.logger.error("Random Item Selection failed in Roulette Crate View: " + vc.displayName);
                }

                //e.set(((CrateRewardHolder)items.get(Math.round(tickCount/2) % items.size())[1]).getDisplayItem());
            }else{
                if(stopped && !firedEnd){
                    if(secRemain < 0){
                        outOfTime = true;
                        ourplr.playSound(SoundTypes.BLOCK_GLASS_BREAK,ourplr.getLocation().getPosition(),1);
                    }
                    Sponge.getScheduler().createTaskBuilder().execute(task -> {
                        updater.cancel();
                        ourplr.closeInventory();
                        handleReward(holder);
                        ourplr.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP,ourplr.getLocation().getPosition(),1);
                    }).delay(3, TimeUnit.SECONDS).submit(HuskyCrates.instance);
                    firedEnd = true;
                }
                e.set(e.peek().get());
            }
            slotNum++;
        }
    }
    private ItemStack confetti(){
        DyeColor[] colors = {DyeColors.BLUE,DyeColors.CYAN,DyeColors.LIME,DyeColors.LIGHT_BLUE,DyeColors.MAGENTA,DyeColors.ORANGE,DyeColors.PINK,DyeColors.PURPLE,DyeColors.RED,DyeColors.YELLOW};
        ItemStack g =ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR,colors[(int)Math.floor(Math.random() * colors.length)])
                .build();
        if(!outOfTime) {
            g.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET, "Your prize awaits..."));
        }else{
            g.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET,TextColors.RED, "Ran out of time!"));
        }
        return g;
    }
    private void updateTick() {
        updateInv(0);
        tickCount++;
    }
    @Override
    public Inventory getInventory() {
        return disp;
    }
}
