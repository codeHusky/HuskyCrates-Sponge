package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.crate.virtual.Crate;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class BlockCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Crate crate = args.<Crate>getOne(Text.of("crate")).get();
        Optional<BlockType> block = args.getOne(Text.of("block"));
        Optional<Player> otherPlayer = args.getOne(Text.of("player"));

        Player playerToGive = null;
        if(src instanceof Player)
            playerToGive = (Player) src;

        if(otherPlayer.isPresent())
            playerToGive = otherPlayer.get();

        if(playerToGive == null){
            src.sendMessage(Text.of(TextColors.RED,"A valid player could not be found to give a crate placement block to."));
            return CommandResult.success();
        }

        ItemStack stack;
        if(block.isPresent()){
            Optional<ItemType> itP = block.get().getItem();
            if(!itP.isPresent()){
                src.sendMessage(Text.of(TextColors.RED,"The block you supplied could not be converted to an item. Please try again with a different block."));
                return CommandResult.success();
            }
            stack = crate.getCratePlacementBlock(itP.get());
        }else{
            stack = crate.getCratePlacementBlock();
        }
        playerToGive.getInventory().offer(stack);
        return CommandResult.success();
    }
}