package com.codehusky.huskycrates.crate.virtual.views;

import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.element.Element;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

public class SimpleView implements Consumer<Page> {
    private Location<World> physicalLocation;
    private Crate crate;
    private int selectedSlot;
    private Player player;
    private ViewConfig config;

    public SimpleView(PhysicalCrate pcrate, Player player){
        this.crate = pcrate.getCrate();
        this.physicalLocation = pcrate.getLocation();
        this.config = crate.getViewConfig();
        this.selectedSlot = crate.selectSlot();
        this.player = player;
        player.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH, player.getPosition(), 0.5);
        Page.PageBuilder builder =
            Page.builder()
                .setAutoPaging(false)
                .setTitle(TextSerializers.FORMATTING_CODE.deserialize(crate.getName()))
                .setUpdatable(true)
                .setUpdater(this)
                .setInterrupt(() -> {
                    crate.getSlot(selectedSlot).rewardPlayer(player,this.physicalLocation);
                    player.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation().getPosition(), 0.5);
                })
                .setInventoryDimension(InventoryDimension.of(3,3))
                .setInventoryArchetype(InventoryArchetypes.DISPENSER);

        for(int i = 0; i < 3*3; i++){
            builder.putElement(i,new Element((i == 4)? crate.getSlot(selectedSlot).getDisplayItem().toItemStack() : getConfetti()));
        }
        Page page = builder.build("meme");
        StateContainer sc = new StateContainer();
        sc.setInitialState(page);
        sc.launchFor(player);
    }

    private ItemStack getConfetti() {
        DyeColor[] colors = {DyeColors.BLUE,DyeColors.CYAN,DyeColors.LIGHT_BLUE,DyeColors.LIME,DyeColors.MAGENTA,DyeColors.ORANGE,DyeColors.PINK,DyeColors.PURPLE,DyeColors.RED, DyeColors.YELLOW};
        ItemStack g =ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR,colors[(int)Math.floor(Math.random() * colors.length)])
                .build();
        g.offer(Keys.DISPLAY_NAME, Text.of(TextStyles.RESET,"You win!"));
        return g;
    }
    private boolean safetyKill = false;
    @Override
    public void accept(Page page) {
        int count = 0;
        for(Slot e : page.getPageView().<Slot>slots()){
            if(count != 4 && page.getTicks() % 5 == 0)
                e.set(getConfetti());
            count ++;
        }
        if(page.getTicks() > 20*3){
            if(!safetyKill) {
                page.getObserver().closeInventory();
                safetyKill = true;
                page.interrupt();
            }
        }
    }

//    public static class Config extends ViewConfig {
//        private Item highlightItem;
//        public Config(ConfigurationNode node){
//            super(node);
//            if(!node.getNode("highlightItem").isVirtual()) {
//                this.highlightItem = new Item(node.getNode("highlightItem"));
//            }else{
//                this.highlightItem = new Item("&6HuskyCrates", ItemTypes.STAINED_GLASS_PANE,null,1,7,null,null,null);
//            }
//
//        }
//
//        public Item getHighlightItem() {
//            return highlightItem;
//        }
//    }
}
