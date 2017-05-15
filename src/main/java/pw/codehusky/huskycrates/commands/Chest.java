package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.util.Optional;

public class Chest implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        String type = commandContext.<String>getOne("type").get();
        Optional<Player> player = commandContext.getOne("player");
        VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
        int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
        if (virtualCrate == null) {
            HuskyCrates.instance.logger.info("Invalid crate id. Please check your config.");
            return CommandResult.empty();
        }

        if (!player.isPresent()) {
            commandSource.sendMessage(Text.of("You need to either specify a player or be in game"));
            return CommandResult.empty();
        }

        ItemStack chestItemStack = virtualCrate.getCrateItem(quantity);
        player.get().getInventory().offer(chestItemStack);

        return CommandResult.success();

    }
}
