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
        Optional<String> key = commandContext.getOne("key");
        VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);

        if (virtualCrate == null) {
            HuskyCrates.instance.logger.info("Invalid crate id. Please check your config.");
            return CommandResult.empty();
        }

        // TODO: THIS SHOULD BE REMOVED SOON
        if (key.isPresent()) {
            HuskyCrates.instance.logger.info("---------------------------------");
            HuskyCrates.instance.logger.info("Please change the husky command from /crate " + type + " key to be in the new format.");
            HuskyCrates.instance.logger.info("/crate key " + type + " [player]  - the old format will be removed soon!!!!");
            HuskyCrates.instance.logger.info("---------------------------------");

            if (!player.isPresent()) {
                commandSource.sendMessage(Text.of("You need to be in game or specify a player for this command to work."));
                return CommandResult.empty();
            }

            ItemStack keyItemStack = virtualCrate.getCrateKey(1);

            if (!player.get().getInventory().offer(keyItemStack.copy()).getType().equals(InventoryTransactionResult.Type.SUCCESS) &&
                    !player.get().getEnderChestInventory().offer(keyItemStack.copy()).getType().equals(InventoryTransactionResult.Type.SUCCESS)) {
                HuskyCrates.instance.logger
                        .info("Couldn't give key to " + player.get().getName() + " because of a full inventory and enderchest");
            }
            return CommandResult.success();
        }
        // TODO: THIS SHOULD BE REMOVED SOON END

        if (!player.isPresent()) {
            commandSource.sendMessage(Text.of("You need to either specify a player or be in game"));
            return CommandResult.empty();
        }


        ItemStack chestItemStack = virtualCrate.getCrateItem();
        player.get().getInventory().offer(chestItemStack);

        return CommandResult.success();

    }
}
