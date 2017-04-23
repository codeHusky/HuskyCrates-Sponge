package pw.codehusky.huskycrates.commands;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class Crate implements CommandExecutor {
    private HuskyCrates plugin;
    public Crate(HuskyCrates ins){
        plugin = ins;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

            if(args.getOne(Text.of("param1")).isPresent()) {
                if(args.getOne(Text.of("param1")).get().toString().equalsIgnoreCase("confitemgen")) {
                    Player plr = null;
                    if (src instanceof Player)
                        plr = (Player) src;
                    else {
                        plr.sendMessage(Text.of("Players only."));
                        return CommandResult.success();
                    }
                    StringWriter sink = new StringWriter();
                    HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink))
                            .build();
                    try {
                        ConfigurationNode node = DataTranslators.CONFIGURATION_NODE.translate(plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer());
                        //node.getNode("data").setValue(null);
                        loader.save(node);
                        System.out.println(sink.toString());
                        ItemStack deserial = ItemStack.builder()
                                .fromContainer(DataTranslators.CONFIGURATION_NODE.translate(node))
                                .build();
                        System.out.println(deserial.getItem().getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else if(!args.getOne(Text.of("param2")).isPresent()) {
                    if(src instanceof Player) {
                        Player plr = (Player) src;

                        ItemStack poss = getCrateItem(args.getOne(Text.of("param1")).get().toString());
                        if (poss != null) {
                            plr.getInventory().offer(poss);
                            plr.sendMessage(Text.of("Giving a crate to " + plr.getName() + "."));
                        } else {
                            System.out.println(plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer());
                            plr.sendMessage(Text.of("Invalid crate id. Please check your config."));
                        }
                    }
                }else{
                    if(args.getOne(Text.of("param2")).get().toString().equalsIgnoreCase("key")){
                        Player plr = null;
                        if(src instanceof Player)
                            plr = (Player) src;
                        ItemStack poss = getCrateKey(args.getOne(Text.of("param1")).get().toString());
                        if(args.getOne(Text.of("player")).isPresent()){
                            plr = (Player) args.getOne(Text.of("player")).get();
                        }
                        if (poss != null) {
                            plr.getInventory().offer(poss);
                            plr.sendMessage(Text.of("Giving a key to " + plr.getName() + "."));
                        } else {
                            plr.sendMessage(Text.of("Invalid crate id. Please check your config."));
                        }
                    }else{
                        src.sendMessage(Text.of("??"));
                    }
                }
            }

        return CommandResult.success();
    }
    public ItemStack getCrateItem(String id){
        VirtualCrate vc = plugin.crateUtilities.getVirtualCrate(id);
        if(vc != null){
            return ItemStack.builder()
                    .itemType(ItemTypes.CHEST)
                    .quantity(1)
                    .add(Keys.DISPLAY_NAME, Text.of(plugin.huskyCrateIdentifier + id)).build();
        }
        return null;
    }

    public ItemStack getCrateKey(String id){
        return this.getCrateKey(id,1);
    }

    public ItemStack getCrateKey(String id,int quantity){
        VirtualCrate vc = plugin.crateUtilities.getVirtualCrate(id);
        if(vc != null){
            ItemStack key = ItemStack.builder()
                    .itemType(ItemTypes.NETHER_STAR)
                    .quantity(quantity)
                    .add(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName + " Key")).build();
            ArrayList<Text> bb = new ArrayList<>();
            bb.add(Text.of(TextColors.WHITE,"A key for a ", TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName) , TextColors.WHITE,"."));
            bb.add(Text.of(TextColors.WHITE,"crate_" + id));
            key.offer(Keys.ITEM_LORE,bb);
            return key;
        }
        return null;
    }
}
