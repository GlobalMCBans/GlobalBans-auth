package co.uk.minecraftcapes;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Main extends Plugin
{
    String API_KEY;

    public void onEnable()
    {
        getLogger().info("Plugin Loaded");

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            System.out.println("No config file found at: " + file.toString());
        }
        try
        {
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            this.API_KEY = configuration.getString("API_KEY");
        }
        catch (IOException localIOException) {}
        getProxy().getPluginManager().registerListener(this, new CodeGetter(this.API_KEY));
    }

    public void onDisable()
    {
        getLogger().info("Plugin Unloaded");
    }
}