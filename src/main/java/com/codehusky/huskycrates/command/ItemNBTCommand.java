package com.codehusky.huskycrates.command;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class ItemNBTCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player player = (Player)src;
            if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
                ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
                src.sendMessage(Text.of(TextColors.GRAY,"---- ITEM NBT ----"));
                String beep = null;
                try {
                    beep = DataFormats.HOCON.write((DataView)stack.toContainer().get(DataQuery.of("UnsafeData")).get());
                    src.sendMessage(Text.of(TextSerializers.LEGACY_FORMATTING_CODE.replaceCodes(beep,'&')));
                } catch (IOException e) {
                    e.printStackTrace();
                    src.sendMessage(Text.of(TextColors.RED,"No custom NBT is present on this item."));
                }

                src.sendMessage(Text.of(TextColors.GRAY,"---- ITEM NBT ----"));
            }else{
                src.sendMessage(Text.of(TextColors.RED,"You must be holding an item to use this command."));
            }
        }else{
            src.sendMessage(Text.of(TextColors.RED,"You must be a player to use this command."));
        }

        return CommandResult.success();
    }
}
