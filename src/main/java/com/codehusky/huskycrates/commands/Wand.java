package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.HuskyCrates;
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


public class Wand implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if(commandContext.getOne("type").isPresent() && commandSource instanceof Player) {
            String type = commandContext.<String>getOne("type").get();
            Player player = (Player) commandSource;
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Please check your config."));
                return CommandResult.empty();
            }



            ItemStack keyItemStack = virtualCrate.getCrateWand();
            InventoryTransactionResult.Type mainInventory = player.getInventory().offer(keyItemStack.copy()).getType();
            if (!mainInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                InventoryTransactionResult.Type enderInventory = player.getEnderChestInventory().offer(keyItemStack.copy()).getType();
                if(!enderInventory.equals(InventoryTransactionResult.Type.SUCCESS)) {
                    commandSource.sendMessage(Text.of("Couldn't give wand to " + player.getName() + " because of a full inventory and enderchest"));
                    HuskyCrates.instance.logger.info("Couldn't give wand to " + player.getName() + " because of a full inventory and enderchest");
                }else{
                    player.sendMessage(Text.of("You have been given a ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName) ," wand, but it has been placed in your Ender Chest."));
                }
            }
        }else{
            commandSource.sendMessage(Text.of("Usage: /crate wand <id>"));
        }
        return CommandResult.success();
    }
}