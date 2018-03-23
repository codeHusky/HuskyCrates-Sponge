package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.Crate;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class Debug implements CommandExecutor{

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(!(src instanceof Player)) return CommandResult.success();
        Player player = (Player)src;

        String crateid = args.getOne(Text.of("crateid")).get().toString();
        String type = args.getOne(Text.of("type")).get().toString();

        Crate crate = HuskyCrates.registry.getCrate(crateid);

        switch(type.toLowerCase()){
            case "key":
                player.getInventory().offer(crate.getLocalKey().getKeyItemStack());
                break;
            case "block":
                player.getInventory().offer(crate.getCratePlacementBlock());
                break;
        }
        return CommandResult.success();
    }
}
