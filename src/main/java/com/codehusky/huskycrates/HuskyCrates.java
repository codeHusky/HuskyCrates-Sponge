package com.codehusky.huskycrates;

import com.codehusky.huskycrates.command.CommandRegister;
import com.codehusky.huskycrates.crate.CrateListeners;
import com.codehusky.huskycrates.crate.virtual.Crate;
import com.codehusky.huskycrates.crate.virtual.Key;
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
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;


@Plugin(id="huskycrates", name = "HuskyCrates", version = "2.0.0", description = "A Crate Plugin for Sponge!",dependencies = {@Dependency(id="huskyui",version = "0.5.2")})
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

    public Path crateConfigPath;
    public ConfigurationLoader<CommentedConfigurationNode> crateConfig;

    public Path keyConfigPath;
    public ConfigurationLoader<CommentedConfigurationNode> keyConfig;

    public Cause genericCause;

    public static HuskyCrates instance;

    public static Registry registry;

    public boolean inErrorState = false;

    private CrateListeners crateListeners;

    public static boolean KEY_SECURITY = true;

    @Listener
    public void gameInit(GamePreInitializationEvent event){
        registry = new Registry();
        logger = LoggerFactory.getLogger(pC.getName());
        instance = this;

        crateConfigPath = configDir.resolve("crates.conf");
        keyConfigPath = configDir.resolve("keys.conf");
        crateConfig = HoconConfigurationLoader.builder().setPath(crateConfigPath).build();
        keyConfig = HoconConfigurationLoader.builder().setPath(keyConfigPath).build();
        loadConfig();

        crateListeners = new CrateListeners();

        Sponge.getEventManager().registerListeners(this,crateListeners);
    }

    public void loadConfig() {

        CommentedConfigurationNode crates;
        CommentedConfigurationNode keys;

        if(checkOrInitalizeConfig(crateConfigPath) && checkOrInitalizeConfig(keyConfigPath)){
            try {
                crates = crateConfig.load();
                keys = keyConfig.load();

                // k both work. wowowwoowow

                for(CommentedConfigurationNode node : keys.getChildrenMap().values()){
                    Key thisKey = new Key(node);
                    registry.registerKey(thisKey);

                }

                for(CommentedConfigurationNode node : crates.getChildrenMap().values()){
                    Crate thisCrate = new Crate(node);
                    registry.registerCrate(thisCrate);
                }

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

    private boolean checkOrInitalizeConfig(Path path){
        if(!path.toFile().exists()) {
            try {
                boolean success = path.toFile().createNewFile();
                if(!success){
                    logger.error("Failed to create new config at " + path.toAbsolutePath().toString());
                    return false;
                }
                PrintWriter pw = new PrintWriter(path.toFile());
                pw.println("# To configure HuskyCrates, please reference the documentation or use HuskyConfigurator!\n# For more information: https://discord.gg/FSETtcx");
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

    @Listener
    public void gameStarted(GameStartedServerEvent event){
        CommandRegister.register(this);
        if(inErrorState) {
            logger.error("Crates has started with errors. Please review the issue(s) above.");
        }else {
            logger.info("Crates has started successfully.");
        }
    }

    @Listener
    public void gameReloaded(GameReloadEvent event){
        inErrorState = false;
        registry.clearRegistry();
        loadConfig();
        if(inErrorState) {
            logger.error("Crates has reloaded with errors. Please review the issue(s) above.");
        }else {
            logger.info("Crates has reloaded successfully.");
        }
    }

}
