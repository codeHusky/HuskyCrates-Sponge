package com.codehusky.huskycrates.crate;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by lokio on 1/16/2017.
 */
public class CrateCommandSource implements CommandSource {
    @Override
    public String getName() {
        return "HuskyCrates";
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return null;
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return null;
    }

    @Override
    public SubjectData getSubjectData() {
        return null;
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return null;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> set, String s) {
        return null;
    }

    @Override
    public boolean isChildOf(Set<Context> set, Subject subject) {
        return false;
    }

    @Override
    public List<Subject> getParents(Set<Context> set) {
        return null;
    }

    @Override
    public Optional<String> getOption(Set<Context> set, String s) {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return null;
    }

    @Override
    public void sendMessage(Text text) {

    }

    @Override
    public MessageChannel getMessageChannel() {
        return null;
    }

    @Override
    public void setMessageChannel(MessageChannel messageChannel) {

    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

}
