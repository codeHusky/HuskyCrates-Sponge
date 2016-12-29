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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.commands.Crate;
import pw.codehusky.huskycrates.crate.CrateUtilities;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
@Plugin(id="huskycrates", name = "HuskyCrates", version = "0.1.0", description = "hey whats up guys its scarce here")
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

    @Listener
    public void gameStarted(GameStartedServerEvent event){
        CommandSpec crateSpec = CommandSpec.builder()
                .description(Text.of("Main crates command"))
                .permission("huskycrates")
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
    public void placeBlock(ChangeBlockEvent event){
        if(event instanceof ChangeBlockEvent.Place || event instanceof ChangeBlockEvent.Break) {
            BlockType t = event.getTransactions().get(0).getOriginal().getLocation().get().getBlock().getType();
            if (blockCanBeCrate(t)) {
                crateUtilities.recognizeChest(event.getTransactions().get(0).getOriginal().getLocation().get());
            }
        }
    }

    @Listener
    public void geocacheInteract(InteractBlockEvent.Secondary.MainHand event){
        if(!event.getTargetBlock().getLocation().isPresent())
            return;

        Location<World> blk = event.getTargetBlock().getLocation().get();
        if(blk.getBlock().getType() == BlockTypes.CHEST) {
            TileEntity te = blk.getTileEntity().get();
            Inventory inv = ((TileEntityCarrier) te).getInventory();
            String name = inv.getName().get();
            if(name.contains(huskyCrateIdentifier)){
                event.setCancelled(true);
                Task.Builder upcoming = scheduler.createTaskBuilder();
                String crateType = name.replace(huskyCrateIdentifier,"");
                upcoming.execute(() ->{
                    crateUtilities.launchCrateForPlayer(crateType,(Player)event.getCause().root(),this);
                }).delayTicks(1).submit(this);

            }
        }
    }
}
