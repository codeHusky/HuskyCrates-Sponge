package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class CrateUtilities {
    private HashMap<String,VirtualCrate> crateTypes = new HashMap<>();
    public HashMap<Location<World>,PhysicalCrate> physicalCrates = new HashMap<>();
    public boolean hasInitalizedVirtualCrates = false;
    private HuskyCrates plugin;
    public CrateUtilities(HuskyCrates plugin){
        this.plugin = plugin;
    }
    public void launchCrateForPlayer(String crateType, Player target,HuskyCrates plugin){
        if(!crateTypes.containsKey(crateType)) {
            System.out.println(crateType);
            target.openInventory(new NullCrateView(plugin,target,null).getInventory(), plugin.genericCause);
        }else{
            if(crateTypes.get(crateType).isGUI) {
                target.openInventory(crateTypes.get(crateType).generateViewForCrate(plugin, target).getInventory(), plugin.genericCause);
            }else{
                crateTypes.get(crateType).generateViewForCrate(plugin, target);
            }
        }
    }
    public VirtualCrate getVirtualCrate(String id){
        if(crateTypes.containsKey(id)){
            return crateTypes.get(id);
        }
        return null;
    }
    private ArrayList<Location<World>> toCheck;
    public void generateVirtualCrates(ConfigurationLoader<CommentedConfigurationNode> config){
        toCheck = new ArrayList<>();
        physicalCrates = new HashMap<>();
        //System.out.println("GEN VC CALLED");
        try {
            CommentedConfigurationNode configRoot = config.load();
            crateTypes = new HashMap<>();
            Map<Object,? extends CommentedConfigurationNode> b = configRoot.getNode("crates").getChildrenMap();
            for(Object prekey: b.keySet()){
                String key = (String) prekey;
                crateTypes.put(key,new VirtualCrate(key,config,configRoot.getNode("crates",key)));
            }
            config.save(configRoot);
        } catch (Exception e) {
            HuskyCrates.instance.logger.error("!!!!! Config loading has failed! !!!!!");
            HuskyCrates.instance.logger.error("!!!!! Config loading has failed! !!!!!");
            HuskyCrates.instance.logger.error("!!!!! Config loading has failed! !!!!!");
            if(e instanceof IOException){
                HuskyCrates.instance.logger.error("CONFIG AT LINE " + e.getMessage().substring(e.getMessage().indexOf("Reader: ") + 8));
            }else{
                e.printStackTrace();
            }
            HuskyCrates.instance.logger.error("Due to the exception, further loading procedures have been stopped. Please address the exception.");
            HuskyCrates.instance.logger.error("If you're having trouble solving this issue, join the support discord: https://discord.gg/FSETtcx");
            return;
        }
        hasInitalizedVirtualCrates = true;
    }
    private Task runner = null;
    public void startParticleEffects(){
        if(runner != null){
            runner.cancel();
        }
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        runner = taskBuilder.execute(this::particleRunner).intervalTicks(1).submit(plugin);
    }
    public String getTypeFromLocation(Location<World> location) {
        if(!location.getTileEntity().isPresent()) {
            return null;
        }
        String prego = ((TileEntityCarrier) location.getTileEntity().get()).getInventory().getName().get();
        if(!prego.contains(plugin.huskyCrateIdentifier))
            return null;
        return prego.replace(plugin.huskyCrateIdentifier,"");
    }
    /*public void recognizeChest(Location<World> location){
        if(physicalCrates.containsKey(location)) return;
        String id = null;
        try {
            id = getTypeFromLocation(location);
        } catch (Exception e) {}
        if(id != null){
            physicalCrates.put(location,new PhysicalCrate(location,id,plugin));
            HuskyCrates.instance.updatePhysicalCrates();
        }

    }*/
    public boolean flag = false;
    private void particleRunner(){
        if(flag)
            return;
        try {
            ArrayList<Location<World>> invalidLocations = new ArrayList<>();
            HashSet<World> invalidLocationWorlds = new HashSet<>();
            for (Location<World> b : physicalCrates.keySet()) {
                PhysicalCrate c = physicalCrates.get(b);

                if (c.vc.crateBlockType != c.location.getBlock().getType() && c.location.getExtent().isLoaded() && c.location.getExtent().getChunk(c.location.getChunkPosition()).isPresent()) {
                    if(c.location.getExtent().getChunk(c.location.getChunkPosition()).get().isLoaded()) {
                        invalidLocations.add(c.location);
                        invalidLocationWorlds.add(c.location.getExtent());
                        continue;
                    }
                }
                c.runParticles();
            }
            for(World w : invalidLocationWorlds) {
                for (Entity e : w.getEntities()) {
                    if (invalidLocations.contains(e.getLocation()) && e.getType() != EntityTypes.ARMOR_STAND) {
                        //System.out.println("woah");
                        invalidLocations.remove(e.getLocation());
                        physicalCrates.get(e.getLocation()).runParticles();

                    }
                }
            }
            for(Location<World> l : invalidLocations){
                PhysicalCrate c = physicalCrates.get(l);
                HuskyCrates.instance.logger.warn("Removing crate that no longer exists! " + c.location.getPosition().toString());
                c.as.remove();
                physicalCrates.remove(l);
                flag = true;
            }
        }catch(Exception e){

        }
        if(flag)
            HuskyCrates.instance.updatePhysicalCrates();
    }

    /***
     * Get the different Types of crate types.
     * @return a ArrayList of the different keys for crates.
     */
    public List<String> getCrateTypes() {
        return new ArrayList<>(crateTypes.keySet());
    }

    public int isAcceptedKey(PhysicalCrate crate, Optional<ItemStack> key, Player using) {
        if (crate.vc.freeCrate) {
            System.out.println("O");
            if (!crate.lastUsed.containsKey(using.getUniqueId())) {
                return 1;
            } else {
                //System.out.println(crate.vc.getOptions());
                LocalDateTime lastUsed = crate.lastUsed.get(using.getUniqueId());
                LocalDateTime minimumWait = lastUsed.plusSeconds((int) crate.vc.getOptions().get("freeCrateDelay"));
                HuskyCrates.instance.logger.info("" + LocalDateTime.now().compareTo(minimumWait));
                if(LocalDateTime.now().compareTo(minimumWait) > 0){
                    return 1;
                }
                return -1;
            }
        } else if (key.isPresent()) {
            if (key.get().getItem() == crate.vc.getKeyType()) {
                if (key.get().toContainer().get(DataQuery.of("UnsafeData", "crateID")).isPresent()) {
                    String id = key.get().toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString();
                    if (id.equals(crate.vc.crateType)) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }
}
