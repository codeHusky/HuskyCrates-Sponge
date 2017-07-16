package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import com.codehusky.huskycrates.HuskyCrates;

import java.util.Optional;

/**
 * Created By KasperFranz.
 *
 * This CommandExecutor is used to get the crate item.
 */
public class KeyBal implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        Optional<User> user = commandContext.getOne("player");

        if (!user.isPresent()) {
            commandSource.sendMessage(Text.of("You need to be in game or specify a player for this command to work."));
            return CommandResult.empty();
        }
        if (commandSource.hasPermission("huskycrates.keybal.others") && user.get() != commandSource) {
            commandSource.sendMessage(Text.of(TextColors.GREEN,user.get().getName() + "'s Key Balance"));
        }else{
            commandSource.sendMessage(Text.of(TextColors.GREEN,"Your Key Balance"));
        }
        boolean atleastOne = false;
        for(String vcid : HuskyCrates.instance.crateUtilities.getCrateTypes()) {
            VirtualCrate vc = HuskyCrates.instance.crateUtilities.getVirtualCrate(vcid);
            int keys = vc.getVirtualKeyBalance(user.get());
            if(keys > 0){
                atleastOne = true;
                commandSource.sendMessage(Text.of("  ", TextSerializers.FORMATTING_CODE.deserialize(vc.displayName), ": " + keys + " (id: " + vc.id + ") "));
            }
        }
        if(!atleastOne){
            if (commandSource.hasPermission("huskycrates.keybal.others") && user.get() != commandSource) {
                commandSource.sendMessage(Text.of(TextColors.GRAY, TextStyles.ITALIC,user.get().getName() + " currently has no virtual keys."));
            }else{
                commandSource.sendMessage(Text.of(TextColors.GRAY, TextStyles.ITALIC,"You currently have no virtual keys."));
            }

        }
        return CommandResult.success();
    }
}