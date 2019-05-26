package com.codehusky.huskycrates.command;

import com.codehusky.huskycrates.HuskyCrates;
import com.codehusky.huskycrates.crate.virtual.Item;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class ItemGenerateCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if(src instanceof Player){
            Player player = (Player)src;
            if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
                ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
                Item item = Item.fromItemStack(stack);
                try {
                    String uuid = UUID.randomUUID().toString();
                    CommentedConfigurationNode root = HuskyCrates.instance.generatedItemConfig.load();
                    CommentedConfigurationNode n = root.getNode(uuid);
                    /*
if(!node.getNode("id").isVirtual()){
    try {
        this.itemType = node.getNode("id").getValue(TypeToken.of(ItemType.class));
    } catch (ObjectMappingException e) {
        HuskyCrates.instance.logger.error("Printing out ItemID ObjectMappingException below!");
        e.printStackTrace();
        throw new ConfigParseError("Supplied Item ID is not valid!",node.getNode("id").getPath());
    }
}else{
    throw new ConfigParseError("Item ID was not specified in an item!",node.getNode("id").getPath());
}

this.name = node.getNode("name").getString();
this.count = node.getNode("count").getInt(1);
this.damage = node.getNode("damage").getInt(node.getNode("meta").getInt(node.getNode("metadata").getInt()));
this.durability = node.getNode("durability").getInt();

if(!node.getNode("lore").isVirtual()){
    try {
        this.lore = node.getNode("lore").getList(TypeToken.of(String.class));
    } catch (ObjectMappingException e) {
        e.printStackTrace();
        throw new ConfigParseError("Invalid lore virtual specified! Reason is printed above.",node.getNode("lore").getPath());
    }
}
this.enchantments = new ArrayList<>();
if(!node.getNode("enchantments").isVirtual()){
    node.getNode("enchantments").getChildrenMap().forEach((enchantID, level) -> {
        Optional<EnchantmentType> enchantType = Sponge.getRegistry().getType(EnchantmentType.class,enchantID.toString());
        if(!enchantType.isPresent()){
            throw new ConfigParseError("Invalid Enchantment specified!",node.getNode("enchantments",enchantID).getPath());
        }
        if(!(level.getValue() instanceof Integer)){
            throw new ConfigParseError("Invalid Type for Enchantment Level!",node.getNode("enchantments",enchantID).getPath());
        }

        this.enchantments.add(Enchantment.builder()
                .type(enchantType.get())
                .level((Integer)level.getValue())
                .build());
    });
}

if(!node.getNode("nbt").isVirtual() && node.getNode("nbt").getValue() instanceof LinkedHashMap) {
    this.nbt = (LinkedHashMap) node.getNode("nbt").getValue();
}
                     */
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
                    HuskyCrates.instance.generatedItemConfig.save(root);
                    src.sendMessage(Text.of(TextColors.GREEN,"Item saved as UUID " + uuid + "."));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                src.sendMessage(Text.of(TextColors.RED,"You must be holding an item to use this command."));
            }
        }else{
            src.sendMessage(Text.of(TextColors.RED,"You must be a player to use this command."));
        }

        return CommandResult.success();
    }
}
