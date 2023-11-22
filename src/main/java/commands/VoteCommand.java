package commands;

import github.staff.DynamicAtmosphere;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteCommand implements CommandExecutor, Listener {
    private final DynamicAtmosphere plugin;
    private final Map<Player, WeatherVote> playerVotes;
    private final Map<String, Integer> voteCount;
    private final Map<UUID, Long> voteCooldown;

    private final int voteThreshold = 1;

    private final long cooldownTime = 60 * 1000; // Tiempo de cooldown en milisegundos (60 segundos en este ejemplo)
    public VoteCommand(DynamicAtmosphere plugin) {
        this.plugin = plugin;
        this.playerVotes = new HashMap<>();
        this.voteCount = new HashMap<>();
        this.voteCooldown = new HashMap<>();
        // Registra el Listener en el constructor para manejar los eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Verificar permisos
            if (!player.hasPermission("dynamicAt.vote") && !player.isOp()) {
                player.sendMessage(ChatColor.RED + "No tienes permisos para votar.");
                return true;
            }

            // Verificar si ya votó
            if (playerVotes.containsKey(player)) {
                player.sendMessage(ChatColor.RED + "Ya has votado. ¡Espera a la próxima votación!");
                return true;
            }

            // Crear el menú de votación
            Inventory voteMenu = createVoteMenu(player);

            // Abrir el menú para el jugador
            player.openInventory(voteMenu);

            // Agregar al jugador a la lista de votantes
            playerVotes.put(player, new WeatherVote());

            player.sendMessage(ChatColor.GREEN + "¡Vota por el clima usando el menú de votación!");
        } else {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por jugadores.");
        }

        return true;
    }

    private Inventory createVoteMenu(Player player) {
        Inventory voteMenu = Bukkit.createInventory(player, 9, "Votación de Clima");

        // Agregar opciones al menú
        ItemStack sunnyItem = createVoteItem("Despejado", "sunny");
        ItemStack rainyItem = createVoteItem("Lluvia", "rainy");
        ItemStack stormyItem = createVoteItem("Tormenta", "stormy");

        voteMenu.setItem(2, sunnyItem);
        voteMenu.setItem(4, rainyItem);
        voteMenu.setItem(6, stormyItem);

        return voteMenu;
    }

    private ItemStack createVoteItem(String displayName, String voteType) {
        ItemStack item = new ItemStack(plugin.getMaterialForWeather(voteType));
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + displayName);
        item.setItemMeta(meta);

        return item;
    }

    // Clase interna para representar el voto de un jugador
    private static class WeatherVote {
        private String voteType;

        public String getVoteType() {
            return voteType;
        }

        public void setVoteType(String voteType) {
            this.voteType = voteType;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Votación de Clima")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                Player player = (Player) event.getWhoClicked();
                UUID playerUUID = player.getUniqueId();

                // Verificar el cooldown
                if (voteCooldown.containsKey(playerUUID) && System.currentTimeMillis() - voteCooldown.get(playerUUID) < cooldownTime) {
                    long timeLeft = (cooldownTime - (System.currentTimeMillis() - voteCooldown.get(playerUUID))) / 1000;
                    player.sendMessage(ChatColor.RED + "Debes esperar " + timeLeft + " segundos antes de volver a votar.");
                    return;
                }

                ItemStack clickedItem = event.getCurrentItem();
                WeatherVote weatherVote = playerVotes.get(player);

                if (weatherVote != null) {
                    String voteType = getVoteTypeFromItem(clickedItem);
                    weatherVote.setVoteType(voteType);

                    player.sendMessage(ChatColor.GREEN + "Has votado por " + voteType + "!");
                    int currentVotes = voteCount.getOrDefault(voteType, 0) + 1;
                    voteCount.put(voteType, currentVotes);

                    // Actualizar el cooldown del jugador
                    voteCooldown.put(playerUUID, System.currentTimeMillis());

                    // Verificar si se alcanzó el umbral de votos para cambiar el clima
                    if (currentVotes >= voteThreshold) {
                        // Restablecer el contador y cambiar el clima
                        voteCount.put(voteType, 0);
                        executeWeatherCommand(player, voteType);
                    } else {
                        int votesLeft = voteThreshold - currentVotes;
                        player.sendMessage(ChatColor.YELLOW + "Faltan " + votesLeft + " votos para cambiar el clima.");
                    }
                }

                player.closeInventory();
            }
        }
    }

    private String getVoteTypeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = ChatColor.stripColor(meta.getDisplayName().toLowerCase());
            if (displayName.contains("despejado")) {
                return "sunny";
            } else if (displayName.contains("lluvia")) {
                return "rainy";
            } else if (displayName.contains("tormenta")) {
                return "stormy";
            }
        }
        return "";
    }


    private void executeWeatherCommand(Player player, String voteType) {
        World world = player.getWorld();

        switch (voteType.toLowerCase()) {
            case "sunny":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear");
                break;
            case "rainy":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather rain");
                break;
            case "stormy":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather thunder");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Tipo de clima no reconocido: " + voteType);
        }
    }
}
