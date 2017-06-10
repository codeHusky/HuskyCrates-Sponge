package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.crate.config.CrateRewardHolder;
import pw.codehusky.huskycrates.exceptions.RandomItemSelectionFailureException;

public class InstantView extends CrateView {
    public InstantView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.plugin = plugin;
        ourplr = runner;
        vc = virtualCrate;
        items = vc.getItemSet();
        try {
            CrateRewardHolder rewardHolder = (CrateRewardHolder)vc.getItemSet().get(itemIndexSelected())[1];
            handleReward(rewardHolder);
            ourplr.playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP,ourplr.getLocation().getPosition(),1);
        } catch (RandomItemSelectionFailureException e) {
            plugin.logger.error("Failed to load instant view item: " + vc.displayName);
        }
    }
}
