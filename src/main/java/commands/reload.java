package commands;

import github.staff.DynamicAtmosphere;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import utils.ChatUtils;

import java.io.File;

public class reload implements CommandExecutor {
    private final DynamicAtmosphere plugin;

    public reload(DynamicAtmosphere plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("dynamicAt.reload") || sender instanceof ConsoleCommandSender || sender.isOp()) {
            reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3DynamicAt &aThe configuration file was reloaded."));
            sender.sendMessage(ChatUtils.getColoredMessage("&7(Some options only apply after the server has been restarted.)"));
        } else {
            // Obtén el mensaje desde la configuración
            String noPermissionMessage = plugin.getConfig().getString("no-permission-message");
            // Si no se encuentra el mensaje en la configuración, usa uno por defecto
            if (noPermissionMessage == null) {
                noPermissionMessage = "&cSorry, but you, %dy-player%, do not have permission to execute this command.";
            }
            // Reemplaza "%player%" con el nombre del jugador
            noPermissionMessage = noPermissionMessage.replace("%dy-player%", sender.getName());
            // Envía el mensaje de falta de permisos
            sender.sendMessage(ChatUtils.getColoredMessage(DynamicAtmosphere.prefix + " " + noPermissionMessage));
        }
        return true;
    }


    private void reloadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            // Si el archivo no existe, carga la configuración por defecto
            plugin.saveDefaultConfig();
        }

        plugin.reloadConfig();
    }
}
