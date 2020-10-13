package com.codehusky.huskycrates.crate.virtual.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Item;
import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.element.Element;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Random;
import java.util.function.Consumer;

public class SpinnerView implements Consumer<Page> {
    private Location<World> physicalLocation;
    private Crate crate;
    private int selectedSlot;
    private Player player;
    private Config config;

    public SpinnerView(Crate ucrate, Player player, Location loc){
        this.crate = ucrate;
        if(this.crate.isScrambled()){
            this.crate = ucrate.getScrambledCrate();
        }
        this.physicalLocation = loc;
        this.config = (Config) crate.getViewConfig();
        this.variance = (int)Math.round(new Random().nextDouble() * config.getTicksToSelectionVariance());
        this.selectedSlot = crate.selectSlot();
        this.player = player;
        player.playSound(SoundTypes.BLOCK_WOOD_BUTTON_CLICK_OFF, player.getPosition(), 1.0);
        Page.PageBuilder builder =
            Page.builder()
                .setAutoPaging(false)
                .setTitle(TextSerializers.FORMATTING_CODE.deserialize(crate.getName()))
                .setUpdatable(true)
                .setUpdater(this)
                    //.setUpdateTickRate(5)

                .setInterrupt(() -> {
                    if(rewardGiven) return;
                    try {
                        crate.getSlot(selectedSlot).rewardPlayer(player, this.physicalLocation);
                        player.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, player.getLocation().getPosition(), 0.5);
                    }catch (Exception e){
                        e.printStackTrace();
                        HuskyCrates.instance.logger.error("Error occurred while trying to reward player.");
                        player.sendMessage(Text.of(TextColors.RED,"A fatal exception has occurred while delivering your reward. Please contact server administration."));
                    }
                    rewardGiven = true;
                })
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
    boolean rewardGiven = false;

    boolean hasWon = false;
    boolean safetyKill = false;
    long tickWinBegin = 0;

    private long getTicksToSelection() {
        return config.getTicksToSelection() + variance;
    }

    private boolean winCondition() {
        return spinnerOffset > getTicksToSelection();
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

    @Override
    public void accept(Page page) {
        if(winCondition() && !hasWon){
            hasWon = true;
            tickWinBegin = page.getTicks();
        }
        if(!hasWon) {
            int num = 0;
            for (Inventory slot : page.getPageView().slots()) {
                if (num >= 10 && num <= 16) {
                    int spinnerSlotAffected = (num-10);
                    int slotSelected = Math.max(0,(
                            /* Buffer and Centering*/
                            (spinnerOffset + spinnerSlotAffected - 3 + crate.getSlotCount()*5)
                    ) );
                    //offset to fit
                    slotSelected+= selectedSlot - (getTicksToSelection()%crate.getSlotCount());
                    //wrap
                    slotSelected = slotSelected % crate.getSlotCount();
                    if(currentTicks >= currentTickDelay && num == 13){
                        //System.out.println(slotSelected + " should be " + selectedSlot + " (" + ((config.getTicksToSelection() + variance ) - spinnerOffset) + " ticks remain) (" + spinnerOffset + ")");
                    }
                    slot.set(
                            //(spinner offset + (a buffer to prevent neg numbers + (sel slot + 1 offset) - 3 for centering) + (slotnum rel to center) % slot count
                            crate.getSlot(slotSelected)
                                    .getDisplayItem()
                                    .toItemStack()
                    );
                }
                num++;
            }

            if (currentTicks >= currentTickDelay) {
                currentTicks = 0;
                currentTickDelay *= config.getTickDelayMultiplier();
                spinnerOffset++;
                page.getObserver().playSound(
                        (winCondition())?
                                SoundTypes.ENTITY_FIREWORK_LAUNCH:
                                SoundTypes.UI_BUTTON_CLICK, page.getObserver().getLocation().getPosition(), 0.5);
            }
            currentTicks++;
        }else{
            if(page.getTicks() % 5 == 0) {
                int num = 0;
                for (Inventory slot : page.getPageView().slots()) {
                    if (num != 13) {
                        slot.set(getConfetti());
                    }
                    num++;
                }
            }
            if(page.getTicks() > tickWinBegin + 20*3){
                if(!safetyKill) {
                    page.getObserver().closeInventory();
                    safetyKill = true;
                    if(page.hasInterupt()) {
                        page.interrupt();
                    }
                }
            }
        }
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
            this.tickDelayMultiplier = node.getNode("tickDelayMultiplier").getDouble(1.08);
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
