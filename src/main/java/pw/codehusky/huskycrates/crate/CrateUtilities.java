package pw.codehusky.huskycrates.crate;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
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


public class CrateUtilities {
    private HashMap<String, VirtualCrate> crateTypes;
    private HashMap<Location<World>, PhysicalCrate> physicalCrates;
    private HashMap<String, ItemStack> keys;
    private HuskyCrates plugin;
    private ArrayList<Location<World>> toCheck;
    private Task runner = null;

    public CrateUtilities(HuskyCrates plugin) {
        this.plugin = plugin;
    }

    public void launchCrateForPlayer(String crateType, Player target, HuskyCrates plugin) {
        crateType = crateType.toLowerCase();
        if (!crateTypes.containsKey(crateType)) {
            target.openInventory(new NullCrateView(plugin, target, null).getInventory(), plugin.genericCause);
        } else {
            target.openInventory(crateTypes.get(crateType).generateViewForCrate(plugin, target).getInventory(), plugin.genericCause);
        }
    }

    public ItemStack getCrateItemStack(String crateType) {
        return null;
    }

    public VirtualCrate getVirtualCrate(String id) {
        if (crateTypes.containsKey(id)) {
            return crateTypes.get(id);
        }
        return null;
    }

    public void generateVirtualCrates(ConfigurationLoader<CommentedConfigurationNode> config) {
        toCheck = new ArrayList<>();
        physicalCrates = new HashMap<>();
        try {
            CommentedConfigurationNode configRoot = config.load();
            crateTypes = new HashMap<>();
            Map<Object, ? extends CommentedConfigurationNode> b = configRoot.getNode("crates").getChildrenMap();
            for (Object prekey : b.keySet()) {
                String key = (String) prekey;
                crateTypes.put(key, new VirtualCrate(key, configRoot.getNode("crates", key)));
            }
            config.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            CommentedConfigurationNode root = plugin.crateConfig.load();
            List<? extends CommentedConfigurationNode> cacher = root.getNode("cachedCrates").getChildrenList();
            for (CommentedConfigurationNode cached : cacher) {
                try {

                    String[] stringList = ((String) cached.getValue()).split(":");

                    World world = Sponge.getServer().getWorld(stringList[0]).get();
                    Location loc = world.getLocation(Double.parseDouble(stringList[1]), Double.parseDouble(stringList[2]), Double.parseDouble(stringList[3]));
                    toCheck.add(loc);
                } catch (Exception e) {
                    e.printStackTrace();
                    cached.setValue(null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (World e : Sponge.getServer().getWorlds()) {
            populatePhysicalCrates(e);
        }
    }

        public void populatePhysicalCrates(Extent bit) {
        ArrayList<Location<World>> eep = new ArrayList<>();
        for (Entity ent : bit.getEntities()) {
            if (ent instanceof ArmorStand) {
                ArmorStand arm = (ArmorStand) ent;
                if (arm.getCreator().isPresent()) {
                    if (arm.getCreator().get().equals(UUID.fromString(plugin.armorStandIdentifier))) {
                        Location woot = arm.getLocation().copy().sub(PhysicalCrate.offset);

                        if (physicalCrates.containsKey(woot))
                            continue;
                        eep.add(woot);
                        //arm.remove();
                    }else{

                    }
                }
            }
        }

        for (Location<World> loco : eep) {
            if (!bit.containsBlock(loco.getBlockPosition())) {
                return;
            }
            String id = getTypeFromLocation(loco);
            if (id != null) {
                HuskyCrates.instance.logger.info("added crate"+loco.getExtent().getName() + ":" + loco.getX() + ":" + loco.getY() + ":" + loco.getZ());
                physicalCrates.put(loco, new PhysicalCrate(loco, id, plugin));
            }else{
                HuskyCrates.instance.logger.info("didn't crate"+loco.getExtent().getName() + ":" + loco.getX() + ":" + loco.getY() + ":" + loco.getZ());
            }
        }
        startParticleEffects();
    }

    public void startParticleEffects() {
        if (runner != null) {
            runner.cancel();
        }
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        runner = taskBuilder.execute(this::particleRunner).intervalTicks(1).submit(plugin);
    }

    public String getTypeFromLocation(Location<World> location) {
        if (!location.getTileEntity().isPresent()) {
            return null;
        }
        String prego = ((TileEntityCarrier) location.getTileEntity().get()).getInventory().getName().get();
        if (!prego.contains(plugin.huskyCrateIdentifier))
            return null;
        return prego.replace(plugin.huskyCrateIdentifier, "");
    }

    public void recognizeChest(Location<World> location) {
        if (location.getTileEntity().isPresent()) {
            String id = null;
            try {
                id = getTypeFromLocation(location);
            } catch (Exception e) {
            }
            if (id != null) {
                physicalCrates.put(location, new PhysicalCrate(location, id, plugin));
                updateConfig();
            }
        }
    }

    private void particleRunner() {
        try {
            for (Location<World> b : physicalCrates.keySet()) {
                PhysicalCrate c = physicalCrates.get(b);
                if (c.location.getBlock().getType() != BlockTypes.CHEST) {
                    c.as.remove();
                    physicalCrates.remove(b);
                    continue;
                }
                c.runParticles();
            }
        } catch (Exception e) {

        }
    }

    public void updateConfig() {
        try {

            CommentedConfigurationNode root = plugin.crateConfig.load();
            CommentedConfigurationNode node = root.getNode("cachedCrates");
            ArrayList<String> cached = new ArrayList<>();
            for (Location<World> b : physicalCrates.keySet()) {
                cached.add(b.getExtent().getName() + ":" + b.getX() + ":" + b.getY() + ":" + b.getZ());
            }

            node.setValue(cached);
            plugin.crateConfig.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
