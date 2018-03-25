package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;

public class CommandRegister {
    public static void register(HuskyCrates plugin) {
        CommandSpec mainCommand = CommandSpec.builder()
                .child(CommandSpec.builder()
                        .executor(new BlockCommand())
                        .arguments()
                        .build(),"b","blk","block")
                .child(CommandSpec.builder()
                        .executor(new KeyCommand())
                        .arguments()
                        .build(),"k","key")
                .child(CommandSpec.builder()
                        .executor(new VirtualKeyCommand())
                        .arguments()
                        .build(),"vk","vkey","virtualkey")
                .child(CommandSpec.builder()
                        .executor(new ReloadCommand())
                        .build(),"rl","r","reload")
                .executor(new MainCommand())
                .arguments(GenericArguments.none())
                .build();
        Sponge.getCommandManager().register(plugin,mainCommand,"hc","husky","huskycrates");
    }
}
