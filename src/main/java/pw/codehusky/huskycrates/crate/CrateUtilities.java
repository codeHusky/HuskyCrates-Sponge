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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
import java.util.*;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class CrateUtilities {
    private HashMap<String,VirtualCrate> crateTypes = new HashMap<>();
    public HashMap<Location<World>,PhysicalCrate> physicalCrates = new HashMap<>();
    private HashMap<String,ItemStack> keys = new HashMap<>();
    public boolean hasInitalizedVirtualCrates = false;
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
        hasInitalizedVirtualCrates = true;
    }
    private Task runner = null;
    public void populatePhysicalCrates(Extent bit) {
        ArrayList<Location<World>> eep = new ArrayList<>();
        for(Entity ent : bit.getEntities()){
            if(ent instanceof ArmorStand){
                ArmorStand arm = (ArmorStand) ent;
                if(arm.getCreator().isPresent()){
                    if(arm.getCreator().get().equals(UUID.fromString(plugin.armorStandIdentifier))){
                        Location woot = arm.getLocation().copy().sub(PhysicalCrate.offset);

                        if(physicalCrates.containsKey(woot))
                            continue;
                        eep.add(woot);
                        //arm.remove();
                    }
                }
            }
        }

        for(Location<World> loco : eep){
            if(!bit.containsBlock(loco.getBlockPosition())) {
                return;
            }
            String id = getTypeFromLocation(loco);
            if(id != null) {
                if(physicalCrates.containsKey(loco))
                    continue;
                physicalCrates.put(loco, new PhysicalCrate(loco, id, plugin));
            }
        }
        startParticleEffects();
        HuskyCrates.instance.updatePhysicalCrates();
    }
    public void startParticleEffects(){
        if(runner != null){
            runner.cancel();
        }
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        runner = taskBuilder.execute(this::particleRunner).intervalTicks(1).async().submit(plugin);
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
    public void recognizeChest(Location<World> location){
        if(physicalCrates.containsKey(location)) return;
        String id = null;
        try {
            id = getTypeFromLocation(location);
        } catch (Exception e) {}
        if(id != null){
            physicalCrates.put(location,new PhysicalCrate(location,id,plugin));
            HuskyCrates.instance.updatePhysicalCrates();
        }

    }
    private void particleRunner(){
        boolean flag = false;
        try {
            for (Location<World> b : physicalCrates.keySet()) {
                PhysicalCrate c = physicalCrates.get(b);
                if (c.vc.crateBlockType != c.location.getBlock().getType()) {
                    c.as.remove();
                    physicalCrates.remove(b);
                    flag = true;
                    continue;
                }
                c.runParticles();
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
}
