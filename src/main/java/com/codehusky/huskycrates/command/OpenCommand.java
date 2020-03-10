package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.common.Checks;
import com.codehusky.huskycrates.crate.virtual.Crate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class OpenCommand implements CommandExecutor {
    private Checks commonChecks = new Checks();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Crate crate = args.<Crate>getOne(Text.of("crate")).get();
        Optional<Player> otherPlayer = args.getOne(Text.of("player"));
        boolean keyLess = args.getOne(Text.of("keyless")).isPresent();
        Optional<String> all = args.getOne(Text.of("all"));

        Crate crateU = HuskyCrates.registry.getCrate(crate.getId());

        if (src instanceof Player || otherPlayer.isPresent() || all.isPresent()){
            if(keyLess && !src.hasPermission("huskycrates.open.keyless")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to open crates without consuming keys!"));
                return CommandResult.success();
            }

            Player playerToOpen = null;
            if(otherPlayer.isPresent() && !src.hasPermission("huskycrates.open.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to open the crate for others!"));
                return CommandResult.success();
            }else {
                if(otherPlayer.isPresent()){
                    playerToOpen = otherPlayer.get();
                }
                if(!otherPlayer.isPresent() && !all.isPresent()){
                    playerToOpen = (Player) src;
                }
            }
            if(all.isPresent() && !src.hasPermission("huskycrates.open.others.all")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to open the crate for all players!"));
                return CommandResult.success();
            }
            else if(all.isPresent()){
                for(Player p : Sponge.getServer().getOnlinePlayers()){
                    if (keyLess){
                        crateU.launchView(crateU, p, p.getLocation());
                    }
                    else{
                        commonChecks.tryCrateFromCrate(crateU, p);
                    }
                }
                return CommandResult.success();
            }
            else if(keyLess){
                crateU.launchView(crateU,playerToOpen, playerToOpen.getLocation());
                return CommandResult.success();
            }
            commonChecks.tryCrateFromCrate(crateU,playerToOpen);
        }
        else {
        src.sendMessage(Text.of(TextColors.RED,"This command must be run by a player, otherwise a player name must be specified!"));
        }
    return CommandResult.success();
    }
}
