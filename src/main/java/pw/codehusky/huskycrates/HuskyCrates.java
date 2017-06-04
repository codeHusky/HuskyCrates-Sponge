package pw.codehusky.huskycrates;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
import pw.codehusky.huskycrates.commands.*;
import pw.codehusky.huskycrates.commands.elements.CrateElement;
import pw.codehusky.huskycrates.crate.CrateUtilities;
import pw.codehusky.huskycrates.crate.PhysicalCrate;
import pw.codehusky.huskycrates.crate.VirtualCrate;
import pw.codehusky.huskycrates.lang.SharedLangData;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
@Plugin(id="huskycrates", name = "HuskyCrates", version = "1.2.0", description = "A CratesReloaded Replacement for Sponge? lol")
public class HuskyCrates {
    //@Inject
    public Logger logger;


    @Inject
    private PluginContainer pC;
    @Inject
    @DefaultConfig(sharedRoot = false)
    public ConfigurationLoader<CommentedConfigurationNode> crateConfig;

    private ArrayList<Extent> pendingExtents = new ArrayList<>();
    public Cause genericCause;
    public Scheduler scheduler;
    public CrateUtilities crateUtilities = new CrateUtilities(this);
    public String huskyCrateIdentifier = "☼1☼2☼3HUSKYCRATE-";
    public String armorStandIdentifier = "ABABABAB-CDDE-0000-8374-CAAAECAAAECA";
    public static HuskyCrates instance;
    public SharedLangData langData = new SharedLangData();
    public Set<BlockType> validCrateBlocks = new HashSet<>();
    @Listener
    public void gameInit(GamePreInitializationEvent event){
        logger = LoggerFactory.getLogger(pC.getName());
        CommentedConfigurationNode conf = null;
        try {
            conf = crateConfig.load();
            if(!conf.getNode("lang").isVirtual()) {
                langData = new SharedLangData(conf.getNode("lang"));
            }else
                logger.info("Using default lang settings.");

        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Lang load failed, using defaults.");
        }


        logger.info("Let's not init VCrates here anymore. ://)");
        instance = this;
    }
    @Listener
    public void gameStarted(GameStartedServerEvent event){



        CommandSpec key = CommandSpec.builder()
                .description(Text.of("Get a key for a specified crate."))
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.playerOrSource(Text.of("player")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                )
                .permission("huskycrates.key")
                .executor(new Key())
                .build();
        CommandSpec keyAll = CommandSpec.builder()
                .description(Text.of("Give everyone a specified amount of keys for a crate."))
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                )
                .permission("huskycrates.keyAll")
                .executor(new KeyAll())
                .build();
        CommandSpec wand = CommandSpec.builder()
                .description(Text.of("Give yourself a block/entity wand for crates."))
                .arguments(
                        new CrateElement(Text.of("type"))
                )
                .permission("huskycrates.wand")
                .executor(new Wand())
                .build();

        CommandSpec chest = CommandSpec.builder()
                .description(Text.of("Get the placeable crate item."))
                .permission("huskycrates.chest")
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.playerOrSource(Text.of("player")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                ).executor(new Chest())
                .build();

        CommandSpec crateSpec = CommandSpec.builder()
                .description(Text.of("Main crates command"))
                .child(key, "key")
                .child(chest, "chest")
                .child(keyAll, "keyAll")
                .child(wand, "wand")
                .arguments(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of(""))))
                .executor(new Crate(this))
                .build();

        scheduler = Sponge.getScheduler();
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));
        Sponge.getCommandManager().register(this, crateSpec, "crate");
        logger.info("Crates has been started.");
    }

    @Listener(order = Order.POST)
    public void postGameStart(GameStartedServerEvent event){
        Sponge.getScheduler().createTaskBuilder().async().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                try {
                    JSONObject obj = JsonReader.readJsonFromUrl("https://api.github.com/repos/codehusky/HuskyCrates-Sponge/releases");
                    String[] thisVersion = pC.getVersion().get().split("\\.");
                    String[] remoteVersion = obj.getJSONArray("releases").getJSONObject(0).getString("tag_name").replace("v","").split("\\.");
                    for(int i = 0; i < Math.min(remoteVersion.length,thisVersion.length); i++){
                        if(!thisVersion[i].equals(remoteVersion[i])){
                            if(Integer.parseInt(thisVersion[i]) > Integer.parseInt(remoteVersion[i])){
                                //we're ahead
                                logger.warn("----------------------------------------------------");
                                logger.warn("Running unreleased version. (Developer build?)");
                                logger.warn("----------------------------------------------------");
                            }else{
                                //we're behind
                                logger.warn("----------------------------------------------------");
                                logger.warn("Your version of HuskyCrates is out of date!");
                                logger.warn("Your version: v" + pC.getVersion().get());
                                logger.warn("Latest version: " + obj.getJSONArray("releases").getJSONObject(0).getString("tag_name"));
                                logger.warn("Update here: https://goo.gl/hgtPMR");
                                logger.warn("----------------------------------------------------");
                            }
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).submit(this);

        Sponge.getScheduler().createTaskBuilder().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                logger.info("Initalizing config...");
                if(!crateUtilities.hasInitalizedVirtualCrates){
                    crateUtilities.generateVirtualCrates(crateConfig);
                }
                CommentedConfigurationNode root = null;
                try {
                    root = crateConfig.load();
                    for(CommentedConfigurationNode node : root.getNode("positions").getChildrenList()){
                        Location<World> ee;
                        try {
                            ee = node.getNode("location").getValue(TypeToken.of(Location.class));
                        }catch(InvalidDataException err2){
                            logger.warn("Bug sponge developers about world UUIDs!");
                            ee = new Location<World>(Sponge.getServer().getWorld(node.getNode("location","WorldName").getString()).get(),node.getNode("location","X").getInt(),node.getNode("location","Y").getInt(),node.getNode("location","Z").getInt());
                        }
                        if(!crateUtilities.physicalCrates.containsKey(ee))
                            crateUtilities.physicalCrates.put(ee,new PhysicalCrate(ee,node.getNode("crateID").getString(),HuskyCrates.instance));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
                crateUtilities.hasInitalizedVirtualCrates = true; // doublecheck
                logger.info("Done initalizing config.");
                logger.info("Populating physical crates...");
                double count = 0;
                for(Extent e : pendingExtents){
                    count++;
                    crateUtilities.populatePhysicalCrates(e);
                    logger.info("Progress: " + Math.round((count / pendingExtents.size())*100)+ "%");
                }
                logger.info("Done populating physical crates.");
                logger.info("Initalization complete.");
            }
        }).delayTicks(1).submit(this);
    }

    @Listener(order = Order.POST)
    public void chunkLoad(LoadChunkEvent event){

            for (Entity e : event.getTargetChunk().getEntities()) {
                if (e instanceof ArmorStand) {
                    if(crateUtilities.hasInitalizedVirtualCrates) {
                        crateUtilities.populatePhysicalCrates(event.getTargetChunk());
                    }else{
                        pendingExtents.add(event.getTargetChunk());
                    }
                    return;
                }
            }
    }

    @Listener(order = Order.POST)
    public void worldLoaded(LoadWorldEvent event){
        if(crateUtilities.hasInitalizedVirtualCrates) {
            crateUtilities.populatePhysicalCrates(event.getTargetWorld());
        }else{
            pendingExtents.add(event.getTargetWorld());
        }
    }
    @Listener
    public void gameReloaded(GameReloadEvent event){
        langData = new SharedLangData("", "You won %a %R&rfrom a %C&r!","&e%p just won %a %R&r&e from a %C&r!","You need a %K&r to open this crate.");
        CommentedConfigurationNode conf = null;
        try {
            conf = crateConfig.load();
            if(!conf.getNode("lang").isVirtual())
                langData = new SharedLangData(conf.getNode("lang"));
            else
                logger.info("Using default lang settings.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Lang load failed, using defaults.");
        }
        crateUtilities.generateVirtualCrates(crateConfig);
        CommentedConfigurationNode root = null;
        try {
            root = crateConfig.load();
            for(CommentedConfigurationNode node : root.getNode("positions").getChildrenList()){
                Location<World> ee = node.getNode("location").getValue(TypeToken.of(Location.class));
                if(!crateUtilities.physicalCrates.containsKey(ee))
                    crateUtilities.physicalCrates.put(ee,new PhysicalCrate(ee,node.getNode("crateID").getString(),HuskyCrates.instance));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        for(World e: Sponge.getServer().getWorlds()){
            crateUtilities.populatePhysicalCrates(e);
        }

    }
    private boolean blockCanBeCrate(BlockType type){
        return type==BlockTypes.CHEST ||
                type==BlockTypes.TRAPPED_CHEST ||
                type==BlockTypes.ENDER_CHEST;
    }

    @Listener
    public void placeBlock(ChangeBlockEvent event){
        if(event.getCause().root() instanceof Player) {
            Player plr = (Player) event.getCause().root();
            if (event instanceof ChangeBlockEvent.Place || event instanceof ChangeBlockEvent.Break) {
                BlockType t = event.getTransactions().get(0).getOriginal().getLocation().get().getBlock().getType();
                Location<World> location = event.getTransactions().get(0).getOriginal().getLocation().get();
                location.getBlock().toContainer().set(DataQuery.of("rock"), 1);
                //location.getBlock().with()
                if (validCrateBlocks.contains(t)) {
                    //crateUtilities.recognizeChest(event.getTransactions().get(0).getOriginal().getLocation().get());
                    if(event instanceof ChangeBlockEvent.Place) {
                        if (plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                            Optional<Object> tt = plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer().get(DataQuery.of("UnsafeData", "crateID"));
                            if (tt.isPresent()) {
                                String crateID = tt.get().toString();
                                if(!plr.hasPermission("huskycrates.tester")) {
                                    event.setCancelled(true);
                                    return;
                                }
                                if(!crateUtilities.physicalCrates.containsKey(location))
                                    crateUtilities.physicalCrates.put(location, new PhysicalCrate(location, crateID, this));

                                crateUtilities.physicalCrates.get(location).createHologram();
                                updatePhysicalCrates();
                            }
                        }
                    }
                }else{
                    //break

                    if(crateUtilities.physicalCrates.containsKey(location)){
                        if(!plr.hasPermission("huskycrates.tester")) {
                            event.setCancelled(true);
                            return;
                        }
                        crateUtilities.flag = true;
                        crateUtilities.physicalCrates.get(location).as.remove();
                        crateUtilities.physicalCrates.remove(location);
                        updatePhysicalCrates();
                    }
                }
            }
        }
    }
    private boolean updating = false;
    public void updatePhysicalCrates() {
        if(updating)
            return;
        updating = true;
        try {
            CommentedConfigurationNode root = crateConfig.load();
            root.getNode("positions").setValue(null);
            for(Object ob: ((HashMap)crateUtilities.physicalCrates.clone()).keySet()) {
                Location<World> e = (Location<World>)ob;
                CommentedConfigurationNode node = root.getNode("positions").getAppendedNode();
                node.getNode("location").setValue(TypeToken.of(Location.class),e);
                //System.out.println("echo");
                try {
                    node.getNode("crateID").setValue(crateUtilities.physicalCrates.get(e).vc.id);
                }catch(NullPointerException err){
                    System.out.println("removing a crate!");
                    node.setValue(null);
                    crateUtilities.physicalCrates.remove(ob);
                    logger.warn("Invalid crate at (" + e.getPosition().getFloorX() + ", " + e.getPosition().getFloorY() + ", " + e.getPosition().getFloorZ() + ")!");
                }
            }
            crateConfig.save(root);
        } catch (IOException e) {

             e.printStackTrace();

        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        crateUtilities.flag = false;
        updating = false;
    }

    @Listener
    public void crateInteract(InteractBlockEvent.Secondary.MainHand event){
        //System.out.println(crateUtilities.physicalCrates.keySet());
        //Player pp = (Player) event.getCause().root();

        //ItemStack ss = pp.getItemInHand(HandTypes.MAIN_HAND).get();
        //System.out.println(ss.toContainer().get(DataQuery.of("UnsafeData")).get());

        //pp.getInventory().offer(ItemStack.builder().fromContainer(ss.toContainer().set(DataQuery.of("UnsafeDamage"),3)).build());*/
        if(!event.getTargetBlock().getLocation().isPresent())
            return;

        Location<World> blk = event.getTargetBlock().getLocation().get();
        //System.out.println(blk.getBlock().getType());
        if(validCrateBlocks.contains(blk.getBlockType())) {
            Player plr = (Player)event.getCause().root();

            if(crateUtilities.physicalCrates.containsKey(blk)){
                String crateType = crateUtilities.physicalCrates.get(blk).vc.id;
                VirtualCrate vc = crateUtilities.getVirtualCrate(crateType);
                crateUtilities.physicalCrates.get(blk).createHologram();
                if(vc.crateBlockType == blk.getBlockType()){
                    event.setCancelled(true);
                }else{
                    return;
                }
                //crateUtilities.recognizeChest(te.getLocation());
                if(plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                    ItemStack inhand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                    if(inhand.getItem() == vc.getKeyType()) {

                        if(inhand.toContainer().get(DataQuery.of("UnsafeData","crateID")).isPresent()) {
                            String id = inhand.toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString();
                            if (id.equals(crateType)) {
                                if (!plr.hasPermission("huskycrates.tester")) {
                                    if (inhand.getQuantity() == 1)
                                        plr.setItemInHand(HandTypes.MAIN_HAND, null);
                                    else {
                                        ItemStack tobe = inhand.copy();
                                        tobe.setQuantity(tobe.getQuantity() - 1);
                                        plr.setItemInHand(HandTypes.MAIN_HAND, tobe);
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
                plr.playSound(SoundTypes.BLOCK_ANVIL_LAND,blk.getPosition(),0.3);
                try {
                    plr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.langData.formatter(vc.langData.prefix + vc.langData.noKeyMessage,null,plr,vc,null)
                    ));
                }catch(Exception e){
                    plr.sendMessage(Text.of(TextColors.RED,"Critical crate failure, contact the administrator. (Admins, check console!)"));
                    e.printStackTrace();
                }
            }


        }
    }

    @Listener
    public void entityInteract(InteractEntityEvent.Secondary.MainHand event){
        //event.getTargetEntity().(event.getTargetEntity().toContainer().set(DataQuery.of("UnsafeData","crateID"),"blap"));
        //event.getTargetEntity().()
        //System.out.println(event.getTargetEntity().toContainer().get(DataQuery.of("UnsafeData","crateID")));
        if(event.getCause().root() instanceof Player) {
            Player plr = (Player) event.getCause().root();
            if(plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                ItemStack hand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                if(hand.getItem() == ItemTypes.BLAZE_ROD) {
                    if(hand.toContainer().get(DataQuery.of("UnsafeData","crateID")).isPresent()) {
                        if(crateUtilities.physicalCrates.containsKey(event.getTargetEntity().getLocation())){
                            event.getTargetEntity().offer(Keys.AI_ENABLED,false);
                            event.getTargetEntity().offer(Keys.HAS_GRAVITY,false);
                            event.getTargetEntity().offer(Keys.)
                        }else {
                            System.out.println(event.getTargetEntity().getLocation().getBlockPosition());
                            crateUtilities.physicalCrates.put(event.getTargetEntity().getLocation(), new PhysicalCrate(event.getTargetEntity().getLocation(), hand.toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString(), this));
                            crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()).createHologram();
                            updatePhysicalCrates();
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if(crateUtilities.physicalCrates.containsKey(event.getTargetEntity().getLocation())){
                String crateType = crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()).vc.id;
                VirtualCrate vc = crateUtilities.getVirtualCrate(crateType);
                crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()).createHologram();
                //crateUtilities.recognizeChest(te.getLocation());
                event.setCancelled(true);
                if(plr.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                    ItemStack inhand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                    if(inhand.getItem() == vc.getKeyType()) {

                        if(inhand.toContainer().get(DataQuery.of("UnsafeData","crateID")).isPresent()) {
                            String id = inhand.toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString();
                            if (id.equals(crateType)) {
                                if (!plr.hasPermission("huskycrates.tester")) {
                                    if (inhand.getQuantity() == 1)
                                        plr.setItemInHand(HandTypes.MAIN_HAND, null);
                                    else {
                                        ItemStack tobe = inhand.copy();
                                        tobe.setQuantity(tobe.getQuantity() - 1);
                                        plr.setItemInHand(HandTypes.MAIN_HAND, tobe);
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
                plr.playSound(SoundTypes.BLOCK_ANVIL_LAND,event.getTargetEntity().getLocation().getPosition(),0.3);
                try {
                    plr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.langData.formatter(vc.langData.prefix + vc.langData.noKeyMessage,null,plr,vc,null)
                    ));
                }catch(Exception e){
                    plr.sendMessage(Text.of(TextColors.RED,"Critical crate failure, contact the administrator. (Admins, check console!)"));
                    e.printStackTrace();
                }
            }
        }

    }

    public CrateUtilities getCrateUtilities() {
        return crateUtilities;
    }

    public String getHuskyCrateIdentifier() {
        return huskyCrateIdentifier;
    }
}
