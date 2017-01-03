package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
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
    public void generateVirtualCrates(ConfigurationLoader<CommentedConfigurationNode> config){
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
        populatePhysicalCrates();
    }
    private Task runner = null;
    public void populatePhysicalCrates() {
        physicalCrates = new HashMap<>();
        try {
            CommentedConfigurationNode root = plugin.crateConfig.load();
            List<? extends CommentedConfigurationNode> cacher = root.getNode("cachedCrates").getChildrenList();
            for(CommentedConfigurationNode i : cacher){
                int x = 0;
                int y = 0;
                int z = 0;
                String worldName = "";
                if(!i.getNode("position").isVirtual()){
                    x = i.getNode("position","x").getInt();
                    y = i.getNode("position","y").getInt();
                    z = i.getNode("position","z").getInt();
                }
                if(!i.getNode("worldName").isVirtual()){
                    worldName = i.getNode("worldName").getString();
                }
                Location<World> newloco = new Location<>(Sponge.getServer().getWorld(worldName).get(),x,y,z);
                String id = getTypeFromLocation(newloco);
                if(id != null) {
                    physicalCrates.put(newloco, new PhysicalCrate(newloco, id, plugin));
                }else{
                    i.setValue(null);
                    plugin.crateConfig.save(root);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        startParticleEffects();
    }
    public void startParticleEffects(){
        ArrayList<World> worldsChecked = new ArrayList<>();
        for(Location<World> e : physicalCrates.keySet()){
            World b = e.getExtent();
            if(!worldsChecked.contains(b)){
                worldsChecked.add(b);
                for(Entity ent : b.getEntities()){
                    if(ent instanceof ArmorStand){
                        ArmorStand arm = (ArmorStand) ent;
                        arm.remove();
                        if(arm.getCreator().isPresent()){
                            if(arm.getCreator().get().toString().equals(plugin.armorStandIdentifier)){
                                arm.remove();
                            }
                        }
                    }
                }
            }
        }
        for(PhysicalCrate e : physicalCrates.values()) e.initParticles();

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
                    CommentedConfigurationNode cacher = root.getNode("cachedCrates").getAppendedNode();
                    cacher.getNode("worldName").setValue(location.getExtent().getProperties().getWorldName());
                    CommentedConfigurationNode pos = cacher.getNode("position");
                    pos.getNode("x").setValue(location.getBlockX());
                    pos.getNode("y").setValue(location.getBlockY());
                    pos.getNode("z").setValue(location.getBlockZ());
                    plugin.crateConfig.save(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                populatePhysicalCrates();
            }
        }
    }
    private void particleRunner(){
        for(PhysicalCrate c : physicalCrates.values()){
            c.runParticles();
        }
    }
}
