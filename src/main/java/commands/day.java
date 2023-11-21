package commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Day implements CommandExecutor {
    private final JavaPlugin plugin;

    public Day(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando es ejecutado por la consola o un jugador
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            // Verificar permisos
            if (sender.hasPermission("dynamicAt.day") || sender instanceof ConsoleCommandSender || sender.isOp()) {
                // Obtener el mundo actual
                World world = ((Player) sender).getWorld();

                // Establecer el tiempo del día a la mañana (0 ticks)
                world.setTime(0);

                // Enviar mensaje de confirmación
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3DynamicAt &aEl tiempo del día ha sido establecido a la mañana."));
            } else {
                // Mensaje de falta de permisos
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo tienes permiso para ejecutar este comando."));
            }
        } else {
            // Mensaje si el comando es ejecutado por una entidad que no sea jugador o consola
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste comando solo puede ser ejecutado por un jugador o la consola."));
        }

        return true;
    }
}
