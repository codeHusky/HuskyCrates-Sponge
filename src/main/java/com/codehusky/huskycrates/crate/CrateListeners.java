package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.common.Checks;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.event.CrateInjectionEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CrateListeners {
    Checks commonChecks = new Checks();

    @Listener
    public void openCrateBlock(InteractBlockEvent.Secondary.MainHand event, @Root Player player){
        //TODO: Move a lot of this logic to a common method so that we don't repeat code.
        if(event.getTargetBlock().getLocation().isPresent()) {
            if(HuskyCrates.registry.isPhysicalCrate(event.getTargetBlock().getLocation().get())){
                PhysicalCrate physicalCrate = HuskyCrates.registry.getPhysicalCrate(event.getTargetBlock().getLocation().get());
                event.setCancelled(true);

                commonChecks.tryCrateFromPhysicalCrate(physicalCrate,player);
            }
        }
    }


    @Listener
    public void openCratePreviewBlock(InteractBlockEvent.Primary.MainHand event, @Root Player player){
        if(event.getTargetBlock().getLocation().isPresent()) {
            if (HuskyCrates.registry.isPhysicalCrate(event.getTargetBlock().getLocation().get())) {
                PhysicalCrate physicalCrate = HuskyCrates.registry.getPhysicalCrate(event.getTargetBlock().getLocation().get());
                if(physicalCrate.getCrate().isPreviewable()){
                    if(!player.hasPermission("huskycrates.admin") || player.getOrNull(Keys.GAME_MODE) != GameModes.CREATIVE){
                        physicalCrate.getCrate().launchPreview(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @Listener
    public void openCrateEntity(InteractEntityEvent.Secondary.MainHand event, @Root Player player){
        //System.out.println(event.getTargetEntity());
    }

    @Listener
    public void openCratePreviewEntity(InteractEntityEvent.Primary.MainHand event, @Root Player player){

    }

    @Listener(order = Order.PRE)
    public void placeCrate(ChangeBlockEvent.Place event, @Root Player player){
        if(event.getContext().get(EventContextKeys.USED_ITEM).isPresent() || player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {

            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                Optional<Location<World>> pLocation = transaction.getFinal().getLocation();
                ItemStack stackUsed;
                if(event.getContext().get(EventContextKeys.USED_ITEM).isPresent()){
                    stackUsed = event.getContext().get(EventContextKeys.USED_ITEM).get().createStack();
                }else{
                    stackUsed = player.getItemInHand(HandTypes.MAIN_HAND).get().createSnapshot().createStack();
                }
                //System.out.println(event.getContext().get(EventContextKeys.USED_ITEM).get().toContainer());
                if (pLocation.isPresent()) {
                    Location<World> location = pLocation.get();
                    String pID = null;
                    try{
                        pID= Crate.extractCrateID(stackUsed);
                    }catch (Exception e){
                        return;
                    }
                    if(pID != null && HuskyCrates.registry.isCrate(pID)) {
                        if(!stackUsed.getType().getBlock().isPresent()) return;
                        if (stackUsed.getType().getBlock().get().equals(transaction.getFinal().getState().getType())) {
                            if(!player.hasPermission("huskycrates.admin")) {
                                event.setCancelled(true);
                                player.setItemInHand(HandTypes.MAIN_HAND,ItemStack.empty());
                                Util.alertAdmins(Text.of(TextColors.RED,"[HuskyCrates]",TextColors.YELLOW, " " + player.getName() + " tried to place a crate without permission."),"[PERM VIOLATION] " + player.getName() + " (" + player.getUniqueId().toString() + ") tried to place a crate without permission.");
                                return;
                            }
                            HuskyCrates.registry.registerPhysicalCrate(new PhysicalCrate(location, pID));
                        }
                    }
                }

            }
        }

    }

    @Listener
    public void crateBlockDestroyed(ChangeBlockEvent event){

        if(!(event instanceof ChangeBlockEvent.Break)) return;
        //System.out.println(event);

        for(Transaction<BlockSnapshot> trans : event.getTransactions()){
            BlockSnapshot original = trans.getOriginal();
            BlockSnapshot after = trans.getFinal();

            if(original.getLocation().isPresent()){
                if(HuskyCrates.registry.isPhysicalCrate(original.getLocation().get())){
                    if(event.getCause().root() instanceof Player){
                        Player plr = (Player)event.getCause().root();
                        if(!plr.hasPermission("huskycrates.admin")){
                            event.setCancelled(true);
                            return;
                        }
                    }
                    if(!original.getState().getType().equals(after.getState().getType())){
                        PhysicalCrate pc = HuskyCrates.registry.getPhysicalCrate(original.getLocation().get());
                        pc.cleanup();
                        HuskyCrates.registry.unregisterPhysicalCrate(original.getLocation().get());
                    }
                }
            }
        }
    }

    @Listener(order = Order.POST)
    public void keyInteract(InteractItemEvent.Secondary.MainHand event, @Root Player player){
        String keyid = Key.extractKeyId(event.getItemStack().createStack());
        if(keyid != null){
            if(HuskyCrates.registry.isKey(keyid)){
                Key key = HuskyCrates.registry.getKey(keyid);
                if(key.canLaunchCrate()){
                    commonChecks.handleKeyItem(new PhysicalCrate(null,key.crateToLaunch().getId(),false).getCrate(),event.getItemStack().createStack(),player);
                }
                //event.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.POST)
    public void afterInjection(CrateInjectionEvent event){
        HuskyCrates.registry.postInjection();
        HuskyCrates.instance.logger.info("Injection checks passed.");
    }
}