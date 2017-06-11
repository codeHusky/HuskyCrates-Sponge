package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.util.Optional;

/**
 * Created By KasperFranz.
 *
 * This CommandExecutor is used to get the crate item.
 */
public class VirtualKey implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if(commandContext.getOne("type").isPresent()) {
            String type = commandContext.<String>getOne("type").get();
            Optional<User> player = commandContext.getOne("player");
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Please check your config."));
                return CommandResult.empty();
            }

            if (!player.isPresent()) {
                commandSource.sendMessage(Text.of("You need to be in game or specify a player for this command to work."));
                return CommandResult.empty();
            }


            HuskyCrates.instance.crateUtilities.giveVirtualKeys(player.get(),virtualCrate,quantity);
            commandSource.sendMessage(Text.of("Gave " + player.get().getName() + " " + quantity + " vkeys."));
            if(commandSource != player.get() && player.get() instanceof Player) {
                ((Player)player.get()).sendMessage(Text.of(TextColors.GREEN,"You received " + quantity + " virtual keys for a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName),"."));
            }
        }else{
            commandSource.sendMessage(Text.of("Usage: /crate vkey <id> [player] [count]"));
        }
        return CommandResult.success();
    }
}