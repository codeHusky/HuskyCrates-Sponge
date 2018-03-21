package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import org.spongepowered.api.Sponge;

public class CommandRegister {
    public static void register(HuskyCrates plugin) {
        Sponge.getCommandManager().register(plugin,new Debug(),"hc");
    }
}
