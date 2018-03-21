package com.codehusky.huskycrates.crate.virtual.views;

import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.element.Element;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.function.Consumer;

public class SpinnerView implements Consumer<Page> {
    private int selectedSlot;
    private Player player;
    public SpinnerView(Crate crate, Player player){
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
        for(int i = 0; i < 9*3; i++){
            builder.putElement(i,borderElement);
        }

    }

    @Override
    public void accept(Page page) {
        if(page.getTicks() > 20 * 5){
            page.getObserver().closeInventory();
        }
        if(page.getTicks() % 20 == 19) {
            page.getObserver().playSound(SoundTypes.UI_BUTTON_CLICK,page.getObserver().getLocation().getPosition(),0.5);
            for (Inventory slot : page.getPageView().slots()) {
                slot.set(ItemStack.of(ItemTypes.STAINED_GLASS_PANE,(int)Math.floor(page.getTicks()/20)));
            }
        }
    }

    public static class Config extends ViewConfig {
        public Config(ConfigurationNode node){
            super(node);
        }
    }
}
