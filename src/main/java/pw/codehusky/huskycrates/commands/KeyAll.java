package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;


public class KeyAll implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if(commandContext.getOne("type").isPresent()) {
            String type = commandContext.<String>getOne("type").get();
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Please check your config."));
                return CommandResult.empty();
            }


            for(Player player : Sponge.getServer().getOnlinePlayers()) {
                ItemStack keyItemStack = virtualCrate.getCrateKey(quantity);
                InventoryTransactionResult.Type mainInventory = player.getInventory().offer(keyItemStack.copy()).getType();
                if (!mainInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                    InventoryTransactionResult.Type enderInventory = player.getEnderChestInventory().offer(keyItemStack.copy()).getType();
                    if (!enderInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                        commandSource.sendMessage(Text.of("Couldn't give key to " + player.getName() + " because of a full inventory and enderchest"));
                        HuskyCrates.instance.logger.info("Couldn't give key to " + player.getName() + " because of a full inventory and enderchest");
                    } else {
                        player.sendMessage(Text.of("You have been given 1 or more ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName), " key(s), but some have been placed in your Ender Chest."));
                    }
                }
            }
        }else{
            commandSource.sendMessage(Text.of("Usage: /crate keyall <id> [count]"));
        }
        return CommandResult.success();
    }
}