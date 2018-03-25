package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public class KeyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<Crate> crate = args.getOne(Text.of("crate"));
        Optional<Key> key = args.getOne(Text.of("key"));
        Optional<Integer> amount = args.getOne(Text.of("amount"));

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
            src.sendMessage(Text.of("The supplied crate did not have a local key."));
            return CommandResult.success();
        }

        if(workingWith.isVirtual()){
            src.sendMessage(Text.of("The resolved key is virtual only. Please supply a key that can be a physical item."));
            return CommandResult.success();
        }

        if(all.isPresent()){
            int deliveredTo = 0;
            for(Player p : Sponge.getServer().getOnlinePlayers()){
                InventoryTransactionResult result = Util.getHotbarFirst(p.getInventory()).offer(workingWith.getKeyItemStack(amount.orElse(1)));
                if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                    p.sendMessage(Text.of("You received " + amount.orElse(1) + " ", TextSerializers.FORMATTING_CODE.deserialize((crate.isPresent())?crate.get().getName():key.get().getName()) ,  "!"));
                    src.sendMessage(Text.of(TextColors.RED,p.getName() + " failed to receive their key(s)!"));
                }else{
                    deliveredTo++;
                }
            }
            src.sendMessage(Text.of(deliveredTo + " players were given " + amount.orElse(1) + " key(s)."));
        }else if(player.isPresent()){
            InventoryTransactionResult result = Util.getHotbarFirst(player.get().getInventory()).offer(workingWith.getKeyItemStack(amount.orElse(1)));
            if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                src.sendMessage(Text.of(TextColors.RED,player.get().getName() + " failed to receive their key(s)!"));
            }else{
                player.get().sendMessage(Text.of("You received " + amount.orElse(1) + " ", TextSerializers.FORMATTING_CODE.deserialize((crate.isPresent())?crate.get().getName():key.get().getName()) ,  "!"));
                src.sendMessage(Text.of(player.get().getName() + " was given " + amount.orElse(1) + " key(s)."));
            }
        }else if(src instanceof Player) {
            Player psrc = (Player) src;
            InventoryTransactionResult result = Util.getHotbarFirst(psrc.getInventory()).offer(workingWith.getKeyItemStack(amount.orElse(1)));
            if(result.getType() != InventoryTransactionResult.Type.SUCCESS){
                src.sendMessage(Text.of(TextColors.RED,"Failed to give you keys!"));
            }else{
                src.sendMessage(Text.of("You were given " + amount.orElse(1) + " key(s)."));
            }
        }else{
            src.sendMessage(Text.of("No valid player(s) could be found to deliver keys to."));
        }
        return CommandResult.success();
    }
}
