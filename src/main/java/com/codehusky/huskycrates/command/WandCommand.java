package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Crate;
import ninja.leaping.configurate.ConfigurationNode;
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

import java.util.Optional;

public class WandCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Crate crate = args.<Crate>getOne(Text.of("crate")).get();
        Optional<Player> otherPlayer = args.getOne(Text.of("player"));
        Player playerToGive = null;
        if(src instanceof Player)
            playerToGive = (Player) src;

        if(otherPlayer.isPresent() && !src.hasPermission("huskycrates.wand.others")){
            src.sendMessage(Text.of(TextColors.RED,"You do not have permission to give others crate wands."));
            return CommandResult.success();
        }
        if(otherPlayer.isPresent())
            playerToGive = otherPlayer.get();

        if(playerToGive == null){
            src.sendMessage(HuskyCrates.blockCommandMessages.getNoPlayersFound());
            return CommandResult.success();
        }

        ItemStack stack = crate.getWandItem();

        Util.getHotbarFirst(playerToGive.getInventory()).offer(stack);
        return CommandResult.success();
    }

    public static class Messages {
        private String noPlayersFound;
        private String itemOnlyFailure;
        public Messages(ConfigurationNode node){
            this.noPlayersFound = node.getNode("noPlayersFound").getString("&cNo valid players could be found to give a crate placement block to.");
            this.itemOnlyFailure = node.getNode("itemOnlyFailure").getString("&cThe block you supplied could not be converted to an item. Please try again with a different block.");
        }

        public Text getNoPlayersFound() {
            return TextSerializers.FORMATTING_CODE.deserialize(noPlayersFound);
        }

        public Text getItemOnlyFailure() {
            return TextSerializers.FORMATTING_CODE.deserialize(itemOnlyFailure);
        }
    }
}