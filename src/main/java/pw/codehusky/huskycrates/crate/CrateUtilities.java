package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
public class CrateUtilities {
    private HashMap<String,VirtualCrate> crateTypes;
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
    }
    private Task runner;
    public void spawnCrateEffecters(){
        for(ArmorStand as : plugin.effecters.keySet() ){
            String type = plugin.effecters.get(as);
            as.getWorld().spawnEntity(as,plugin.genericCause);
        }
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        runner = taskBuilder.execute(this::particleRunner).intervalTicks(1).submit(plugin);
    }
    public void recognizeChest(Location<World> location){
        if(location.getTileEntity().isPresent()){
            TileEntityCarrier te = (TileEntityCarrier) location.getTileEntity().get();
            if(te.getInventory().getName().get().contains(plugin.huskyCrateIdentifier)){
                try {
                    CommentedConfigurationNode root = plugin.crateConfig.load();
                    CommentedConfigurationNode cacher = root.getNode("cachedCrates").getAppendedNode();
                    cacher.getNode("worldUUID").setValue(location.getExtent().getUniqueId().toString());
                    CommentedConfigurationNode pos = cacher.getNode("position");
                    pos.getNode("x").setValue(location.getBlockX());
                    pos.getNode("y").setValue(location.getBlockY());
                    pos.getNode("z").setValue(location.getBlockZ());
                    plugin.crateConfig.save(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String id = te.getInventory().getName().get().replace(plugin.huskyCrateIdentifier,"");
                ArmorStand g = (ArmorStand) te.getWorld().createEntity(EntityTypes.ARMOR_STAND,te.getLocation().getPosition().add(0.5,1,0.5));
                g.offer(Keys.DISPLAY_NAME, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(crateTypes.get(id).displayName));
                g.offer(Keys.INVISIBLE,true);
                g.offer(Keys.CUSTOM_NAME_VISIBLE,true);
                g.offer(Keys.HAS_GRAVITY,false);
                g.offer(Keys.ARMOR_STAND_MARKER, true);
                te.getWorld().spawnEntity(g,plugin.genericCause);
                plugin.effecters.put(g,id);
                spawnCrateEffecters();
            }
        }
    }
    private void particleRunner(){
        for(ArmorStand as : plugin.effecters.keySet() ){
            String type = plugin.effecters.get(as);
            double time = Sponge.getServer().getRunningTimeTicks()*0.25;
            double size = 0.8;

            double x = Math.sin(time) * size;
            double y = Math.sin(time*2) * 0.2 - 0.45;
            double z = Math.cos(time) * size;
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, Color.ofRgb(100,100,100))
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x,y,z));

            x = Math.cos(time + 10) * size;
            y = Math.sin(time*2 + 10) * 0.2  - 0.55;
            z = Math.sin(time + 10) * size;
            as.getWorld().spawnParticles(
                    ParticleEffect.builder()
                            .type(ParticleTypes.REDSTONE_DUST)
                            .option(ParticleOptions.COLOR, Color.ofRgb(255,0,0))
                            .build(),
                    as.getLocation()
                            .getPosition()
                            .add(x,y,z));
        }
    }
}
