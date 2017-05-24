package pw.codehusky.huskycrates.commands.elements;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import pw.codehusky.huskycrates.HuskyCrates;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created By KasperFranz.
 *
 * This CommandElement is used to get the different crate Types, so they can be tabbed when using the command.
 */
public class CrateElement extends CommandElement {

    public CrateElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource commandSource, CommandArgs commandArgs) throws ArgumentParseException {
        return commandArgs.next();
    }

    @Override
    public List<String> complete(CommandSource commandSource, CommandArgs commandArgs, CommandContext commandContext) {

        //TODO: It is here we should do some perm check :)!
        return HuskyCrates.instance.getCrateUtilities().getCrateTypes();
    }

}