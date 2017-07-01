package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;

public class InstantView extends CrateView {
    public InstantView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.plugin = plugin;
        ourplr = runner;
        vc = virtualCrate;
        items = vc.getItemSet();
        if(virtualCrate.scrambleRewards){
            scrambleRewards();
        }
        try {
            CrateReward rewardHolder = (CrateReward)vc.getItemSet().get(itemIndexSelected())[1];
            handleReward(rewardHolder);
            ourplr.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP,ourplr.getLocation().getPosition(),1);
        } catch (RandomItemSelectionFailureException e) {
            plugin.logger.error("Failed to load instant view item: " + vc.displayName);
        }
    }
}
