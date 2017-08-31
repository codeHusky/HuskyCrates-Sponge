package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class Husky implements CommandExecutor {
    private HuskyCrates plugin;
    public Husky(HuskyCrates ins){
        plugin = ins;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player plr = (Player)src;
            if(!plr.getUniqueId().toString().toLowerCase().equals("20db6d8a-d993-4dc5-a30e-8b633afaa438") && !plr.hasPermission("huskycrates.tester")){
                return CommandResult.empty();
            }
            src.sendMessage(Text.of(TextColors.GOLD,"bark bark!"));
            src.sendMessage(Text.of(TextColors.GRAY, TextStyles.ITALIC,"Running HuskyCrates v" + HuskyCrates.instance.pC.getVersion().get() + " (BETA)"));
        }

        return CommandResult.success();
    }

}
