package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;
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
import org.spongepowered.api.text.serializer.TextSerializers;
import com.codehusky.huskycrates.crate.config.CrateReward;

import java.util.concurrent.TimeUnit;

public class InstantGUIView extends CrateView {
    private Inventory disp;
    private Task updater;
    private CrateReward holder = null;
    public InstantGUIView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        //System.out.println("AA");
        this.plugin = plugin;
        vc = virtualCrate;
        ourplr = runner;
        items = vc.getItemSet();
        disp = Inventory.builder()
                .of(InventoryArchetypes.DISPENSER)
                .listener(InteractInventoryEvent.class, evt ->{
                    if(!(evt instanceof InteractInventoryEvent.Open) && !(evt instanceof  InteractInventoryEvent.Close)){
                        evt.setCancelled(true);
                    }

                })
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName)))
                .build(plugin);
        updateInv(0);
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        updater = taskBuilder.execute(this::updateTick).intervalTicks(1).submit(plugin);
        scheduler.createTaskBuilder().execute(() -> {
            updater.cancel();
            ourplr.closeInventory(plugin.genericCause);
            handleReward(holder);
            ourplr.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP,ourplr.getLocation().getPosition(),1);
        }).delay(3, TimeUnit.SECONDS).submit(HuskyCrates.instance);
    }
    private int tickCount = 0;
    private void updateInv(int state){
        int slotNum = 0;
        for(Inventory e : disp.slots()){
            double speed = 3;
            double confettiSpeed = 2;
            if(slotNum != 4) {
                if (tickCount == 0 || Math.round(tickCount / confettiSpeed) > Math.round((tickCount - 1) / confettiSpeed)) {
                    e.set(confetti());
                } else {
                    e.set(e.peek().get());
                }
            }else if(holder == null){
                try {
                    int i = itemIndexSelected();
                    e.set(((CrateReward)items.get(i)[1]).getDisplayItem());
                    holder = (CrateReward)items.get(i)[1];
                    ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
                } catch (RandomItemSelectionFailureException e1) {
                    plugin.logger.error("Random Item Selection failed in Roulette Crate View: " + vc.displayName);
                }

                //e.set(((CrateRewardHolder)items.get(Math.round(tickCount/2) % items.size())[1]).getDisplayItem());
            }else{
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
        g.offer(Keys.DISPLAY_NAME, Text.of("HuskyCrates"));
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
