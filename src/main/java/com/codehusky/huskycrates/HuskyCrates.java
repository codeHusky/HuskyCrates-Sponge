package com.codehusky.huskycrates;

import com.codehusky.huskycrates.crate.Crate;
import com.codehusky.huskycrates.crate.Key;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;


@Plugin(id="huskycrates", name = "HuskyCrates", version = "2.0.0", description = "A Crate Plugin for Sponge!",dependencies = {@Dependency(id="huskyui",version = "0.5.1")})
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

    public boolean inErrorState = false;

    @Listener
    public void gameInit(GamePreInitializationEvent event){
        logger = LoggerFactory.getLogger(pC.getName());
        instance = this;

        crateConfigPath = configDir.resolve("crates.conf");
        crateConfig = HoconConfigurationLoader.builder().setPath(crateConfigPath).build();
        keyConfig = HoconConfigurationLoader.builder().setPath(keyConfigPath).build();

        CommentedConfigurationNode crates;
        CommentedConfigurationNode keys;

        if(checkOrInitalizeConfig(crateConfigPath) && checkOrInitalizeConfig(keyConfigPath)){
            try {
                crates = crateConfig.load();
                keys = keyConfig.load();

                // k both work. wowowwoowow

                for(CommentedConfigurationNode node : keys.getChildrenList()){
                    Key thisKey = new Key(node);
                }

                for(CommentedConfigurationNode node : crates.getChildrenList()){
                    Crate thisCrate = new Crate(node);
                }

            }catch(Exception e){
                inErrorState = true;
                //todo: handle exceptions based on type
            }
        }else{
            logger.error("Config initialization experienced an error. Please report this to the developer for help.");
        }
    }

    private boolean checkOrInitalizeConfig(Path path){
        if(!path.toFile().exists()) {
            try {
                path.toFile().createNewFile();
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
        if(inErrorState) {
            logger.error("Crates has started with errors. Please review the issue above.");
        }else {
            logger.info("Crates has been started.");
        }
    }

}
