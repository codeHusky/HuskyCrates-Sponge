package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.Util;
import com.codehusky.huskycrates.crate.virtual.Item;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class CrateGenerateCommand implements CommandExecutor {
    public Map<Integer, ItemStack> inventory  = new HashMap<>();
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if (src instanceof Player) {
            Player player = (Player) src;
            Inventory playerInventory = Util.getHotbarFirst(player.getInventory());
            String genType = args.<String>getOne("type").get();
            String name = args.<String>getOne("name").get();
            Path newCratePath = HuskyCrates.instance.crateDirectoryPath.resolve(name +".crate");

            if(newCratePath.toFile().exists()){
                player.sendMessage(Text.of(TextColors.RED,"Crate file already exists with this name!"));
            }
            else{
                if (genType.equals("inventory"))
                {
                    if(playerInventory.size() != 0){

                        try{
                            if(newCratePath.toFile().createNewFile()){

                                ConfigurationLoader<CommentedConfigurationNode> generatedCrateConfig;
                                generatedCrateConfig = HoconConfigurationLoader.builder().setPath(newCratePath.toAbsolutePath()).build();
                                try {
                                    src.sendMessage(Text.of(TextColors.GRAY,"Attempting to generate a crate from your inventory..."));
                                    int items = 0;

                                    CommentedConfigurationNode root = generatedCrateConfig.load();
                                    CommentedConfigurationNode n = root.getNode(name);

                                    n.getNode("name").setValue(name);
                                    n.getNode("free").setValue(false);

                                    ConfigurationNode n1 = n.getNode("slots");
                                    List<Map> slots = new ArrayList<>();


                                    for (Inventory slot : playerInventory.slots())
                                    {
                                        Optional<ItemStack> itemslot = slot.peek();
                                        if (itemslot.isPresent())
                                        {
                                            Item item = Item.fromItemStack(itemslot.get());

                                            Map<String, Object> mapA = new HashMap<>();

                                            mapA.put("chance", 10.0);

                                            Map<String, Object> displayItem = new HashMap<>();

                                            displayItem.put("id", item.getItemType().getName());
                                            displayItem.put("name", item.getName());
                                            displayItem.put("count", item.getCount());
                                            displayItem.put("damage", item.getDamage());
                                            displayItem.put("durability", item.getDurability());

                                            if(item.getLore() != null && item.getLore().size() > 0){
                                                displayItem.put("lore", item.getLore());
                                            }
                                            if(item.getEnchantments() != null && item.getEnchantments().size() > 0){
                                                Map<String, Object> enchants = new HashMap<>();
                                                item.getEnchantments().forEach(enchantment -> {
                                                    enchants.put(enchantment.getType().getId(), enchantment.getLevel());
                                                });
                                                displayItem.put("enchantments", enchants);
                                            }
                                            if(item.getNBT() != null && item.getNBT().size() > 0){
                                                displayItem.put("nbt", item.getNBT());
                                            }

                                            List<Map> rewards = new ArrayList<>();
                                            Map<String, Object> reward1 = new HashMap<>();

                                            reward1.put("type", "item");
                                            rewards.add(reward1);

                                            mapA.put("displayItem", displayItem);
                                            mapA.put("rewards", rewards);

                                            slots.add(mapA);
                                            generatedCrateConfig.save(root);
                                            items++;
                                        }
                                    }
                                    n1.setValue(slots);

                                    List<String> hologramLines = new ArrayList<>();
                                    hologramLines.add("&a"+name);
                                    hologramLines.add("&aCrate");
                                    n.getNode("hologram").getNode("lines").setValue(hologramLines);
                                    n.getNode("hologram").getNode("entityYOffset").setValue(0.0);

                                    n.getNode("viewType").setValue("spinner");

                                    Map<String, Object> viewConfig = new HashMap<>();
                                    viewConfig.put("tickDelayMultiplier", 1.025);
                                    viewConfig.put("ticksToSelection", 75);
                                    viewConfig.put("ticksToSelectionVariance", 5);
                                    n.getNode("viewConfig").setValue(viewConfig);

                                    n.getNode("localKey").getNode("id").setValue("minecraft:tripwire_hook");
                                    n.getNode("localKey").getNode("name").setValue("&a" + name + " Crate Key");

                                    n.getNode("previewable").setValue(true);
                                    n.getNode("useLocalKey").setValue(true);

                                    n.getNode("messages").getNode("rejectionNeedKey").setValue("&cYou lack the key needed to open this crate!");

                                    generatedCrateConfig.save(root);
                                    src.sendMessage(Text.of(TextColors.GREEN,"Successfully generated a crate with " + items + " items, with the name " + name));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    src.sendMessage(Text.of(TextColors.RED,"Failed to generate a crate! Error has been printed to your console/logs!"));
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            System.out.println("Failed to create crate file!");
                            player.sendMessage(Text.of(TextColors.RED,"Failed to create crate file!"));
                        }
                    }
                    else{
                        src.sendMessage(Text.of(TextColors.RED,"Your inventory has no items in it! You must have atleast 1 item present within your primary 36 inventory slots to use this generation option!"));
                    }
                }




                if (genType.equals("item"))
                {
                    if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
                        try{
                            if(newCratePath.toFile().createNewFile()){

                                ConfigurationLoader<CommentedConfigurationNode> generatedCrateConfig;
                                generatedCrateConfig = HoconConfigurationLoader.builder().setPath(newCratePath.toAbsolutePath()).build();
                                try {
                                    src.sendMessage(Text.of(TextColors.GRAY,"Attempting to generate a crate from your held item..."));

                                    Item item = Item.fromItemStack(player.getItemInHand(HandTypes.MAIN_HAND).get());

                                    CommentedConfigurationNode root = generatedCrateConfig.load();
                                    CommentedConfigurationNode n = root.getNode(name);

                                    n.getNode("name").setValue(name);
                                    n.getNode("free").setValue(false);

                                    ConfigurationNode n1 = n.getNode("slots");
                                    List<Map> slots = new ArrayList<>();

                                    Map<String, Object> mapA = new HashMap<>();

                                    mapA.put("chance", 10.0);

                                    Map<String, Object> displayItem = new HashMap<>();

                                    displayItem.put("id", item.getItemType().getName());
                                    displayItem.put("name", item.getName());
                                    displayItem.put("count", item.getCount());
                                    displayItem.put("damage", item.getDamage());
                                    displayItem.put("durability", item.getDurability());

                                    if(item.getLore() != null && item.getLore().size() > 0){
                                        displayItem.put("lore", item.getLore());
                                    }
                                    if(item.getEnchantments() != null && item.getEnchantments().size() > 0){
                                        Map<String, Object> enchants = new HashMap<>();
                                        item.getEnchantments().forEach(enchantment -> {
                                            enchants.put(enchantment.getType().getId(), enchantment.getLevel());
                                        });
                                        displayItem.put("enchantments", enchants);
                                    }
                                    if(item.getNBT() != null && item.getNBT().size() > 0){
                                        displayItem.put("nbt", item.getNBT());
                                    }

                                    List<Map> rewards = new ArrayList<>();
                                    Map<String, Object> reward1 = new HashMap<>();

                                    reward1.put("type", "item");
                                    rewards.add(reward1);

                                    mapA.put("displayItem", displayItem);
                                    mapA.put("rewards", rewards);

                                    slots.add(mapA);
                                    generatedCrateConfig.save(root);

                                    n1.setValue(slots);

                                    List<String> hologramLines = new ArrayList<>();
                                    hologramLines.add("&a"+name);
                                    hologramLines.add("&aCrate");
                                    n.getNode("hologram").getNode("lines").setValue(hologramLines);
                                    n.getNode("hologram").getNode("entityYOffset").setValue(0.0);

                                    n.getNode("viewType").setValue("spinner");

                                    Map<String, Object> viewConfig = new HashMap<>();
                                    viewConfig.put("tickDelayMultiplier", 1.025);
                                    viewConfig.put("ticksToSelection", 75);
                                    viewConfig.put("ticksToSelectionVariance", 5);
                                    n.getNode("viewConfig").setValue(viewConfig);

                                    n.getNode("localKey").getNode("id").setValue("minecraft:tripwire_hook");
                                    n.getNode("localKey").getNode("name").setValue("&a" + name + " Crate Key");

                                    n.getNode("previewable").setValue(true);
                                    n.getNode("useLocalKey").setValue(true);

                                    n.getNode("messages").getNode("rejectionNeedKey").setValue("&cYou lack the key needed to open this crate!");

                                    generatedCrateConfig.save(root);
                                    src.sendMessage(Text.of(TextColors.GREEN,"Successfully generated a crate with 1 item, and the name " + name));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    src.sendMessage(Text.of(TextColors.RED,"Failed to generate a crate! Error has been printed to your console/logs!"));
                                }
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                            System.out.println("Failed to create crate file!");
                            player.sendMessage(Text.of(TextColors.RED,"Failed to create crate file!"));
                        }
                    }
                    else{
                        player.sendMessage(Text.of(TextColors.RED,"You're not holding an item! You must be holding an item to use this generation option!"));
                    }
                }
            }
        }else{
            src.sendMessage(Text.of(TextColors.RED,"You must be a player to use this command."));
        }
        return CommandResult.success();
    }
}
