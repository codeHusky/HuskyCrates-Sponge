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

import java.util.function.Consumer;

public class SpinnerView implements Consumer<Page> {
    private Crate crate;
    private int selectedSlot;
    private Player player;
    public SpinnerView(Crate crate, Player player){
        this.crate = crate;
        this.selectedSlot = crate.selectSlot();
        this.player = player;
        Page.PageBuilder builder =
            Page.builder()
                .setAutoPaging(false)
                .setTitle(TextSerializers.FORMATTING_CODE.deserialize(crate.getName()))
                .setUpdatable(true)
                .setUpdater(this)
                .setInventoryDimension(InventoryDimension.of(9,3));
        Element borderElement = new Element(crate.getViewConfig().getBorderItem().toItemStack());
        Element selectorItem = new Element(((SpinnerView.Config)crate.getViewConfig()).getSelectorItem().toItemStack());
        for(int i = 0; i < 9*3; i++){
            builder.putElement(i,(i == 4 | i == 22)? selectorItem : borderElement);
        }
        Page page = builder.build("meme");
        StateContainer sc = new StateContainer();
        sc.setInitialState(page);
        sc.launchFor(player);
    }

    int spinnerOffset = 0;
    @Override
    public void accept(Page page) {
        if(page.getTicks() > 20 * 5){
            crate.getSlot(selectedSlot).rewardPlayer(player);
            page.getObserver().closeInventory();
        }
        page.getObserver().playSound(SoundTypes.UI_BUTTON_CLICK,page.getObserver().getLocation().getPosition(),0.5);
        int num = 0;
        for (Inventory slot : page.getPageView().slots()) {
            if(num >= 10 && num <= 16){
                slot.set(
                        crate.getSlot( ( spinnerOffset + (num - 10) ) % crate.getSlotCount() )
                                .getDisplayItem()
                                .toItemStack()
                );
            }
            num++;
        }
        spinnerOffset++;

    }

    public static class Config extends ViewConfig {
        private Item selectorItem;
        public Config(ConfigurationNode node){
            super(node);
            if(!node.getNode("selectorItem").isVirtual()) {
                this.selectorItem = new Item(node.getNode("selectorItem"));
            }else{
                this.selectorItem = new Item("&6HuskyCrates", ItemTypes.REDSTONE_TORCH,null,1,null,null,null,null);
            }
        }

        public Item getSelectorItem() {
            return selectorItem;
        }
    }
}
