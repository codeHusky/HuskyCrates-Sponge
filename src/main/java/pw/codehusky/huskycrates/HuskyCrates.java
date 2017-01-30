package pw.codehusky.huskycrates;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.world.chunk.PopulateChunkEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.commands.Crate;
import pw.codehusky.huskycrates.crate.CrateUtilities;
import pw.codehusky.huskycrates.crate.VirtualCrate;

import java.util.List;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
@Plugin(id="huskycrates", name = "HuskyCrates", version = "0.8.6", description = "A CratesReloaded Replacement for Sponge? lol")
public class HuskyCrates {
    @Inject
    public Logger logger;

    @Inject
    private PluginContainer pC;
    @Inject
    @DefaultConfig(sharedRoot = false)
    public ConfigurationLoader<CommentedConfigurationNode> crateConfig;

    public Cause genericCause;
    public Scheduler scheduler;
    public CrateUtilities crateUtilities = new CrateUtilities(this);
    public String huskyCrateIdentifier = "☼1☼2☼3HUSKYCRATE-";
    public String armorStandIdentifier = "ABABABAB-CDDE-0000-8374-CAAAECAAAECA";

    @Listener
    public void gameStarted(GameStartedServerEvent event){
        CommandSpec crateSpec = CommandSpec.builder()
                .description(Text.of("Main crates command"))
                .permission("huskycrates")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("param1"))),GenericArguments.optional(GenericArguments.string(Text.of("param2"))),GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new Crate(this))
                .build();
        scheduler = Sponge.getScheduler();
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));
        Sponge.getCommandManager().register(this, crateSpec, "crate");
        crateUtilities.generateVirtualCrates(crateConfig);
        logger.info("Crates has been started.");
    }
    @Listener
    public void gameReloaded(GameReloadEvent event){
        crateUtilities.generateVirtualCrates(crateConfig);
    }
    private boolean blockCanBeCrate(BlockType type){
        return type==BlockTypes.CHEST ||
                type==BlockTypes.TRAPPED_CHEST ||
                type==BlockTypes.ENDER_CHEST;
    }

    @Listener
    public void onChunkLoad(PopulateChunkEvent.Post event){
        event.getTargetChunk().containsBlock()

    }
    @Listener
    public void placeBlock(ChangeBlockEvent event){
        if(event instanceof ChangeBlockEvent.Place || event instanceof ChangeBlockEvent.Break) {
            BlockType t = event.getTransactions().get(0).getOriginal().getLocation().get().getBlock().getType();
            if (blockCanBeCrate(t)) {
                crateUtilities.recognizeChest(event.getTransactions().get(0).getOriginal().getLocation().get());
            }
        }
    }

    @Listener
    public void crateInteract(InteractBlockEvent.Secondary.MainHand event){
        if(!event.getTargetBlock().getLocation().isPresent())
            return;

        Location<World> blk = event.getTargetBlock().getLocation().get();
        if(blk.getBlock().getType() == BlockTypes.CHEST) {
            Player plr = (Player)event.getCause().root();
            TileEntity te = blk.getTileEntity().get();
            Inventory inv = ((TileEntityCarrier) te).getInventory();
            String name = inv.getName().get();
            if(name.contains(huskyCrateIdentifier)){
                event.setCancelled(true);
                String crateType = name.replace(huskyCrateIdentifier, "");
                if(plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                    ItemStack inhand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                    if(inhand.getItem() == ItemTypes.RED_FLOWER && inhand.get(Keys.ITEM_LORE).isPresent()) {
                        List<Text> lore = inhand.get(Keys.ITEM_LORE).get();
                        if(lore.size() > 1) {
                            String idline = lore.get(1).toPlain();
                            if(idline.contains("crate_")) {
                                if(idline.replace("crate_","").equalsIgnoreCase(crateType)) {
                                    if(!plr.hasPermission("huskycrates.tester")){
                                        if(inhand.getQuantity() == 1)
                                            plr.setItemInHand(HandTypes.MAIN_HAND,null);
                                        else{
                                            ItemStack tobe = inhand.copy();
                                            tobe.setQuantity(tobe.getQuantity()-1);
                                            plr.setItemInHand(HandTypes.MAIN_HAND,tobe);
                                        }
                                    }
                                    Task.Builder upcoming = scheduler.createTaskBuilder();

                                    upcoming.execute(() -> {
                                        crateUtilities.launchCrateForPlayer(crateType, plr, this);
                                    }).delayTicks(1).submit(this);
                                    return;
                                }
                            }
                        }
                    }

                }
                VirtualCrate vc = crateUtilities.getVirtualCrate(crateType);
                plr.playSound(SoundTypes.BLOCK_ANVIL_LAND,plr.getLocation().getPosition(),1.0);
                plr.sendMessage(Text.of("You need a ",TextSerializers.LEGACY_FORMATTING_CODE.deserialize(vc.displayName + " Key")," to open this crate."));
            }


        }
    }
}
