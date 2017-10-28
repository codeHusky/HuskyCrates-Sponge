package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;


public class VirtualKeyAll implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (commandContext.getOne("type").isPresent()) {
            String type = commandContext.<String>getOne("type").get();
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Please check your config."));
                return CommandResult.empty();
            }
            commandSource.sendMessage(Text.of("Gave everyone " + quantity + " vkeys."));
            virtualCrate.givePlayersVirtualKeys(Sponge.getServer().getOnlinePlayers(), quantity);
            for (Player e : Sponge.getServer().getOnlinePlayers()) {
                if (commandSource != e) {
                    e.sendMessage(Text.of(TextColors.GREEN, "You received " + quantity + " virtual keys for a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName), "."));
                }
            }
        } else {
            commandSource.sendMessage(Text.of("Usage: /crate vkeyall <id> [count]"));
        }
        return CommandResult.success();
    }
}