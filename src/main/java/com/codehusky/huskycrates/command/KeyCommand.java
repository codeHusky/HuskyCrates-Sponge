package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

import static com.codehusky.huskycrates.HuskyCrates.keyCommandMessages;

public class KeyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Crate> crate = args.getOne(Text.of("crate"));
        Optional<Key> key = args.getOne(Text.of("key"));
        Optional<Integer> pamount = args.getOne(Text.of("amount"));
        boolean isVirtual = args.getOne(Text.of("virtual")).isPresent();

        Optional<Player> player = args.getOne(Text.of("player"));
        Optional<String> all = args.getOne(Text.of("all"));

        Key workingWith = null;

        if(crate.isPresent()){
            if(crate.get().hasLocalKey()){
                workingWith = crate.get().getLocalKey();
            }
        }else if(key.isPresent()){
            workingWith = key.get();
        }
        if(workingWith == null){
            src.sendMessage(keyCommandMessages.getCrateNoLocalKey());
            return CommandResult.success();
        }

        if(workingWith.isVirtual() && !isVirtual){
            src.sendMessage(keyCommandMessages.getCrateKeyVirtual());
            return CommandResult.success();
        }
        int amount = pamount.orElse(1);
        String keyName = (crate.isPresent())?crate.get().getName():key.get().getName();

        if(all.isPresent()){/** Deliver keys to all players **/
            int deliveredTo = 0;
            for(Player p : Sponge.getServer().getOnlinePlayers()){
                InventoryTransactionResult result = null;

                if(!isVirtual)
                    result = Util.getHotbarFirst(p.getInventory()).offer(workingWith.getKeyItemStack(amount));
                else
                    HuskyCrates.registry.addVirtualKeys(p.getUniqueId(),workingWith.getId(),amount);

                if(!isVirtual && result.getType() != InventoryTransactionResult.Type.SUCCESS){
                    src.sendMessage(keyCommandMessages.getKeyDeliveryFail(p.getName(),amount));
                }else{
                    p.sendMessage(keyCommandMessages.getReceivedKey(keyName,amount));
                    deliveredTo++;
                }
            }
            src.sendMessage(keyCommandMessages.getMassKeyDeliverySuccess(deliveredTo,amount));

        }else if(player.isPresent()){ /** Deliver keys to a player **/
            InventoryTransactionResult result = null;

            if(!isVirtual)
                result = Util.getHotbarFirst(player.get().getInventory()).offer(workingWith.getKeyItemStack(amount));
            else
                HuskyCrates.registry.addVirtualKeys(player.get().getUniqueId(),workingWith.getId(),amount);

            if(!isVirtual && result.getType() != InventoryTransactionResult.Type.SUCCESS){
                src.sendMessage(keyCommandMessages.getKeyDeliveryFail(player.get().getName(),amount));
            }else{
                player.get().sendMessage(keyCommandMessages.getReceivedKey(keyName,amount));
                src.sendMessage(keyCommandMessages.getKeyDeliverySuccess(player.get().getName(),amount));
            }

        }else if(src instanceof Player) { /** Deliver keys to self **/
            Player psrc = (Player) src;
            InventoryTransactionResult result = null;

            if(!isVirtual)
                result = Util.getHotbarFirst(psrc.getInventory()).offer(workingWith.getKeyItemStack(amount));
            else
                HuskyCrates.registry.addVirtualKeys(psrc.getUniqueId(),workingWith.getId(),amount);

            if(!isVirtual && result.getType() != InventoryTransactionResult.Type.SUCCESS){
                src.sendMessage(keyCommandMessages.getSelfKeyDeliveryFail());
            }else{
                src.sendMessage(keyCommandMessages.getSelfKeyDeliverySuccess(amount));
            }

        }else{ /** No valid subject... **/
            src.sendMessage(keyCommandMessages.getNoPlayersFound());

        }
        return CommandResult.success();
    }

    public static class Messages {
        private String crateNoLocalKey;
        private String crateKeyVirtual;
        private String receivedKey;
        private String keyDeliveryFail;
        private String massKeyDeliverySuccess;
        private String keyDeliverySuccess;
        private String selfKeyDeliveryFail;
        private String selfKeyDeliverySuccess;
        private String noPlayersFound;
        public Messages(ConfigurationNode node){
            this.crateNoLocalKey = node.getNode("crateNoLocalKey")
                    .getString("&cThe supplied crate did not have a local key.");
            this.crateKeyVirtual = node.getNode("crateKeyVirtual")
                    .getString("&cThe resolved key is virtual only. Please supply a key that can be a physical item, or use the \"v\" flag.");
            this.receivedKey = node.getNode("receivedKey")
                    .getString("&aYou received {amount} {key}{amount.plural}&r!");
            this.keyDeliveryFail = node.getNode("keyDeliveryFail")
                    .getString("&c{player} failed to receive their {amount} key{amount.plural}!");
            this.massKeyDeliverySuccess = node.getNode("massKeyDeliverySuccess")
                    .getString("&a{playerAmount} player{playerAmount.plural} received {amount} key{amount.plural}.");
            this.keyDeliverySuccess = node.getNode("keyDeliverySuccess")
                    .getString("&a{player} received {amount} key{amount.plural}.");
            this.selfKeyDeliveryFail = node.getNode("selfKeyDeliveryFail")
                    .getString("&cFailed to give you keys!");
            this.selfKeyDeliverySuccess = node.getNode("selfKeyDeliverySuccess")
                    .getString("&aYou were given {amount} key{amount.plural}.");
            this.noPlayersFound = node.getNode("noPlayersFound")
                    .getString("No valid players could be found to deliver keys to.");
        }

        public Text getCrateKeyVirtual() {
            return TextSerializers.FORMATTING_CODE.deserialize(crateKeyVirtual);
        }

        public Text getCrateNoLocalKey() {
            return TextSerializers.FORMATTING_CODE.deserialize(crateNoLocalKey);
        }

        public Text getKeyDeliveryFail(String playerName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(keyDeliveryFail
                    .replace("{player}",playerName)
                    .replace("{amount}",amount.toString())
                    .replace("{amount.plural}",(amount != 1)?"s":""));
        }

        public Text getKeyDeliverySuccess(String playerName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(keyDeliverySuccess
                    .replace("{player}",playerName)
                    .replace("{amount}",amount.toString())
                    .replace("{amount.plural}",(amount != 1)?"s":""));
        }

        public Text getMassKeyDeliverySuccess(Integer playerAmount, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(massKeyDeliverySuccess
                    .replace("{playerAmount}",playerAmount.toString())
                    .replace("{playerAmount.plural}",(playerAmount != 1)?"s":"")
                    .replace("{amount}",amount.toString())
                    .replace("{amount.plural}",(amount != 1)?"s":""));
        }

        public Text getNoPlayersFound() {
            return TextSerializers.FORMATTING_CODE.deserialize(noPlayersFound);
        }

        public Text getReceivedKey(String keyName, Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(receivedKey
                    .replace("{key}",keyName)
                    .replace("{amount}",amount.toString())
                    .replace("{amount.plural}",(amount != 1)?"s":""));
        }

        public Text getSelfKeyDeliveryFail() {
            return TextSerializers.FORMATTING_CODE.deserialize(selfKeyDeliveryFail);
        }

        public Text getSelfKeyDeliverySuccess(Integer amount) {
            return TextSerializers.FORMATTING_CODE.deserialize(selfKeyDeliverySuccess
                    .replace("{amount}",amount.toString())
                    .replace("{amount.plural}",(amount != 1)?"s":""));
        }
    }
}
