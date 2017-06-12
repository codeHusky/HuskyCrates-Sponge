package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

/**
 * Created By KasperFranz.
 *
 * This CommandExecutor is used to get the crate item.
 */
public class DepositKey implements CommandExecutor {

    @Override public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        if (!(commandSource instanceof Player)) {
            commandSource.sendMessage(Text.of("You need to be in game or specify a player for this command to work."));
            return CommandResult.empty();
        }
        Player plr = (Player)commandSource;
        if(!plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
            commandSource.sendMessage(Text.of("You must be holding an item to deposit a key."));
            return CommandResult.empty();
        }
        ItemStack key = plr.getItemInHand(HandTypes.MAIN_HAND).get();
        if(HuskyCrates.instance.crateUtilities.vcFromKey(key) == null){
            commandSource.sendMessage(Text.of(TextColors.RED,"Not a valid key."));
            return CommandResult.empty();
        }
        VirtualCrate virtualCrate = HuskyCrates.instance.crateUtilities.vcFromKey(plr.getItemInHand(HandTypes.MAIN_HAND).get());
        int keyCount = key.getQuantity();
        plr.setItemInHand(HandTypes.MAIN_HAND,null);
        HuskyCrates.instance.crateUtilities.giveVirtualKeys(plr,virtualCrate,keyCount);
        //commandSource.sendMessage(Text.of(TextColors.GREEN,"Successfully deposited " + keyCount + " ", TextSerializers.FORMATTING_CODE.deserialize(virtualCrate.displayName),TextColors.GREEN," Key(s)."));
        commandSource.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                virtualCrate.getLangData().formatter(virtualCrate.getLangData().depositSuccess,null,plr,virtualCrate,null,null,keyCount)
        ));
        return CommandResult.success();
    }
}