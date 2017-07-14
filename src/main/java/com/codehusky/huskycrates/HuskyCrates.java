package com.codehusky.huskycrates;

import com.codehusky.huskycrates.commands.*;
import com.codehusky.huskycrates.crate.CrateUtilities;
import com.codehusky.huskycrates.crate.PhysicalCrate;
import com.codehusky.huskycrates.crate.VirtualCrate;
import com.codehusky.huskycrates.crate.config.CrateReward;
import com.codehusky.huskyui.StateContainer;
import com.codehusky.huskyui.states.Page;
import com.codehusky.huskyui.states.State;
import com.codehusky.huskyui.states.action.Action;
import com.codehusky.huskyui.states.action.ActionType;
import com.codehusky.huskyui.states.element.ActionableElement;
import com.codehusky.huskyui.states.element.Element;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Dependency;
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
import com.codehusky.huskycrates.commands.elements.CrateElement;
import com.codehusky.huskycrates.lang.LangData;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by lokio on 12/28/2016.
 */
@SuppressWarnings("deprecation")
@Plugin(id="huskycrates", name = "HuskyCrates", version = "1.6.0", description = "A CratesReloaded Replacement for Sponge? lol",dependencies = {@Dependency(id="huskyui",version = "0.2.1")})
public class HuskyCrates {
    //@Inject
    public Logger logger;


    @Inject
    public PluginContainer pC;
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
    public LangData langData = new LangData();
    public Set<BlockType> validCrateBlocks = new HashSet<>();
    private boolean forceStop = false;
    @Listener
    public void gameInit(GamePreInitializationEvent event){
        logger = LoggerFactory.getLogger(pC.getName());
        instance = this;
        for(PluginContainer pc: Sponge.getPluginManager().getPlugins()){
            if(pc.getId().equalsIgnoreCase("inventorytweaks")||pc.getId().equalsIgnoreCase("inventorysorter")||pc.getId().equalsIgnoreCase("mousetweaks")){
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.error(pc.getName() + " is loaded! This plugin or mod is on a blacklist for HuskyCrates, and as a result, HuskyCrates is not starting. ");
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                forceStop = true;

            }
        }
        if(forceStop)
            return;

        CommentedConfigurationNode conf = null;
        try {
            conf = crateConfig.load();
            if(!conf.getNode("lang").isVirtual()) {
                langData = new LangData(conf.getNode("lang"));
            }else
                logger.info("Using default lang settings.");

        } catch (Exception e) {
            crateUtilities.exceptionHandler(e);
        }


        //logger.info("Let's not init VCrates here anymore. ://)");


    }
    @Listener
    public void gameStarted(GameStartedServerEvent event){

        if(forceStop) {
            logger.error("Since a blacklisted mod is loaded, HuskyCrates will not start. Please check higher in your logs for the reasoning.");
            return;
        }

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
        CommandSpec vKey = CommandSpec.builder()
                .description(Text.of("Credits a user an amount of virtual keys for a specified crate."))
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.userOrSource(Text.of("player")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                )
                .permission("huskycrates.vKey")
                .executor(new VirtualKey())
                .build();
        CommandSpec vKeyAll = CommandSpec.builder()
                .description(Text.of("Credits everyone a specified amount of virtual keys for a crate."))
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                )
                .permission("huskycrates.vKeyAll")
                .executor(new VirtualKeyAll())
                .build();
        CommandSpec wand = CommandSpec.builder()
                .description(Text.of("Give Runningelf an entity wand for crates."))
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
        CommandSpec keyBal = CommandSpec.builder()
                .description(Text.of("Check your (or another user's) virtual key balance."))
                .arguments(
                        GenericArguments.userOrSource(Text.of("player"))
                )
                .permission("huskycrates.keybal.self")
                .executor(new KeyBal())
                .build();
        CommandSpec deposit = CommandSpec.builder()
                .description(Text.of("Transfer the held physical key(s) into your virtual key balance."))
                .permission("huskycrates.depositkey")
                .executor(new DepositKey())
                .build();
        CommandSpec withdraw = CommandSpec.builder()
                .description(Text.of("Convert an amount of virtual key(s) into a physical key(s)."))
                .permission("huskycrates.withdrawkey")
                .arguments(
                        new CrateElement(Text.of("type")),
                        GenericArguments.optional(GenericArguments.integer(Text.of("quantity")))
                )
                .executor(new WithdrawKey())
                .build();
        CommandSpec crateSpec = CommandSpec.builder()
                .description(Text.of("Main crates command"))
                .child(key, "key")
                .child(chest, "chest")
                .child(keyAll, "keyAll")
                .child(vKey, "vkey","virtualkey")
                .child(vKeyAll, "vkeyall","virtualkeyall")
                .child(keyBal,"bal","keybal")
                .child(wand, "wand")
                .child(deposit, "deposit","depositkey","ptov")
                .child(withdraw, "withdraw","withdrawkey","vtop")
                .arguments(GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of(""))))
                .executor(new Crate(this))
                .build();
        CommandSpec huskySpec = CommandSpec.builder()
                .executor(new Husky(this))
                .build();

        scheduler = Sponge.getScheduler();
        genericCause = Cause.of(NamedCause.of("PluginContainer",pC));
        Sponge.getCommandManager().register(this, crateSpec, "crate");
        Sponge.getCommandManager().register(this, huskySpec, "husky","huskycrates","hc");
        logger.info("Crates has been started.");
    }

    public OutOfDateData oodd = new OutOfDateData();
    public void checkVersion() {
        Sponge.getScheduler().createTaskBuilder().async().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                try {
                    JSONObject obj = JsonReader.readJsonFromUrl("https://api.github.com/repos/codehusky/HuskyCrates-Sponge/releases");
                    boolean foundLatest = false;
                    JSONObject newPre = null;
                    for(int i = 0; i < obj.getJSONArray("releases").length() && !foundLatest; i++) {
                        if(obj.getJSONArray("releases").getJSONObject(i).getBoolean("prerelease") && !pC.getVersion().get().contains("PRE")){
                            if(newPre == null) {
                                newPre = obj.getJSONArray("releases").getJSONObject(i);
                                if(newPre.getString("tag_name").contains(pC.getVersion().get())){
                                    newPre = null;
                                }
                            }
                            continue;
                        }
                        foundLatest = true;

                        if(obj.getJSONArray("releases").getJSONObject(i).getString("tag_name").equals("v" + pC.getVersion().get())){
                            logger.info("----------------------------------------------------");
                            logger.info("HuskyCrates is up to date.");
                            logger.info("Running v" + pC.getVersion().get());
                            logger.info("----------------------------------------------------");
                        } else if (newPre != null) {
                            logger.warn("----------------------------------------------------");
                            logger.warn("HuskyCrates is up to date, but a pre-release is out.");
                            logger.warn("Running v" + pC.getVersion().get());
                            logger.warn("PreRelease: " + newPre.getString("tag_name"));
                            logger.warn("----------------------------------------------------");
                        }else {
                            String latestTag = obj.getJSONArray("releases").getJSONObject(0).getString("tag_name");
                            boolean preCheck = latestTag.contains(pC.getVersion().get()) && obj.getJSONArray("releases").getJSONObject(0).getBoolean("prerelease");
                            boolean majorCheck = Integer.parseInt(latestTag.replace("v","").substring(0,1)) > Integer.parseInt(pC.getVersion().get().substring(0,1));
                            boolean minorCheck = !majorCheck && Integer.parseInt(latestTag.replace("v","").substring(2,3)) > Integer.parseInt(pC.getVersion().get().substring(2,3));
                            boolean bugCheck = !minorCheck && Integer.parseInt(latestTag.replace("v","").substring(4,5)) > Integer.parseInt(pC.getVersion().get().substring(4,5));
                            if(preCheck || majorCheck || minorCheck || bugCheck){
                                logger.warn("----------------------------------------------------");
                                logger.warn("HuskyCrates is running an unreleased version.");
                                logger.warn("Running v" + pC.getVersion().get());
                                logger.warn("----------------------------------------------------");
                            }else {
                                //we're behind
                                oodd = new OutOfDateData(obj.getJSONArray("releases").getJSONObject(i).getString("tag_name"));
                                logger.error("----------------------------------------------------");
                                logger.error("Your version of HuskyCrates is out of date!");
                                logger.error("Latest: " + obj.getJSONArray("releases").getJSONObject(i).getString("tag_name"));
                                logger.error("Running: v" + pC.getVersion().get());
                                logger.error("Check GitHub Releases for downloads.");
                                logger.error("----------------------------------------------------");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).submit(this);
    }
    @Listener(order = Order.POST)
    public void postGameStart(GameStartedServerEvent event){
        checkVersion();
        if(forceStop) {
            //logger.error("Since a blacklisted mod is loaded, HuskyCrates will not start. Please check higher in your logs for the reasoning.");
            return;
        }
        Sponge.getScheduler().createTaskBuilder().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                logger.info("Deleting existing armor stands...");
                for(World bit: Sponge.getServer().getWorlds()) {
                    for (Entity ent : bit.getEntities()) {
                        if (ent instanceof ArmorStand) {
                            ArmorStand arm = (ArmorStand) ent;
                            if (arm.getCreator().isPresent()) {
                                if (arm.getCreator().get().equals(UUID.fromString(armorStandIdentifier))) {
                                    arm.remove();
                                }
                            }
                        }
                    }
                }
                logger.info("Initalizing config...");
                if(!crateUtilities.hasInitalizedVirtualCrates){
                    crateUtilities.generateVirtualCrates(crateConfig);
                }
                crateUtilities.hasInitalizedVirtualCrates = true; // doublecheck
                logger.info("Done initalizing config.");
                logger.info("Populating physical crates...");
                CommentedConfigurationNode root = null;
                try {
                    root = crateConfig.load();
                    double max = root.getNode("positions").getChildrenList().size();
                    double count = 0;
                    for(CommentedConfigurationNode node : root.getNode("positions").getChildrenList()){
                        count++;
                        Location<World> ee;
                        try {
                            ee = node.getNode("location").getValue(TypeToken.of(Location.class));
                        }catch(InvalidDataException err2){
                            logger.warn("Bug sponge developers about world UUIDs!");
                            ee = new Location<World>(Sponge.getServer().getWorld(node.getNode("location","WorldName").getString()).get(),node.getNode("location","X").getDouble(),node.getNode("location","Y").getDouble(),node.getNode("location","Z").getDouble());
                        }
                        if(!crateUtilities.physicalCrates.containsKey(ee))
                            crateUtilities.physicalCrates.put(ee,new PhysicalCrate(ee,node.getNode("crateID").getString(),HuskyCrates.instance,node.getNode("location","BlockType").getString().equals("minecraft:air")));
                        logger.info("PROGRESS: "  + Math.round((count/max)*100) + "%");
                    }
                } catch (Exception e) {
                    crateUtilities.exceptionHandler(e);
                    if(event.getCause().root() instanceof Player){
                        CommandSource cs = (CommandSource) event.getCause().root();
                        cs.sendMessage(Text.of(TextColors.GOLD,"HuskyCrates",TextColors.WHITE,":",TextColors.RED," An error has occured. Please check the console for more information."));
                    }
                    return;
                }
                crateUtilities.startParticleEffects();
                logger.info("Done populating physical crates.");
                logger.info("Initalization complete.");
            }
        }).delayTicks(1).submit(this);
    }

    @Listener
    public void onArmorStand(SpawnEntityEvent.ChunkLoad event){
        for(Entity e : event.getEntities()){
            if(!(e instanceof ArmorStand))
                continue;
            ArmorStand as = (ArmorStand) e;
            if (as.getCreator().isPresent()) {
                if (as.getCreator().get().equals(UUID.fromString(armorStandIdentifier))) {
                    as.remove();
                }
            }
        }
    }
    @Listener
    public void gameReloaded(GameReloadEvent event){
        langData = null;
        if(forceStop) {
            if(event.getCause().root() instanceof Player){
                CommandSource cs = (CommandSource) event.getCause().root();
                cs.sendMessage(Text.of(TextColors.GOLD,"HuskyCrates",TextColors.WHITE,":",TextColors.RED," HuskyCrates is currently force stopped. Check the console for more information."));
            }
            return;
        }
        if(event.getCause().root() instanceof Player){
            CommandSource cs = (CommandSource) event.getCause().root();
            cs.sendMessage(Text.of(TextColors.GOLD,"HuskyCrates",TextColors.WHITE,":",TextColors.YELLOW," Please check console to verify that any config modifications you've done are valid."));
        }
        for(World bit: Sponge.getServer().getWorlds()) {
            for (Entity ent : bit.getEntities()) {
                if (ent instanceof ArmorStand) {
                    ArmorStand arm = (ArmorStand) ent;
                    if (arm.getCreator().isPresent()) {
                        if (arm.getCreator().get().equals(UUID.fromString(armorStandIdentifier))) {
                            arm.remove();
                        }
                    }
                }
            }
        }
        langData = new LangData();
        CommentedConfigurationNode root = null;
        try {
            root = crateConfig.load();
            if(!root.getNode("lang").isVirtual())
                langData = new LangData(root.getNode("lang"));
            else
                logger.info("Using default lang settings.");
            crateUtilities.generateVirtualCrates(crateConfig);
            for(CommentedConfigurationNode node : root.getNode("positions").getChildrenList()){
                Location<World> ee;
                try {
                    ee = node.getNode("location").getValue(TypeToken.of(Location.class));
                }catch(InvalidDataException err2){
                    logger.warn("Bug sponge developers about world UUIDs!");
                    ee = new Location<World>(Sponge.getServer().getWorld(node.getNode("location","WorldName").getString()).get(),node.getNode("location","X").getDouble(),node.getNode("location","Y").getDouble(),node.getNode("location","Z").getDouble());
                }
                if(!crateUtilities.physicalCrates.containsKey(ee))
                    crateUtilities.physicalCrates.put(ee,new PhysicalCrate(ee,node.getNode("crateID").getString(),HuskyCrates.instance,node.getNode("location","BlockType").getString().equals("minecraft:air")));
            }
            crateUtilities.startParticleEffects();
        } catch (Exception e) {
            crateUtilities.exceptionHandler(e);
            if(event.getCause().root() instanceof Player){
                CommandSource cs = (CommandSource) event.getCause().root();
                cs.sendMessage(Text.of(TextColors.GOLD,"HuskyCrates",TextColors.WHITE,":",TextColors.RED," An error has occured. Please check the console for more information."));
            }
            return;
        }
        checkVersion();
        for(Player plr: Sponge.getServer().getOnlinePlayers()){
            notifyOutOfDate(plr);
        }
    }
    private boolean blockCanBeCrate(BlockType type){
        return type==BlockTypes.CHEST ||
                type==BlockTypes.TRAPPED_CHEST ||
                type==BlockTypes.ENDER_CHEST;
    }

    @Listener(order=Order.PRE)
    public void placeBlock(ChangeBlockEvent event){
        if(forceStop) {
            return;
        }
        if(event.getCause().root() instanceof Player) {
            Player plr = (Player) event.getCause().root();

            if (event instanceof ChangeBlockEvent.Place || event instanceof ChangeBlockEvent.Break) {
                BlockType t = event.getTransactions().get(0).getOriginal().getLocation().get().getBlock().getType();
                Location<World> location = event.getTransactions().get(0).getOriginal().getLocation().get();
                location.getBlock().toContainer().set(DataQuery.of("rock"), 1);
                //location.getBlock().with()
                //System.out.println(event instanceof ChangeBlockEvent.Break);
                if (validCrateBlocks.contains(t)) {
                    //System.out.println("valid block");
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
                                    crateUtilities.physicalCrates.put(location, new PhysicalCrate(location, crateID, this,false));

                                crateUtilities.physicalCrates.get(location).createHologram();
                                updatePhysicalCrates();
                                return;
                            }
                        }
                    }
                }else if(event instanceof ChangeBlockEvent.Break){
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
                    //System.out.println("removing a crate!");
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
    public void keyHandler(Player plr, int keyResult, VirtualCrate vc, Location<World> blk,String crateType){
        if(keyResult == 1  || keyResult == 2) {
            if(!vc.freeCrate && keyResult == 1) {
                ItemStack inhand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                if (!plr.hasPermission("huskycrates.tester")) {
                    if (inhand.getQuantity() == 1)
                        plr.setItemInHand(HandTypes.MAIN_HAND, null);
                    else {
                        ItemStack tobe = inhand.copy();
                        tobe.setQuantity(tobe.getQuantity() - 1);
                        plr.setItemInHand(HandTypes.MAIN_HAND, tobe);
                    }
                }else{
                    plr.sendMessage(Text.of(TextColors.GRAY,"Since you are a tester, a key was not taken."));
                }
            }else if(keyResult == 2){
                if(!plr.hasPermission("huskycrates.tester")) {
                    crateUtilities.takeVirtualKey(plr, vc);
                    plr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                            vc.getLangData().formatter(vc.getLangData().vkeyUseNotifier, null, plr, vc, null, crateUtilities.physicalCrates.get(blk), null)
                    ));
                }else{
                    plr.sendMessage(Text.of(TextColors.GRAY,"Since you are a tester, a virtual key was not taken."));
                    plr.sendMessage(Text.of(TextColors.GRAY,"You can remove them manually by withdrawing your keys."));
                }
            }
            Task.Builder upcoming = scheduler.createTaskBuilder();
            crateUtilities.physicalCrates.get(blk).handleUse(plr);
            upcoming.execute(() -> {
                crateUtilities.launchCrateForPlayer(crateType, plr, this);
            }).delayTicks(1).submit(this);
            return;

        }else if(keyResult == -1){
            plr.playSound(SoundTypes.BLOCK_IRON_DOOR_CLOSE,blk.getPosition(),1);
            try {
                plr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                        vc.getLangData().formatter(vc.getLangData().freeCrateWaitMessage,null,plr,vc,null,crateUtilities.physicalCrates.get(blk),null)
                ));
            }catch(Exception e){
                plr.sendMessage(Text.of(TextColors.RED,"Critical crate failure, contact the administrator. (Admins, check console!)"));
                e.printStackTrace();
            }
            return;
        }else if(keyResult == -2){
            plr.sendMessage(Text.of(TextColors.RED,"Unfortunately, the key you attempted to use is a legacy key. Please contact a server administrator for details."));
            plr.sendMessage(Text.of(TextColors.RED,"This incident has been logged to the console."));
            String id = plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString();
            for(Player player: Sponge.getServer().getOnlinePlayers()){
                if(player.hasPermission("huskycrates.adminlog")){
                    player.sendMessage(Text.of(TextColors.DARK_RED,"[HuskyCrates][Legacy/Dupe] ", TextColors.RED, plr.getName() + " used a legacy " + id + " key"));
                    player.playSound(SoundTypes.ENTITY_CAT_HISS,player.getLocation().getPosition(),1.0);
                }
            }
            logger.error("[DUPE LOG] " + plr.getName() + " attempted to use a legacy " + id + " key.");
            plr.setItemInHand(HandTypes.MAIN_HAND,null);
            return;
        }else if(keyResult == -3){
            plr.sendMessage(Text.of(TextColors.RED,"You appear to have used a duplicated key. Fortunately, we caught you."));
            plr.sendMessage(Text.of(TextColors.RED,"This incident has been reported to admins and the console."));
            String id = plr.getItemInHand(HandTypes.MAIN_HAND).get().toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString();
            for(Player player: Sponge.getServer().getOnlinePlayers()){
                if(player.hasPermission("huskycrates.adminlog")){
                    player.sendMessage(Text.of(TextColors.DARK_RED,"[HuskyCrates][Dupe] ", TextColors.RED, plr.getName() + " used a duplicated " + id + " key"));
                    player.playSound(SoundTypes.ENTITY_CAT_HISS,player.getLocation().getPosition(),1.0);
                }
            }
            logger.error("[DUPE LOG] " + plr.getName() + " used a duplicated " + id + " key.");
            plr.setItemInHand(HandTypes.MAIN_HAND,null);
            return;
        }
        plr.playSound(SoundTypes.BLOCK_ANVIL_LAND,blk.getPosition(),0.3);
        try {
            plr.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                    vc.getLangData().formatter(vc.getLangData().noKeyMessage,null,plr,vc,null,null,null)
            ));
        }catch(Exception e){
            plr.sendMessage(Text.of(TextColors.RED,"Critical crate failure, contact the administrator. (Admins, check console!)"));
            e.printStackTrace();
        }
    }
    @Listener
    public void crateInteract(InteractBlockEvent.Secondary.MainHand event){
        if(forceStop) {
            return;
        }
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
                keyHandler(plr,crateUtilities.isAcceptedKey(crateUtilities.physicalCrates.get(blk),plr.getItemInHand(HandTypes.MAIN_HAND),plr),vc,blk,crateType);
            }


        }
    }

    @Listener
    public void onCrateRightClick(InteractBlockEvent.Primary.MainHand event){
        //System.out.println("primary interaction");
        if(!(event.getCause().root() instanceof Player)) return;
        Player plr = (Player) event.getCause().root();
        if(!event.getTargetBlock().getLocation().isPresent()) return;
        Location location = event.getTargetBlock().getLocation().get();
        if(crateUtilities.physicalCrates.containsKey(location)){
            if(!plr.hasPermission("huskycrates.tester") || plr.hasPermission("huskycrates.tester") && plr.getGameModeData().get(Keys.GAME_MODE).get() != GameModes.CREATIVE) {
                event.setCancelled(true);
                listRewards(plr,crateUtilities.physicalCrates.get(location).vc);
            }
        }
    }
    public void listRewards(Player player, VirtualCrate vc){
        if(!vc.showRewardsOnRight) return;
        /* Home */
        StateContainer test = new StateContainer();
        Page.PageBuilder rewards = Page.builder();
        rewards.setAutoPaging(true);
        rewards.setTitle(TextSerializers.FORMATTING_CODE.deserialize(vc.displayName + " Rewards"));
        rewards.setEmptyStack(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.BLACK)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_GRAY, "HuskyCrates")).build());
        for(Object[] e : vc.getItemSet()){
            CrateReward rew = (CrateReward)e[1];
            rewards.addElement(new Element(rew.getDisplayItem()));
        }
        test.setInitialState(rewards.build("rewards"));
        test.launchFor(player);
    }
    @Listener
    public void entityMove(MoveEntityEvent event){
        if(forceStop) {
            return;
        }
        if(crateUtilities.physicalCrates.containsKey(event.getFromTransform().getLocation())){
            event.setCancelled(true);
        }
    }
    @Listener
    public void entityInteract(InteractEntityEvent.Secondary.MainHand event){
        if(forceStop) {
            return;
        }
        //event.getTargetEntity().(event.getTargetEntity().toContainer().set(DataQuery.of("UnsafeData","crateID"),"blap"));
        //event.getTargetEntity().()
        //System.out.println(event.getTargetEntity().toContainer().get(DataQuery.of("UnsafeData","crateID")));
        if(event.getCause().root() instanceof Player) {
            Player plr = (Player) event.getCause().root();
            if(plr.getItemInHand(HandTypes.MAIN_HAND).isPresent() && plr.hasPermission("huskycrates.wand")) {
                ItemStack hand = plr.getItemInHand(HandTypes.MAIN_HAND).get();
                if(hand.getItem() == ItemTypes.BLAZE_ROD) {
                    if(hand.toContainer().get(DataQuery.of("UnsafeData","crateID")).isPresent()) {
                        if(!crateUtilities.physicalCrates.containsKey(event.getTargetEntity().getLocation())){
                            //System.out.println(event.getTargetEntity().getLocation().getBlockPosition());
                            event.getTargetEntity().offer(Keys.AI_ENABLED,false);
                            event.getTargetEntity().offer(Keys.IS_SILENT,true);
                            crateUtilities.physicalCrates.put(event.getTargetEntity().getLocation(), new PhysicalCrate(event.getTargetEntity().getLocation(), hand.toContainer().get(DataQuery.of("UnsafeData", "crateID")).get().toString(), this,true));
                            crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()).createHologram();
                            updatePhysicalCrates();
                        }else{
                            event.getTargetEntity().offer(Keys.AI_ENABLED,true);
                            event.getTargetEntity().offer(Keys.IS_SILENT,false);
                            crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()).as.remove();
                            crateUtilities.physicalCrates.remove(event.getTargetEntity().getLocation());
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
                int keyResult = crateUtilities.isAcceptedKey(crateUtilities.physicalCrates.get(event.getTargetEntity().getLocation()),plr.getItemInHand(HandTypes.MAIN_HAND),plr);
                keyHandler(plr,keyResult,vc,event.getTargetEntity().getLocation(),crateType);

            }
        }

    }

    public CrateUtilities getCrateUtilities() {
        return crateUtilities;
    }

    public String getHuskyCrateIdentifier() {
        return huskyCrateIdentifier;
    }

    public void notifyOutOfDate(Player plr){
        if(plr.hasPermission("huskycrates.adminlog") && oodd.isOutOfDate()){
            plr.sendMessage(Text.of(TextColors.RED,"------------------------------------------"));
            plr.sendMessage(Text.of(TextColors.RED,"HuskyCrates is out of date!"));
            plr.sendMessage(Text.of(TextColors.WHITE,"Latest: " + oodd.latestVersion()));
            plr.sendMessage(Text.of(TextColors.WHITE,"Running: v" + pC.getVersion().get()));
            plr.sendMessage(Text.of(TextColors.RED,"Please update your HuskyCrates soon."));
            plr.sendMessage(Text.of(TextColors.RED,"------------------------------------------"));
        }
    }

    @Listener
    public void playerJoin(ClientConnectionEvent.Join event){
        notifyOutOfDate(event.getTargetEntity());
    }
}
