package com.codehusky.huskycrates.crate;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class CrateListeners {

    @Listener
    public void openCrateBlock(InteractBlockEvent.Secondary.MainHand event, @Root Player player){
        if(event.getTargetBlock().getLocation().isPresent()) {
            if(HuskyCrates.registry.isPhysicalCrate(event.getTargetBlock().getLocation().get())){
                event.setCancelled(true);
                HuskyCrates.registry.getPhysicalCrate(event.getTargetBlock().getLocation().get())
                        .getCrate()
                        .launchView(player);
            }
        }
    }

    @Listener
    public void openCratePreviewBlock(InteractBlockEvent.Primary.MainHand event){

    }

    @Listener
    public void openCrateEntity(InteractEntityEvent.Secondary.MainHand event){
        System.out.println(event.getTargetEntity());
    }

    @Listener
    public void openCratePreviewEntity(InteractEntityEvent.Primary.MainHand event){

    }

    @Listener
    public void placeCrate(ChangeBlockEvent.Place event){

        for(Transaction<BlockSnapshot> transaction : event.getTransactions()){
            Optional<Location<World>> pLocation = transaction.getFinal().getLocation();

            if(pLocation.isPresent()){
                Location<World> location = pLocation.get();

                //TODO: Change logic to be non-placeholder
                if(transaction.getFinal().getState().getType() == BlockTypes.CHEST){
                    HuskyCrates.registry.registerPhysicalCrate(new PhysicalCrate(location,"testCrate"));
                }
            }

        }

    }
}
