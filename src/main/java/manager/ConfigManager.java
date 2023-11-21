package manager;


import github.staff.DynamicAtmosphere;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static FileConfiguration config;

    public static void setupConfig(DynamicAtmosphere dynamicAtmosphere){
        ConfigManager.config = dynamicAtmosphere.getConfig();
        dynamicAtmosphere.saveDefaultConfig();
    }
}