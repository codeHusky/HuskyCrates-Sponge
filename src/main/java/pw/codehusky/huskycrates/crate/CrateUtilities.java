package pw.codehusky.huskycrates.crate;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
import java.util.*;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class CrateUtilities {
    private HashMap<String,VirtualCrate> crateTypes;
    private HashMap<Location<World>,PhysicalCrate> physicalCrates;
    private HashMap<String,ItemStack> keys;
    private HuskyCrates plugin;
    public CrateUtilities(HuskyCrates plugin){
        this.plugin = plugin;
    }
    public void launchCrateForPlayer(String crateType, Player target,HuskyCrates plugin){
        crateType = crateType.toLowerCase();
        if(!crateTypes.containsKey(crateType)) {
            System.out.println(crateType);
            target.openInventory(new NullCrateView(plugin,target,null).getInventory(), plugin.genericCause);
        }else{
            target.openInventory(crateTypes.get(crateType).generateViewForCrate(plugin, target).getInventory(), plugin.genericCause);
        }
    }
    public ItemStack getCrateItemStack(String crateType){
        return null;
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
        try {
            CommentedConfigurationNode configRoot = config.load();
            crateTypes = new HashMap<>();
            Map<Object,? extends CommentedConfigurationNode> b = configRoot.getNode("crates").getChildrenMap();
            for(Object prekey: b.keySet()){
                String key = (String) prekey;
                crateTypes.put(key,new VirtualCrate(key,configRoot.getNode("crates",key)));
            }
            config.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CommentedConfigurationNode root = plugin.crateConfig.load();
            List<? extends CommentedConfigurationNode> cacher = root.getNode("cachedCrates").getChildrenList();
            for(CommentedConfigurationNode i : cacher){
                try {
                    toCheck.add(i.getValue(TypeToken.of(Location.class)));
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                    i.setValue(null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Task runner = null;
    public void populatePhysicalCrates(Chunk chunk) {
        for(Entity ent : chunk.getEntities()){
            if(ent instanceof ArmorStand){
                ArmorStand arm = (ArmorStand) ent;
                if(arm.getCreator().isPresent()){
                    if(arm.getCreator().get().equals(UUID.fromString(plugin.armorStandIdentifier))){
                        arm.remove();
                    }
                }
            }
        }


        for(Location<World> loco : toCheck){
            if(!chunk.containsBlock(loco.getBlockPosition()))
                return;
            String id = getTypeFromLocation(loco);
            if(id != null) {
                physicalCrates.put(loco, new PhysicalCrate(loco, id, plugin));
                System.out.println("crate identified");
            }else{
                System.out.println("ahem.");
            }
            toCheck.remove(loco);
        }
        startParticleEffects();
    }
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
            plugin.logger.info("not present");
            plugin.logger.info(location.getX() + "," + location.getY() + "," + location.getZ());
            plugin.logger.info(location.getBlockType().getName());
            return null;
        }
        String prego = ((TileEntityCarrier) location.getTileEntity().get()).getInventory().getName().get();
        if(!prego.contains(plugin.huskyCrateIdentifier))
            return null;
        return prego.replace(plugin.huskyCrateIdentifier,"");
    }
    public void recognizeChest(Location<World> location){
        if(location.getTileEntity().isPresent()){
            String id = null;
            try {
                id = getTypeFromLocation(location);
            } catch (Exception e) {}
            if(id != null){
                try {

                    CommentedConfigurationNode root = plugin.crateConfig.load();
                    try {
                        root.getNode("cachedCrates").getAppendedNode().setValue(TypeToken.of(Location.class),location);
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                    plugin.crateConfig.save(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                populatePhysicalCrates(location.getExtent().getChunk(location.getChunkPosition()).get());
            }
        }
    }
    private void particleRunner(){
        for(PhysicalCrate c : physicalCrates.values()){
            c.runParticles();
        }
    }
}
