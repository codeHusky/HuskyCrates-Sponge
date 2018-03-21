package com.codehusky.huskycrates.crate.virtual.views;

import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Item;
import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.element.Element;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Random;
import java.util.function.Consumer;

public class SpinnerView implements Consumer<Page> {
    private Crate crate;
    private int selectedSlot;
    private Player player;
    private Config config;

    public SpinnerView(Crate crate, Player player){
        this.crate = crate;
        this.config = (Config) crate.getViewConfig();
        this.variance = (int)Math.round(new Random().nextDouble() * config.getTicksToSelectionVariance());
        this.selectedSlot = crate.selectSlot();
        this.player = player;
        Page.PageBuilder builder =
            Page.builder()
                .setAutoPaging(false)
                .setTitle(TextSerializers.FORMATTING_CODE.deserialize(crate.getName()))
                .setUpdatable(true)
                .setUpdater(this)
                .setInterrupt(() -> crate.getSlot(selectedSlot).rewardPlayer(player))
                .setInventoryDimension(InventoryDimension.of(9,3));

        Element borderElement = new Element(config.getBorderItem().toItemStack());

        Element selectorItem = new Element(config.getSelectorItem().toItemStack());

        for(int i = 0; i < 9*3; i++){
            builder.putElement(i,(i == 4 | i == 22)? selectorItem : borderElement);
        }
        Page page = builder.build("meme");
        StateContainer sc = new StateContainer();
        sc.setInitialState(page);
        sc.launchFor(player);
    }

    int spinnerOffset = 0;
    int currentTicks = 0;
    double currentTickDelay = 1;
    int variance = 0;
    @Override
    public void accept(Page page) {
        if(spinnerOffset + variance >= config.getTicksToSelection()){
            crate.getSlot(selectedSlot).rewardPlayer(player);
            page.getObserver().closeInventory();
            return;
        }

        int num = 0;
        for (Inventory slot : page.getPageView().slots()) {
            if(num >= 10 && num <= 16){
                slot.set(
        //(spinner offset + (a buffer to prevent neg numbers + (sel slot + 1 offset) - 3 for centering) + (slotnum rel to center) % slot count
                    crate.getSlot( ( (spinnerOffset + (crate.getSlotCount()*3) + (selectedSlot+1) - 3) + (num - 10) ) % crate.getSlotCount() )
                            .getDisplayItem()
                            .toItemStack()
                );
            }
            num++;
        }

        if(currentTicks >= currentTickDelay) {
            currentTicks = 0;
            currentTickDelay *= config.getTickDelayMultiplier();
            page.getObserver().playSound(SoundTypes.UI_BUTTON_CLICK,page.getObserver().getLocation().getPosition(),0.5);
            spinnerOffset++;
        }
        currentTicks++;
    }

    public static class Config extends ViewConfig {
        private Item selectorItem;
        private Integer ticksToSelection;
        private Double tickDelayMultiplier;
        private Integer ticksToSelectionVariance;
        public Config(ConfigurationNode node){
            super(node);
            if(!node.getNode("selectorItem").isVirtual()) {
                this.selectorItem = new Item(node.getNode("selectorItem"));
            }else{
                this.selectorItem = new Item("&6HuskyCrates", ItemTypes.REDSTONE_TORCH,null,1,null,null,null,null);
            }

            this.ticksToSelection = node.getNode("ticksToSelection").getInt(30);
            this.tickDelayMultiplier = node.getNode("tickDelayMultiplier").getDouble(1.2);
            this.ticksToSelectionVariance = node.getNode("ticksToSelectionVariance").getInt(0);
        }

        public Item getSelectorItem() {
            return selectorItem;
        }

        public Integer getTicksToSelection() {
            return ticksToSelection;
        }

        public Double getTickDelayMultiplier() {
            return tickDelayMultiplier;
        }

        public Integer getTicksToSelectionVariance() {
            return ticksToSelectionVariance;
        }
    }
}
