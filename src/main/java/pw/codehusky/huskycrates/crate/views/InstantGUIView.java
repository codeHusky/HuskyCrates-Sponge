package pw.codehusky.huskycrates.crate.views;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

public class InstantGUIView extends CrateView {
    private Inventory disp;
    public InstantGUIView(HuskyCrates plugin, Player runner, VirtualCrate virtualCrate){
        this.plugin = plugin;
        this.ourplr = runner;
        this.vc = virtualCrate;
    }
}
