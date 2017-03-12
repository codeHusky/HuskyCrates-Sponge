package pw.codehusky.huskycrates.commands.subcommand;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import pw.codehusky.huskycrates.HuskyCrates;

public class Reload implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        HuskyCrates.instance.reload();
        commandSource.sendMessage(Text.of("HuskyCrates reloaded"));
        return CommandResult.success();
    }
}
