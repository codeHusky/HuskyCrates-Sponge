package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import ninja.leaping.configurate.ConfigurationNode;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
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

    	// Added bal others + fix username + change player to user
        User user = (User)args.getOne(Text.of("player")).orElse(null); // default null
        UUID uuid = (UUID)args.getOne(Text.of("uuid")).orElse(null); // default null
        String username = (String)args.getOne(Text.of("username")).orElse(null);

        UUID balanceToUse = (src instanceof Player)?((Player) src).getUniqueId():null;

        if(user != null){ 
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }
            // overwrite balance to check
            balanceToUse = user.getUniqueId();
            src.sendMessage(HuskyCrates.balanceCommandMessages.getOtherBalanceHeader(user.getName()));
        }else if(uuid != null){
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }
            // overwrite balance to check
            balanceToUse = uuid;
            src.sendMessage(HuskyCrates.balanceCommandMessages.getUUIDBalanceHeader(uuid.toString()));
        }else if(username != null){
            if(!src.hasPermission("huskycrates.bal.others")){
                src.sendMessage(Text.of(TextColors.RED,"You do not have permission to view the balance of others."));
                return CommandResult.success();
            }
            
            // Get user by (last known) username
            Optional<UserStorageService> service = Sponge.getServiceManager().provide(UserStorageService.class);
            if( service.get() != null&& service.isPresent() ) {
            	User us = service.get().get(username).isPresent()?service.get().get(username).get():null;
            	if(us == null) { // user does not exist
                    src.sendMessage(HuskyCrates.balanceCommandMessages.getUserNotExist(username));
                    return CommandResult.success();
            	}
            	// user does exist
                src.sendMessage(HuskyCrates.balanceCommandMessages.getUUIDBalanceHeader(us.getName()));
                // overwrite balance to check
                balanceToUse = us.getUniqueId();
            }else { // no service, so no user
                src.sendMessage(HuskyCrates.balanceCommandMessages.getUserNotExist(username));
                return CommandResult.success();
            }

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
        	if(entry.getValue() > 0) { // decrease huge lists with 0 keys
                String keyID = entry.getKey();
                src.sendMessage(HuskyCrates.balanceCommandMessages.getBalanceRow(HuskyCrates.registry.getKey(keyID).getName(),keyID,entry.getValue()));
        	}
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
