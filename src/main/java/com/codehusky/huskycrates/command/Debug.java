package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.views.SpinnerView;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Debug implements CommandCallable{
    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        new SpinnerView(HuskyCrates.registry.getCrate(arguments),(Player) source);
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return new ArrayList<>();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Debug command"));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Help ya'll"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("/hc");
    }
}
