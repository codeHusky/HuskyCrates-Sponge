package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.events.VKeyBalanceChangeEvent;
import org.spongepowered.api.Sponge;
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

import java.util.Optional;

/**
 * Created By KasperFranz.
 * <p>
 * This CommandExecutor is used to get the crate item.
 */
public class VirtualKey implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
		if (commandContext.getOne("operation").isPresent()) {
			if (commandContext.getOne("type").isPresent()) {
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
				Integer oldBal = virtualCrate.getVirtualKeyBalance(player.get());
				String operation = commandContext.<String>getOne("operation").get();
				if (operation.equalsIgnoreCase("add")) {
					virtualCrate.giveVirtualKeys(player.get(), quantity);
					commandSource.sendMessage(Text.of("Gave " + player.get().getName() + " " + quantity + " vkeys."));
					if (commandSource != player.get() && player.get() instanceof Player) {
						((Player) player.get()).sendMessage(Text.of(TextColors.GREEN, "You received " + quantity + " virtual keys for a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName), "."));
					}
				} else if (operation.equalsIgnoreCase("set")) {
					virtualCrate.setVirtualKeys(player.get(), quantity);
					commandSource.sendMessage(Text.of("Set " + player.get().getName() + " " + quantity + " vkeys."));
					if (commandSource != player.get() && player.get() instanceof Player) {
						((Player) player.get()).sendMessage(Text.of(TextColors.GREEN, "Your virtual key balance was set to " + quantity + " for a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName), "."));
					}
				} else if (operation.equalsIgnoreCase("remove")) {
					virtualCrate.takeVirtualKeys(player.get(), quantity);
					commandSource.sendMessage(Text.of("Took " + player.get().getName() + " " + quantity + " vkeys."));
					if (commandSource != player.get() && player.get() instanceof Player) {
						((Player) player.get()).sendMessage(Text.of(TextColors.GREEN, "You lost " + quantity + " virtual keys for a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName), "."));
					}
				} else {
					//invalid operation
				}
				fireEvent(player.get(), virtualCrate.id, oldBal, virtualCrate.getVirtualKeyBalance(player.get()));
			} else {
				printUsage(commandSource);
			}
		} else {
			printUsage(commandSource);
		}
		return CommandResult.success();
	}

	private void printUsage(CommandSource source) {
		source.sendMessage(Text.of("Usage: /crate vkey <add/set/remove> <id> [player] [count]"));
	}

	private void fireEvent(User user, String crateId, Integer oldBal, Integer newBal) {
		VKeyBalanceChangeEvent vKeyBalanceChangeEvent = new VKeyBalanceChangeEvent(user, crateId, oldBal, newBal);
		Sponge.getEventManager().post(vKeyBalanceChangeEvent);
	}
}