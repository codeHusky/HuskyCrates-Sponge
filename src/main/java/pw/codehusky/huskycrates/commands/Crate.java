package pw.codehusky.huskycrates.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.VirtualCrate;

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
                if(!args.getOne(Text.of("param2")).isPresent()) {
                    if(src instanceof Player) {
                        Player plr = (Player) src;

                        ItemStack poss = getCrateItem(args.getOne(Text.of("param1")).get().toString());
                        if (poss != null) {
                            plr.getInventory().offer(poss);
                        } else {
                            plr.sendMessage(Text.of("Invalid crate id. Please check your config."));
                        }
                    }
                }else{
                    if(args.getOne(Text.of("param1")).get().toString().equalsIgnoreCase("key")){
                        Player plr = null;
                        if(src instanceof Player)
                            plr = (Player) src;
                        ItemStack poss = getCrateKey(args.getOne(Text.of("param2")).get().toString());
                        if(args.getOne(Text.of("player")).isPresent()){
                            plr = (Player) args.getOne(Text.of("player")).get();
                        }
                        if (poss != null) {
                            plr.getInventory().offer(poss);
                        } else {
                            plr.sendMessage(Text.of("Invalid crate id. Please check your config."));
                        }
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
        VirtualCrate vc = plugin.crateUtilities.getVirtualCrate(id);
        if(vc != null){
            ItemStack key = ItemStack.builder()
                    .itemType(ItemTypes.NETHER_STAR)
                    .quantity(1)
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
