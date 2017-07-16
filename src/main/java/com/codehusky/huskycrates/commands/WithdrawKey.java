package com.codehusky.huskycrates.commands;

import com.codehusky.huskycrates.crate.VirtualCrate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import com.codehusky.huskycrates.HuskyCrates;

/**
 * Created By KasperFranz.
 *
 * This CommandExecutor is used to get the crate item.
 */
public class WithdrawKey implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if(commandContext.getOne("type").isPresent()) {
            if (!(commandSource instanceof Player)) {
                commandSource.sendMessage(Text.of("You need to be in game or specify a player for this command to work."));
                return CommandResult.empty();
            }
            Player player = (Player) commandSource;
            String type = commandContext.<String>getOne("type").get();
            VirtualCrate virtualCrate = HuskyCrates.instance.getCrateUtilities().getVirtualCrate(type);
            int quantity = commandContext.getOne("quantity").isPresent() ? commandContext.<Integer>getOne("quantity").get() : 1;
            if (virtualCrate == null) {
                commandSource.sendMessage(Text.of("Invalid crate id: " + type + ". Try using tab auto completion."));
                return CommandResult.empty();
            }
            int balance = virtualCrate.getVirtualKeyBalance(player);
            if(balance >= quantity && quantity > 0){
                ItemStack key = virtualCrate.getCrateKey(quantity);
                virtualCrate.takeVirtualKey(player, quantity);
                player.getInventory().offer(key);
                commandSource.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        virtualCrate.getLangData().formatter(virtualCrate.getLangData().withdrawSuccess,null,player,virtualCrate,null,null,quantity)
                ));
            }else{
                if(quantity <= 0){
                    player.sendMessage(Text.of(TextColors.RED, "Positive integer amounts only."));
                }else {
                    commandSource.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                            virtualCrate.getLangData().formatter(virtualCrate.getLangData().withdrawInsufficient,null,player,virtualCrate,null,null,quantity)
                    ));
                }
            }
        }else{
            commandSource.sendMessage(Text.of("Usage: /crate withdraw <id> [count]"));
        }
        return CommandResult.success();
    }
}