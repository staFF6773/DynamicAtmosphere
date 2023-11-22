package commands;

import github.staff.DynamicAtmosphere;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Storm implements CommandExecutor {
    private final DynamicAtmosphere plugin;

    public Storm(DynamicAtmosphere plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando es ejecutado por la consola o un jugador
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            // Verificar permisos
            if (sender.hasPermission("dynamicAt.storm") || sender instanceof ConsoleCommandSender || sender.isOp()) {
                // Obtener el mundo actual
                World world = ((Player) sender).getWorld();

                // Activar la tormenta
                world.setStorm(true);
                world.setThundering(true);

                // Obtener el mensaje de tormenta desde la configuración
                String timeSetMessage = plugin.getConfig().getString("time-set-message");
                // Si no se encuentra el mensaje en la configuración, usa uno por defecto
                if (timeSetMessage == null) {
                    timeSetMessage = "&3DynamicAt &aSe ha activado una tormenta con lluvia y truenos.";
                }

                timeSetMessage = timeSetMessage.replace("%dy-time-en%", "storm");
                timeSetMessage = timeSetMessage.replace("%dy-time-es%", "tormenta");

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', DynamicAtmosphere.prefix + " " + timeSetMessage));
            } else {
                // Obtén el mensaje desde la configuración
                String noPermissionMessage = plugin.getConfig().getString("messages.no-permission-message");
                // Si no se encuentra el mensaje en la configuración, usa uno por defecto
                if (noPermissionMessage == null) {
                    noPermissionMessage = "&cSorry, but you, %dy-player%, do not have permission to execute this command.";
                }
                // Reemplaza "%player%" con el nombre del jugador
                noPermissionMessage = noPermissionMessage.replace("%dy-player%", sender.getName());
                // Envía el mensaje de falta de permisos
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', DynamicAtmosphere.prefix + " " + noPermissionMessage));
            }
        } else {
            // Mensaje si el comando es ejecutado por una entidad que no sea jugador o consola
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cEste comando solo puede ser ejecutado por un jugador o la consola."));
        }

        return true;
    }
}
