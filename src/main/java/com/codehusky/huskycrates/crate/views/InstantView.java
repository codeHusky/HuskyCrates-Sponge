package com.codehusky.huskycrates.crate.views;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;

public class InstantView extends CrateView {

	public InstantView(HuskyCrates plugin, Player viewer, VirtualCrate virtualCrate) {
		super(viewer, null, null);

		this.plugin = plugin;
		vc = virtualCrate;
		items = vc.getItemSet();

		if (virtualCrate.scrambleRewards) {
			scrambleRewards();
		}

		try {
			CrateReward rewardHolder = (CrateReward) vc.getItemSet().get(itemIndexSelected())[1];
			handleReward(rewardHolder);
			viewer.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, viewer.getLocation().getPosition(), 1);
		} catch (RandomItemSelectionFailureException e) {
			plugin.logger.error("Failed to load instant view item: " + vc.displayName);
		}
	}

	@Override
	public void updateTick() {
	}
}