package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
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
            }
        }
    }
}
