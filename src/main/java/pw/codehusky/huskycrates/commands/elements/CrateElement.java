package pw.codehusky.huskycrates.commands.elements;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import pw.codehusky.huskycrates.HuskyCrates;

import java.util.List;

import javax.annotation.Nullable;

public class CrateElement extends CommandElement {

    public CrateElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource commandSource, CommandArgs commandArgs) throws ArgumentParseException {
        String arg = commandArgs.next();
        if (HuskyCrates.instance.getCrateUtilities().getCrateTypes().contains(arg)) {
            return arg;
        }
        throw commandArgs.createError(Text.of(new Object[]{TextColors.RED, arg, " is not a valid Crate!"}));
    }

    @Override
    public List<String> complete(CommandSource commandSource, CommandArgs commandArgs, CommandContext commandContext) {

        //TODO: It is here we should do some perm check :)!
        return HuskyCrates.instance.getCrateUtilities().getCrateTypes();
    }

}
