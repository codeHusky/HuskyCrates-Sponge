package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CommandRegister {
    public static void register(HuskyCrates plugin) {
        CommandSpec mainCommand = CommandSpec.builder()
                .executor(new Debug())
                .arguments(GenericArguments.string(Text.of("crateid")), GenericArguments.string(Text.of("type")))
                .build();
        Sponge.getCommandManager().register(plugin,mainCommand,"hc");
    }
}
