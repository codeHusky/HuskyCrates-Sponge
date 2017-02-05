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
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.CrateCommandSource;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class CSGOCrateView implements CrateView {
    private HuskyCrates plugin;

    Integer offset = null;
    int itemNum = -1;
    private ArrayList<Object[]> items;
    private Inventory disp;
    private Task updater;
    private Player ourplr;
    private VirtualCrate vc;
    private int clicks = 0;
    private int maxTicks = 45; // maximum times the spinner "clicks" in one spin
    public CSGOCrateView(HuskyCrates plugin,Player runner, VirtualCrate virtualCrate){
        this.vc = virtualCrate;
        ourplr = runner;
        this.plugin = plugin;

        items = virtualCrate.getItemSet();
        //offsetBase = (int)Math.floor(gg);
        float random = new Random().nextFloat()*vc.getMaxProb();
        System.out.println(vc.getMaxProb() + " - " + vc.displayName);
        float cummProb = 0;
        for(int i = 0; i < items.size(); i++){
            cummProb += (float)items.get(i)[0];
            if(random <= cummProb && offset == null){
                System.out.println(((ItemStack)items.get(i)[1]).get(Keys.DISPLAY_NAME));
                offset = i;
                clicks = -maxTicks + i;
                itemNum = i;
            }
        }
        if(offset == null){
            System.out.println("--------------------------------");
            System.out.println("--------------------------------");
            System.out.println("ERROR WHEN INITING PROBABILITY FOR " + vc.displayName);
            System.out.println("--------------------------------");
            System.out.println("--------------------------------");
        }
        disp = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .listener(ClickInventoryEvent.class,evt -> evt.setCancelled(true))
                .property(InventoryTitle.PROPERTY_NAME,InventoryTitle.of(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(virtualCrate.displayName)))
                .build(plugin);
        updateInv(0);
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        updater = taskBuilder.execute(this::updateTick).intervalTicks(1).submit(plugin);


    }
    private String commandToRun = "";
    private boolean runCmd = false;
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
                int itemNum = Math.abs(((slotnum - 10) + (clicks-3)) % items.size());
                e.set((ItemStack)items.get(itemNum)[1]);
                if(slotnum == 13) {
                    giveToPlayer = ((ItemStack)items.get(itemNum)[1]).copy();
                    if(items.get(itemNum).length == 3){
                        runCmd = true;
                        commandToRun = items.get(itemNum)[2].toString();
                    }else{
                        runCmd = false;
                        commandToRun = "";
                    }
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
    private int tickerState = 0;
    private void updateTick() {
        //revDampening = 1.15;
        waitCurrent++;
        //int revolutions = (int) Math.floor(clicks / items.size());
        //once clicks is greater than offset we stop the spinner
        if (waitCurrent == Math.round(updateMax) && clicks < offset && tickerState == 0) {
            //System.out.println(clicks + " : " + offset);

            waitCurrent = 0;
            updateMax *= dampening;
            updateInv(-1);
            ourplr.playSound(SoundTypes.UI_BUTTON_CLICK,ourplr.getLocation().getPosition(),0.25);
            clicks++;
        }else if(clicks >= offset && updateMax != 100 && tickerState == 0){
            tickerState = 1;
            ourplr.playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH,ourplr.getLocation().getPosition(),1);
            updateMax = 100;
            waitCurrent = 0;
        }else if(tickerState == 1){
            if (waitCurrent == Math.round(updateMax)) {
                updater.cancel();
                ourplr.closeInventory(plugin.genericCause);

                Text name = Text.of(TextColors.YELLOW, giveToPlayer.createSnapshot().getType().getTranslation().get());
                if(giveToPlayer.get(Keys.DISPLAY_NAME).isPresent()){
                    name = Text.of(TextStyles.ITALIC,giveToPlayer.get(Keys.DISPLAY_NAME).get());
                }
                
                if(runCmd)
                    Sponge.getCommandManager().process(new CrateCommandSource(),commandToRun.replace("%p",ourplr.getName()));

                if(giveToPlayer.getQuantity() != 1 && !runCmd){
                    ourplr.sendMessage(Text.of("You won ",TextColors.YELLOW, giveToPlayer.getQuantity() + " ",  name, TextColors.RESET, " from a ", TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName),TextColors.RESET,"!"));
                }else{
                    String[] vowels = {"a","e","i","o","u"};
                    if(Arrays.asList(vowels).contains(name.toPlain().substring(0,1).toLowerCase())){
                        ourplr.sendMessage(Text.of("You won an ", name, TextColors.RESET, " from a ", TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName), TextColors.RESET, "!"));
                    }else {
                        ourplr.sendMessage(Text.of("You won a ", name, TextColors.RESET, " from a ", TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName), TextColors.RESET, "!"));
                    }
                }
                if(!runCmd)
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
