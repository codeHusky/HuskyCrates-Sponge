package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of("Reloading HuskyCrates..."));
        HuskyCrates.instance.reload();
        if(HuskyCrates.instance.inErrorState){
            src.sendMessage(Text.of(TextColors.RED,"HuskyCrates experienced an error while reloading. Please check the console for more information." + (!Sponge.getPlatform().asMap().get("ImplementationName").equals("SpongeVanilla")?"\nYou may have to view a file named like \"fml-junk.log\" to see the errors.":"")));
        }else{
            src.sendMessage(Text.of("HuskyCrates reloaded successfully."));
        }
        return CommandResult.success();
    }
}
