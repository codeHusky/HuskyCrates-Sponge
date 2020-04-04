package com.codehusky.huskycrates;

import com.codehusky.huskycrates.command.BalanceCommand;
import com.codehusky.huskycrates.command.BlockCommand;
import com.codehusky.huskycrates.command.CommandRegister;
import com.codehusky.huskycrates.command.KeyCommand;
import com.codehusky.huskycrates.crate.listeners.CrateListeners;
import com.codehusky.huskycrates.crate.listeners.SQLUpdateListener;
import com.codehusky.huskycrates.crate.physical.EffectInstance;
import com.codehusky.huskycrates.crate.physical.PhysicalCrate;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
import com.codehusky.huskycrates.event.CrateInjectionEvent;
import com.codehusky.huskycrates.exception.ConfigParseError;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


@Plugin(
        id="huskycrates",
        name = "HuskyCrates",
        version = "2.0.0RC3",
        description = "A Crate Plugin for Sponge!",
        dependencies = {@Dependency(id="huskyui",version = "0.6.0PRE4"), @Dependency(id="placeholderapi", optional = true)})
public class HuskyCrates {
    //@Inject
    public Logger logger;

    @Inject
    public PluginContainer pC;

    @Inject
    @ConfigDir(sharedRoot = false)
    public Path configDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    public ConfigurationLoader<CommentedConfigurationNode> config;
    public CommentedConfigurationNode mainConfig;

    public Path crateDirectoryPath;
    public ConfigurationLoader<CommentedConfigurationNode> crateLoop;

    public Path keyConfigPath;
    public ConfigurationLoader<CommentedConfigurationNode> keyConfig;
    public CommentedConfigurationNode keys;

    public Path generatedItemConfigPath;
    public ConfigurationLoader<CommentedConfigurationNode> generatedItemConfig;

    public Path generatedInventoryConfigPath;
    public ConfigurationLoader<CommentedConfigurationNode> generatedInventoryConfig;

    public static Path dupeLogPath;

    public static Path storageDirectoryPath;

    //TODO unused variable
    public Cause genericCause;

    public static HuskyCrates instance;

    public static Registry registry;

    public boolean inErrorState = false;

    private CrateListeners crateListeners;

    public static boolean KEY_SECURITY = true;
    public static boolean FORCE_CRATE_CMD = true;

    private static boolean firstBoot = false;

    public static KeyCommand.Messages keyCommandMessages;
    public static BlockCommand.Messages blockCommandMessages;
    public static BalanceCommand.Messages balanceCommandMessages;
    public static Crate.Messages crateMessages;

    private static ScriptEngineManager mgr = new ScriptEngineManager();
    public static ScriptEngine jsengine = mgr.getEngineByName("JavaScript");

    public boolean virtualKeyDB = false;


    @Listener
    public void gameInit(GamePreInitializationEvent event){
        registry = new Registry();
        logger = LoggerFactory.getLogger(pC.getName());
        instance = this;

        storageDirectoryPath = configDir.resolve("storage/");
        dupeLogPath = configDir.resolve("storage/dupealert.log");
        generatedItemConfigPath = configDir.resolve("storage/generateditems.conf");
        generatedInventoryConfigPath = configDir.resolve("storage/generatedinventorys.conf");
        crateDirectoryPath = configDir.resolve("crates/");
        keyConfigPath = configDir.resolve("crates/keys.conf");

        keyConfig = HoconConfigurationLoader.builder().setPath(keyConfigPath).build();
        generatedItemConfig = HoconConfigurationLoader.builder().setPath(generatedItemConfigPath).build();
        generatedInventoryConfig = HoconConfigurationLoader.builder().setPath(generatedInventoryConfigPath).build();

        Path crateMigrationPath = configDir.resolve("crates.conf");
        Path keysMigrationPath = configDir.resolve("keys.conf");
        Path dbMigrationPath = configDir.resolve("data.mv.db");
        Path genItemMigrationPath = configDir.resolve("generateditems.conf");

        migrateConfigs(crateMigrationPath, "/crates/crates.crate");
        migrateConfigs(keysMigrationPath, "/crates/keys.conf");
        migrateConfigs(dbMigrationPath, "/storage/data.mv.db");
        migrateConfigs(genItemMigrationPath, "/storage/generateditems.conf");

    }
    private float cumulative = 0;
    private int iterations = 0;
    private long lastMessage = 0;
    @Listener
    public void gamePostInit(GamePreInitializationEvent event){
        crateListeners = new CrateListeners();
        Sponge.getEventManager().registerListeners(this,crateListeners);
    }

    public static final String generalDefaultConfig = "# To configure HuskyCrates, please reference the documentation, use the \"/hc generatecrate\" command, or use the HuskyConfigurator!\n\n# For more information: https://discord.gg/FSETtcx";

    private void migrateConfigs(Path n, String name){
        Path conf = Paths.get(configDir.toString() + name);
        if(n.toFile().exists()) {
            checkOrInitalizeDirectory(crateDirectoryPath);
            checkOrInitalizeDirectory(storageDirectoryPath);
            try {
                Files.move(n,conf,StandardCopyOption.REPLACE_EXISTING);
            }catch(Exception e){
                e.printStackTrace();
                logger.error("Failed to migrate a config to newer path!");
            }
        }
    }

    public void loadConfig() {
        if(checkOrInitalizeDirectory(crateDirectoryPath) && checkOrInitalizeDirectory(storageDirectoryPath) && checkOrInitalizeConfig(keyConfigPath,generalDefaultConfig)){
            checkOrInitalizeConfig(dupeLogPath,"#Here you'll find a log of information stored after an instance duplication detection!\n#File will not be read, is simply a log");
            checkOrInitalizeConfig(generatedItemConfigPath,"# This config contains generated item objects that you create in-game. This file will not be read by the plugin.\n# With the admin permission, try /hc genitem with an item in your hand, then check back here.");
            checkOrInitalizeConfig(generatedInventoryConfigPath,"# This config contains generated inventory sets that you create in-game. This file will not be read by the plugin.\n# With the admin permission, try /hc geninvent with an inventory full of items, then check back here.");
            try {
                mainConfig = config.load();
                virtualKeyDB = mainConfig.getNode("virtualkeydatabase").getNode("useRemoteDatabase").getBoolean();
                keys = keyConfig.load();

                if(virtualKeyDB){
                    Sponge.getEventManager().registerListeners(this, new SQLUpdateListener());
                }

                for(CommentedConfigurationNode node : keys.getChildrenMap().values()){
                    Key thisKey = new Key(node);
                    registry.registerKey(thisKey);

                }

                File folder = new File(crateDirectoryPath.toString());
                File[] files = folder.listFiles();
                List<File> crateFiles = new ArrayList<>();

                for (File file : files){
                    if (file.getName().endsWith(".crate")){
                        crateFiles.add(file);
                    }
                }

                if(crateFiles.size() == 0){
                    HuskyCrates.instance.logger.debug("The crate directory contains no crates, pushing example config!");
                    //System.out.println("The crate directory contains no crates, pushing example config!");
                    if(Sponge.getAssetManager().getAsset(pC, "example.crate").isPresent()){
                        Sponge.getAssetManager().getAsset(pC, "example.crate").get().copyToFile(crateDirectoryPath.resolve("example.crate"));
                    }
                    else{
                        HuskyCrates.instance.logger.error("Failed to read asset to copy file from!");
                        //System.out.println("Failed to read asset to copy file from!");
                    }
                }

                for (File file : files) {
                    if (file.isFile() && file.getPath().endsWith(".crate") && file.length() > 0) {
                        crateLoop = HoconConfigurationLoader.builder().setPath(file.toPath()).build();

                        CommentedConfigurationNode crateThing;
                        crateThing = crateLoop.load();

                        if(!crateThing.getNode("secureKeys").isVirtual() && !crateThing.getNode("secureKeys").hasMapChildren()){
                            throw new ConfigParseError("\"secureKeys\" must be removed from \""+file.getName()+ "\"!",crateThing.getNode("secureKeys").getPath());
                        }

                        for(CommentedConfigurationNode node : crateThing.getChildrenMap().values()){
                            Crate thisCrate = new Crate(node);
                            registry.registerCrate(thisCrate);
                         }

                        logger.debug("Crate Config File \"" + file.getName() + "\" Has been loaded!");
                    }
                }

                if(mainConfig.getNode("virtualkeydatabase").isVirtual()){
                    CommentedConfigurationNode db = mainConfig.getNode("virtualkeydatabase");
                    db.getNode("useRemoteDatabase").setValue(false);
                    db.getNode("type").setValue("mysql");
                    db.getNode("host").setValue("127.0.0.1");
                    db.getNode("port").setValue(3306);
                    db.getNode("database").setValue("huskycrates");
                    db.getNode("username").setValue("root");
                    db.getNode("password").setValue("");
                }

                if(!mainConfig.getNode("crates").isVirtual()){
                    throw new ConfigParseError("HuskyCrates.conf contains 1.x config data! Please update it using the Config Converter application!",mainConfig.getNode("crates").getPath());
                }

                if(mainConfig.getNode("secureKeys").isVirtual()){
                    mainConfig.getNode("secureKeys").setValue(HuskyCrates.KEY_SECURITY);
                }else{
                    HuskyCrates.KEY_SECURITY = mainConfig.getNode("secureKeys").getBoolean(true);
                }

                if(mainConfig.getNode("forceCrateCMD").isVirtual()){
                    mainConfig.getNode("forceCrateCMD").setValue(HuskyCrates.FORCE_CRATE_CMD);
                }else{
                    if(firstBoot && mainConfig.getNode("forceCrateCMD").getBoolean(true) != HuskyCrates.FORCE_CRATE_CMD){
                        logger.error("!!!!!! CRITICAL ERROR !!!!!!");
                        logger.error("forceCrateCMD changes require a server reboot to apply!");
                        logger.error("Please reboot immediately!");
                        logger.error("!!!!!! CRITICAL ERROR !!!!!!");
                        inErrorState=true;
                    }else {
                        HuskyCrates.FORCE_CRATE_CMD = mainConfig.getNode("forceCrateCMD").getBoolean(true);
                    }
                }

                firstBoot = true;

                keyCommandMessages = new KeyCommand.Messages(mainConfig.getNode("messages","keyCommand"));
                blockCommandMessages = new BlockCommand.Messages(mainConfig.getNode("messages","blockCommand"));
                balanceCommandMessages = new BalanceCommand.Messages(mainConfig.getNode("messages","balanceCommand"));

                crateMessages = new Crate.Messages(mainConfig.getNode("messages","crate"),null);

                // k both work. wowowwoowow


                config.save(mainConfig);
                Sponge.getEventManager().post(new CrateInjectionEvent());

            }catch(Exception e){
                inErrorState = true;
                e.printStackTrace();
                logger.error("Failed to register crates and keys. Please review the errors printed above.");
                //todo: handle exception based on type
            }
        }else{
            logger.error("Config initialization experienced an error. Please report this to the developer for help.");
        }
    }

    private boolean checkOrInitalizeConfig(Path path, String defaultContent){
        if(!path.toFile().exists()) {
            try {
                boolean success = path.toFile().createNewFile();
                if(!success){
                    logger.error("Failed to create new config at " + path.toAbsolutePath().toString());
                    return false;
                }
                PrintWriter pw = new PrintWriter(path.toFile());
                pw.println(defaultContent);
                pw.close();
                return true;
            } catch (IOException e) {
                inErrorState = true;
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    private boolean checkOrInitalizeDirectory(Path path){
        if(!path.toFile().exists()) {
                if(!path.toFile().mkdirs()){
                    logger.error("Failed to create new directory at " + path.toAbsolutePath().toString());
                    return false;
                }
        }
        return true;
    }

    @Listener
    public void gameStarted(GameStartedServerEvent event) {
        logger.info("Loading Crates...");
        loadConfig();




        Sponge.getScheduler().createTaskBuilder().execute(new Consumer<Task>() {
            @Override
            public void accept(Task task) {
                try {
                    long startTime = System.nanoTime();
                    int particles = 0;
                    for (Location<World> location : registry.getPhysicalCrates().keySet()) {
                        PhysicalCrate pcrate = registry.getPhysicalCrate(location);
                        if (pcrate.getIdleEffect() != null) {
                            pcrate.getIdleEffect().tick();
                            particles += pcrate.getIdleEffect().getEffect().getParticleCount();
                        }
                    }
                    long endTime = System.nanoTime();
                    cumulative += (endTime - startTime);
                    iterations++;
                    if(lastMessage + 1000 < System.currentTimeMillis()){
                        lastMessage = System.currentTimeMillis();
                        float avg = (cumulative / ((float)iterations));
                        /*System.out.println("AVG PARTICLE TIME: " + avg + " nanoseconds (" + (avg / 1000000) + " milliseconds)");
                        System.out.println("EST TIME PER EFFECT: " + (avg / particles) + " nanoseconds (" + (avg / particles / 1000000) + " milliseconds)");
                        System.out.println("PARTICLES: " + particles);
                        System.out.println("--------------------------");*/
                        iterations = 0;
                        cumulative = 0;
                    }
                    //System.out.println("PARTICLE TIME: " + ((endTime - startTime)/1000000.0) + " milliseconds");
                    ArrayList<EffectInstance> nuke = new ArrayList<>();
                    for (EffectInstance inst : registry.getEffects()) {
                        inst.tick();
                        if (inst.getEffect().isFinished()) {
                            nuke.add(inst);
                        }
                    }
                    for (EffectInstance inst : nuke) {
                        inst.resetEffect();
                        registry.removeEffect(inst);
                    }
                }catch (ConcurrentModificationException e){}
            }
        }).intervalTicks(1).async().submit(this);

        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(registry::pushDirty)
                .interval(1, TimeUnit.MINUTES)
                .submit(this);

        HuskyCrates.registry.loadFromDatabase();
        CommandRegister.register(this);
        if(inErrorState) {
            logger.error("Crates has started with errors. Please review the issue(s) above.");
        }else {
            logger.info("Crates has started successfully.");

        }
        if(pC.getVersion().get().contains("PRE")) {
            logger.warn("You are currently running a pre-release build!");
            logger.warn("This is an unstable version of HuskyCrates and, as such,");
            logger.warn("  it has not been tested thoroughly and will have bugs!");
            logger.warn("Report all issues to codeHusky on the support discord!");
            logger.warn("For help configuring, please consult the SRC or the discord.");
            logger.warn("Thanks! - codeHusky");
        }
        logger.info("Running HuskyCrates v" + pC.getVersion().get());
    }

    public void reload() {
        inErrorState = false;
        registry.pushDirty();
        registry.clearRegistry();
        loadConfig();
        if(!inErrorState) {
            registry.loadFromDatabase();
        }
        if(inErrorState) {
            logger.error("Crates has reloaded with errors. Please review the issue(s) above.");
        }else {
            logger.info("Crates has reloaded successfully.");
        }
    }

    @Listener
    public void gameReloaded(GameReloadEvent event) {
        reload();
    }

    @Listener
    public void gameShutdown(GameStoppingServerEvent event){
        registry.pushDirty();
        logger.info("HuskyCrates has shut down.");
    }

}
