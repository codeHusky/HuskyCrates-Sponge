package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class Crate implements CommandExecutor {
    private HuskyCrates plugin;
    public Crate(HuskyCrates ins){
        plugin = ins;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player plr = (Player) src;
            plr.getInventory().offer(ItemStack.builder()
            .itemType(ItemTypes.CHEST)
            .quantity(1)
            .add(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize("☼1☼2☼3HUSKYCRATE-mining")).build());
        }
        return CommandResult.success();
    }
}
