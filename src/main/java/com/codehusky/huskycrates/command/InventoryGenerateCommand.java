package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Item;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InventoryGenerateCommand implements CommandExecutor {
    public Map<Integer, ItemStack> inventory  = new HashMap<>();
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;

            Inventory playerInventory = Util.getHotbarFirst(player.getInventory());

            src.sendMessage(Text.of(TextColors.GRAY,"Attempting to generate items based on your inventory..."));

            if(playerInventory.size() == 0){
                src.sendMessage(Text.of(TextColors.RED,"Your inventory must have an item in it to use this command!"));
            }
            else{
                inventory = new HashMap<>();
                int i = 0;
                for (Inventory slot : playerInventory.slots())
                {
                    Optional<ItemStack> itemslot = slot.peek();
                    if (itemslot.isPresent())
                    {
                        Item item = Item.fromItemStack(itemslot.get());
                        try {
                            String uuid = UUID.randomUUID().toString();
                            CommentedConfigurationNode root = HuskyCrates.instance.generatedInventoryConfig.load();
                            CommentedConfigurationNode n = root.getNode(uuid);

                            n.getNode("id").setValue(item.getItemType().getName());
                            n.getNode("name").setValue(item.getName());
                            n.getNode("count").setValue(item.getCount());
                            n.getNode("damage").setValue(item.getDamage());
                            n.getNode("durability").setValue(item.getDurability());
                            if(item.getLore() != null && item.getLore().size() > 0){
                                n.getNode("lore").setValue(item.getLore());
                            }
                            if(item.getEnchantments() != null && item.getEnchantments().size() > 0){
                                ConfigurationNode en = n.getNode("enchantments");
                                item.getEnchantments().forEach(enchantment -> {
                                    en.getNode(enchantment.getType().getId()).setValue(enchantment.getLevel());
                                });
                            }
                            if(item.getNBT() != null && item.getNBT().size() > 0){
                                n.getNode("nbt").setValue(item.getNBT());
                            }
                            HuskyCrates.instance.generatedInventoryConfig.save(root);
                            src.sendMessage(Text.of(TextColors.GREEN,"Item saved as UUID " + uuid + "."));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    i++;
                }

            }
        }else{
            src.sendMessage(Text.of(TextColors.RED,"You must be a player to use this command."));
        }
        return CommandResult.success();
    }
}
