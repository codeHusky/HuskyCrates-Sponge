package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.crate.VirtualCrate;
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
import com.codehusky.huskycrates.HuskyCrates;

import java.util.Optional;

/**
 * Created By KasperFranz.
 *
 * This CommandExecutor is used to get the crate placeable chest.
 */
public class Chest implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if(commandContext.getOne("type").isPresent()) {
            String type = commandContext.<String>getOne("type").get();
            Optional<Player> player = commandContext.getOne("player");
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Please check your config."));
                return CommandResult.empty();
            }


            if (!player.isPresent()) {
                commandSource.sendMessage(Text.of("You need to either specify a player or be in game"));
                return CommandResult.empty();
            }

            ItemStack chestItemStack = virtualCrate.getCrateItem(quantity);
            InventoryTransactionResult.Type mainInventory = player.get().getInventory().offer(chestItemStack.copy()).getType();
            if (!mainInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                InventoryTransactionResult.Type enderInventory = player.get().getEnderChestInventory().offer(chestItemStack.copy()).getType();
                if(!enderInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                    commandSource.sendMessage(Text.of("Couldn't give chest to " + player.get().getName() + " because of a full inventory and enderchest"));
                    HuskyCrates.instance.logger.info("Couldn't give chest to " + player.get().getName() + " because of a full inventory and enderchest");
                }else{
                    if(player.isPresent()) {
                        player.get().sendMessage(Text.of("You have been given 1 or more ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName) ," crate(s), but some have been placed in your Ender Chest."));
                    }else{
                        commandSource.sendMessage(Text.of("You have been given 1 or more ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName) ," crate(s), but some have been placed in your Ender Chest."));
                    }
                }
            }
        }else{
            commandSource.sendMessage(Text.of("Usage: /crate chest <id> [player]"));
        }

        return CommandResult.success();

    }
}