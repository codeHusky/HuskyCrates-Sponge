package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BalanceCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        /*
         .arguments(GenericArguments.optionalWeak(GenericArguments.user(Text.of("player"))),
                    GenericArguments.optionalWeak(GenericArguments.uuid(Text.of("uuid"))),
                    GenericArguments.optionalWeak(GenericArguments.string(Text.of("username"))))
         */
        Optional<User> user = args.getOne(Text.of("player"));
        Optional<UUID> uuid = args.getOne(Text.of("uuid"));
        Optional<String> username = args.getOne(Text.of("username"));

        UUID balanceToUse = (src instanceof Player)?((Player) src).getUniqueId():null;

        if(user.isPresent()){
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }

            src.sendMessage(HuskyCrates.balanceCommandMessages.getOtherBalanceHeader(user.get().getName()));
        }else if(uuid.isPresent()){
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }

            src.sendMessage(HuskyCrates.balanceCommandMessages.getUUIDBalanceHeader(uuid.get().toString()));
        }else if(username.isPresent()){
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }

            src.sendMessage(HuskyCrates.balanceCommandMessages.getUserNotExist(username.get()));
            return CommandResult.success();
        }else{
            src.sendMessage(HuskyCrates.balanceCommandMessages.getSelfBalanceHeader());
        }

        if(balanceToUse == null){
            src.sendMessage(HuskyCrates.balanceCommandMessages.getNoValidUser());
            return CommandResult.success();
        }

        HashMap<String, Integer> balances = HuskyCrates.registry.getVirtualKeyBalances(balanceToUse);

        for(Map.Entry<String, Integer> entry : balances.entrySet()){
            String keyID = entry.getKey();
            src.sendMessage(HuskyCrates.balanceCommandMessages.getBalanceRow(HuskyCrates.registry.getKey(keyID).getName(),keyID,entry.getValue()));
        }

        if(balances.size() == 0){
            src.sendMessage(HuskyCrates.balanceCommandMessages.getNoBalanceEntries());
        }

        return CommandResult.success();
    }

    public static class Messages {
        private String balanceRow;
        private String otherBalanceHeader;
        private String uuidBalanceHeader;
        private String selfBalanceHeader;
        private String userNotExist;
        private String noValidUser;
        private String noBalanceEntries;
        public Messages(ConfigurationNode node){
            this.balanceRow = node.getNode("balanceRow")
                    .getString("&7 - {key}&r&7: &a{amount}");
            this.otherBalanceHeader = node.getNode("otherBalanceHeader")
                    .getString("&6{player}'s Key Balances");
            this.uuidBalanceHeader = node.getNode("uuidBalanceHeader")
                    .getString("&2Balance for {uuid}");
            this.selfBalanceHeader = node.getNode("selfBalanceHeader")
                    .getString("&3Your Key Balances");
            this.userNotExist = node.getNode("userNotExist")
                    .getString("&c{username} has never logged into this server.");
            this.noValidUser = node.getNode("noValidUser")
                    .getString("&cNo valid user found to list the balance of.");
            this.noBalanceEntries = node.getNode("noBalanceEntries")
                    .getString("&7This user has no balances.");
        }

        public Text getBalanceRow(String keyName, String keyID, int amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    balanceRow
                            .replace("{key}",keyName)
                            .replace("{key.id}",keyID)
                            .replace("{amount}","" + amount)
                            .replace("{amount.plural}",(amount != 1)?"s":"")
            );
        }

        public Text getOtherBalanceHeader(String playerName) {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    otherBalanceHeader
                        .replace("{player}",playerName)
            );
        }

        public Text getSelfBalanceHeader() {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    selfBalanceHeader
            );
        }

        public Text getUserNotExist(String username) {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    userNotExist
                        .replace("{username}",username)
            );
        }

        public Text getUUIDBalanceHeader(String uuid) {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    uuidBalanceHeader
                        .replace("{uuid}",uuid)
            );
        }

        public Text getNoValidUser() {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    noValidUser
            );
        }

        public Text getNoBalanceEntries() {
            return TextSerializers.FORMATTING_CODE.deserialize(
                    noBalanceEntries
            );
        }
    }
}
