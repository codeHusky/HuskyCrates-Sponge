package pw.codehusky.huskycrates.crate;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.CSGOCrateView;

/**
 * Created by lokio on 12/28/2016.
 */
public class CrateUtilities {
    public static void launchCrateForPlayer(String crateType, Player target,HuskyCrates plugin){
        CSGOCrateView view = new CSGOCrateView(plugin,target);
        target.openInventory(view.getInventory(),plugin.genericCause);
    }
    public static ItemStack getCrateItemStack(String crateType){
        return null;
    }
}
