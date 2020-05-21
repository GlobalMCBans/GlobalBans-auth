package co.uk.minecraftcapes;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Main extends Plugin {

    public static String AUTH_ENDPOINT;
    public static String API_KEY;
    public static Main instance;

    @Override
    public void onEnable() {
        //Set plugin instance
        Main.instance = this;

        try {
            //Makes plugin folders if they don't exist
            File pluginFolder = this.getDataFolder();
            if(!pluginFolder.exists()) {
                pluginFolder.mkdir();
            }

            //Makes config file if it doesn't exist
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                InputStream in = this.getResourceAsStream("config.yml");
                Files.copy(in, configFile.toPath());
            }

            //Loads config
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            this.AUTH_ENDPOINT = configuration.getString("AUTH_ENDPOINT");
            this.API_KEY = configuration.getString("API_KEY");
        } catch(IOException e) {
            e.printStackTrace();
        }

        getProxy().getPluginManager().registerListener(this, new PlayerConnectionHandler());
    }
}