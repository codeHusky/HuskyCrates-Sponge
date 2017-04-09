package pw.codehusky.huskycrates.crate;

import com.flowpowered.math.vector.Vector3i;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import pw.codehusky.huskycrates.HuskyCrates;
import pw.codehusky.huskycrates.crate.views.NullCrateView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class CrateUtilities {

    private HashMap<String, VirtualCrate> crateTypes;
    private HashMap<Location<World>, PhysicalCrate> physicalCrates;
    private HashMap<String, ItemStack> keys;
    private HuskyCrates plugin;
    private ArrayList<Location<World>> toCheck;
    private HashMap<World, Vector3i> toCheckChunk;
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
        toCheckChunk = new HashMap<>();
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
            HuskyCrates.instance.logger.info(root.getNode("cachedCrates").getChildrenList().size() + "");
            for (CommentedConfigurationNode cached : root.getNode("cachedCrates").getChildrenList()) {
                try {

                    String[] stringList = ((String) cached.getValue()).split(":");

                    World world = Sponge.getServer().getWorld(stringList[0]).get();
                    Location loc = world.getLocation(Double.parseDouble(stringList[1]), Double.parseDouble(stringList[2]),
                            Double.parseDouble(stringList[3]));
                    if (world.getChunkAtBlock(loc.getBlockPosition()).isPresent()) {
                        this.addCrate(loc);
                    } else {
                        toCheck.add(loc);
                        toCheckChunk.put(world, loc.getChunkPosition());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cached.setValue(null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void populatePhysicalCrates(Extent bit) {
        ArrayList<Location<World>> eep = new ArrayList<>();
        for (Entity ent : bit.getEntities()) {
            if (ent instanceof ArmorStand) {
                ArmorStand arm = (ArmorStand) ent;
                if (arm.getCreator().isPresent()) {
                    if (arm.getCreator().get().equals(UUID.fromString(HuskyCrates.instance.getArmorStandIdentifier()))) {
                        Location woot = arm.getLocation().copy().sub(PhysicalCrate.offset);

                        if (physicalCrates.containsKey(woot)) {
                            continue;
                        }
                        eep.add(woot);
                        //arm.remove();
                    } else {

                    }
                }
            }
        }

        for (Location<World> loco : eep) {
            if (!bit.containsBlock(loco.getBlockPosition())) {
                return;
            }

            this.addCrate(loco);
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
            location.getExtent().getChunkAtBlock(location.getBlockPosition()).isPresent();
            return null;
        }
        String prego = ((TileEntityCarrier) location.getTileEntity().get()).getInventory().getName().get();
        if (!prego.contains(HuskyCrates.instance.getHuskyCrateIdentifier())) {
            return null;
        }
        return prego.replace(HuskyCrates.instance.getHuskyCrateIdentifier(), "");
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

    public void checkChunk(Chunk chunk) {
        if (toCheckChunk != null && toCheckChunk.containsKey(chunk.getWorld()) && toCheckChunk.containsValue(chunk.getPosition())) {
            ArrayList<Location<World>> found = new ArrayList<>();
            for (Location<World> location : toCheck) {
                if (location.getExtent().equals(chunk.getWorld()) && location.getChunkPosition().equals(chunk.getPosition())) {
                    this.addCrate(location);
                    found.add(location);
                }
            }
            if (!found.isEmpty()) {
                toCheckChunk.remove(chunk.getPosition());
                for (Location<World> location : found) {
                    toCheck.remove(found);
                }

            }
        }
    }


    private void addCrate(Location location) {
        String id = getTypeFromLocation(location);
        if (id != null) {
            HuskyCrates.instance.logger.info("added crate" + location.getX() + ":" + location.getY() + ":" + location.getZ());
            physicalCrates.put(location, new PhysicalCrate(location, id, plugin));
        } else {
            HuskyCrates.instance.logger.info("didn't crate" + location.getX() + ":" + location.getY() + ":" + location.getZ());
        }

    }

    public List<String> getCrateTypes() {
        return new ArrayList<String>(crateTypes.keySet());
    }


    public ItemStack getCrateItem(String id) {
        VirtualCrate vc = getVirtualCrate(id);
        if (vc != null) {
            return ItemStack.builder()
                    .itemType(ItemTypes.CHEST)
                    .quantity(1)
                    .add(Keys.DISPLAY_NAME, Text.of(HuskyCrates.instance.getHuskyCrateIdentifier() + id)).build();
        }
        return null;
    }

    public ItemStack getCrateKey(String id) {
        return this.getCrateKey(id, 1);
    }

    public ItemStack getCrateKey(String id, int quantity) {
        VirtualCrate vc = getVirtualCrate(id);
        if (vc != null) {
            ItemStack key = ItemStack.builder()
                    .itemType(ItemTypes.RED_FLOWER)
                    .quantity(quantity)
                    .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(vc.displayName + " Key")).build();
            ArrayList<Text> bb = new ArrayList<>();
            bb.add(Text.of(TextColors.WHITE, "A key for a ", TextSerializers.FORMATTING_CODE.deserialize(vc.displayName), TextColors.WHITE, "."));
            bb.add(Text.of(TextColors.WHITE, "crate_" + id));
            key.offer(Keys.ITEM_LORE, bb);
            return key;
        }
        return null;
    }
}
