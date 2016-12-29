package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.CSGOCrateView;

/**
 * Created by lokio on 12/28/2016.
 */
public class Crate implements CommandExecutor {
    private HuskyCrates plugin;
    public Crate(HuskyCrates ins){
        plugin = ins;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player plr = (Player) src;
            CSGOCrateView view = new CSGOCrateView(plugin,plr);
            plr.openInventory(view.getInventory(),plugin.genericCause);
        }
        return CommandResult.success();
    }
}
