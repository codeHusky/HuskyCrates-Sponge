package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class CrateListeners {

    @Listener
    public void openCrateBlock(InteractBlockEvent.Secondary.MainHand event, @Root Player player){
        //TODO: Move a lot of this logic to a common method so that we don't repeat code.
        if(event.getTargetBlock().getLocation().isPresent()) {
            if(HuskyCrates.registry.isPhysicalCrate(event.getTargetBlock().getLocation().get())){
                event.setCancelled(true);
                Optional<ItemStack> pItemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
                if(pItemInHand.isPresent()) {
                    String keyID = Key.extractKeyId(pItemInHand.get());
                    if(keyID != null && HuskyCrates.registry.isKey(keyID)) {
                        if(HuskyCrates.registry.getKey(keyID).testKey(pItemInHand.get())) {
                            PhysicalCrate physicalCrate = HuskyCrates.registry.getPhysicalCrate(event.getTargetBlock().getLocation().get());
                            if(physicalCrate.getCrate().testKey(pItemInHand.get())) {
                                player.playSound(SoundTypes.BLOCK_WOOD_BUTTON_CLICK_OFF, player.getPosition(), 1.0);
                                physicalCrate.getCrate().launchView(player);
                                return;
                            }
                        }
                    }
                }
                player.playSound(SoundTypes.ENTITY_CREEPER_DEATH,player.getPosition(),1.0);
                player.spawnParticles(ParticleEffect.builder().type(ParticleTypes.SMOKE).quantity(20).offset(new Vector3d(0.1,0.3,0.1)).build(),
                        event.getTargetBlock().getPosition().clone().toDouble().add(0.5,1.3,0.5));
                player.sendMessage(Text.of("You need a key to open this crate. ;)"));
            }
        }
    }

    @Listener
    public void openCratePreviewBlock(InteractBlockEvent.Primary.MainHand event, @Root Player player){

    }

    @Listener
    public void openCrateEntity(InteractEntityEvent.Secondary.MainHand event, @Root Player player){
        System.out.println(event.getTargetEntity());
    }

    @Listener
    public void openCratePreviewEntity(InteractEntityEvent.Primary.MainHand event, @Root Player player){

    }

    @Listener
    public void placeCrate(ChangeBlockEvent.Place event, @Root Player player){
        if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                Optional<Location<World>> pLocation = transaction.getFinal().getLocation();

                if (pLocation.isPresent()) {
                    Location<World> location = pLocation.get();
                    String pID = Crate.extractCrateID(player.getItemInHand(HandTypes.MAIN_HAND).get());
                    if(pID != null && HuskyCrates.registry.isCrate(pID)) {
                        if(!player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getBlock().isPresent()) return;
                        if (player.getItemInHand(HandTypes.MAIN_HAND).get().getType().getBlock().get().equals(transaction.getFinal().getState().getType())) {
                            HuskyCrates.registry.registerPhysicalCrate(new PhysicalCrate(location, pID));
                        }
                    }
                }

            }
        }

    }

    @Listener
    public void crateBlockDestroyed(ChangeBlockEvent event){
        if(event instanceof ChangeBlockEvent.Place || event instanceof ChangeBlockEvent.Post) return;

        for(Transaction<BlockSnapshot> trans : event.getTransactions()){
            BlockSnapshot original = trans.getOriginal();
            BlockSnapshot after = trans.getFinal();

            if(original.getLocation().isPresent()){
                if(HuskyCrates.registry.isPhysicalCrate(original.getLocation().get())){
                    if(!original.getState().getType().equals(after.getState().getType())){
                        HuskyCrates.registry.unregisterPhysicalCrate(original.getLocation().get());
                    }
                }
            }
        }
    }
}
