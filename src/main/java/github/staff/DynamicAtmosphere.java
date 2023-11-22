package github.staff;

import commands.*;
import manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import utils.ChatUtils;


public final class DynamicAtmosphere extends JavaPlugin implements Listener {

    ConsoleCommandSender mycmd = Bukkit.getConsoleSender();
    public static String prefix;
    public static DynamicAtmosphere plugin;

    @Override
    public void onEnable() {

        // prefix
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix", "&3&lDynamicAt"));

        // Config
        ConfigManager.setupConfig(this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        //Register
        registerCommands();
        plugin = this;
        registerEvents();

        mycmd.sendMessage(ChatUtils.getColoredMessage("     &3_____"));
        mycmd.sendMessage(ChatUtils.getColoredMessage("   &3|      \\  &3DynamicAt &7v1.0.0        "));
        mycmd.sendMessage(ChatUtils.getColoredMessage("   &3|  |  | |  &7Running on Bukkit - Paper  "));
        mycmd.sendMessage(ChatUtils.getColoredMessage("   &3|  |  | |    &fPlugin by &3[srstaff_tv]"));
        mycmd.sendMessage(ChatUtils.getColoredMessage("   &3|  |__| |  "));
        mycmd.sendMessage(ChatUtils.getColoredMessage("   &3|_____ /    "));
        mycmd.sendMessage(ChatUtils.getColoredMessage(""));
        mycmd.sendMessage(ChatUtils.getColoredMessage("&7Commands successfully loaded"));

    }

    @Override
    public void onDisable() {

        // Guardar configuración al desactivar el plugin
        saveConfig();

        mycmd.sendMessage(ChatUtils.getColoredMessage("&3DynamicAt &7is disabling, if this is a reload and you experience issues consider rebooting."));
        mycmd.sendMessage(ChatUtils.getColoredMessage("&7Commands Saved Successfully"));
        mycmd.sendMessage(ChatUtils.getColoredMessage("&7Goodbye!"));
    }



    public void registerCommands() {
        this.getCommand("reload").setExecutor(new reload(this));
        this.getCommand("Day").setExecutor(new Day(this));
        this.getCommand("Night").setExecutor(new Night(this));
        this.getCommand("Rain").setExecutor(new rain(this));
        this.getCommand("Sun").setExecutor(new Sun(this));
        this.getCommand("Storm").setExecutor(new Storm(this));
    }

    public void registerEvents() {
    }

    public static DynamicAtmosphere getplugin() {
        return plugin;
    }
}
