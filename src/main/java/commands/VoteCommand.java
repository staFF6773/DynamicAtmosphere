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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteCommand implements CommandExecutor, Listener {
    private final DynamicAtmosphere plugin;
    private final Map<Player, WeatherVote> playerVotes;
    private final Map<String, Integer> voteCount;
    private final Map<UUID, Long> voteCooldown;

    // Variables configurables
    private int cooldownTime;
    private int voteThreshold;

    private String voteSuccessMessage;
    private String cooldownMessage;
    private String votesLeftMessage;
    private String unknownWeatherMessage;

    private String voteMenuTitle;
    private Map<String, VoteOption> voteMenuConfig;

    public VoteCommand(DynamicAtmosphere plugin) {
        this.plugin = plugin;
        this.playerVotes = new HashMap<>();
        this.voteCount = new HashMap<>();
        this.voteCooldown = new HashMap<>();

        // Cargar configuración al inicializar
        loadConfiguration();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadConfiguration() {
        // Obtener el nodo de configuración
        ConfigurationSection config = plugin.getConfig();

        // Leer valores de configuración
        cooldownTime = config.getInt("cooldown-time", 60) * 1000; // Convertir a milisegundos
        voteThreshold = config.getInt("vote-threshold", 5);

        // Leer mensajes de configuración
        voteSuccessMessage = ChatColor.translateAlternateColorCodes('&', config.getString("vote-success", "&a&aYou have voted for %weather%!"));
        cooldownMessage = ChatColor.translateAlternateColorCodes('&', config.getString("cooldown-message", "&cYou must wait %time% seconds before voting again."));
        votesLeftMessage = ChatColor.translateAlternateColorCodes('&', config.getString("votes-left", "&eLack of %votes% votes to change the weather."));
        unknownWeatherMessage = ChatColor.translateAlternateColorCodes('&', config.getString("unknown-weather", "&cUnrecognized climate type: %weather%"));

        // Leer configuración del menú de votación
        ConfigurationSection voteMenuSection = config.getConfigurationSection("vote-menu");
        if (voteMenuSection != null) {
            voteMenuTitle = ChatColor.translateAlternateColorCodes('&', voteMenuSection.getString("title", "&6Choose the weather"));
            voteMenuConfig = new HashMap<>();

            for (String voteType : voteMenuSection.getKeys(false)) {
                ConfigurationSection optionSection = voteMenuSection.getConfigurationSection(voteType);
                if (optionSection != null) {
                    String displayName = ChatColor.translateAlternateColorCodes('&', optionSection.getString("display-name", ""));
                    Material material = Material.matchMaterial(optionSection.getString("material", "STONE"));
                    int slot = optionSection.getInt("slot", 0);

                    if (displayName.isEmpty() || material == null || slot < 0 || slot > 8) {
                        // Log or handle invalid configuration
                        continue;
                    }

                    voteMenuConfig.put(voteType, new VoteOption(displayName, material, slot));
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
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
        Inventory voteMenu = Bukkit.createInventory(player, 9, voteMenuTitle);

        // Agregar opciones al menú
        for (Map.Entry<String, VoteOption> entry : voteMenuConfig.entrySet()) {
            VoteOption voteOption = entry.getValue();
            ItemStack voteItem = createVoteItem(voteOption.getDisplayName(), voteOption.getMaterial(), entry.getKey());

            if (voteOption.getSlot() >= 0 && voteOption.getSlot() < 9) {
                voteMenu.setItem(voteOption.getSlot(), voteItem);
            } else {
                // Log or handle invalid slot
            }
        }

        return voteMenu;
    }

    private ItemStack createVoteItem(String displayName, Material material, String voteType) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + displayName);
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(voteMenuTitle)) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                Player player = (Player) event.getWhoClicked();
                UUID playerUUID = player.getUniqueId();

                // Verificar el cooldown
                if (voteCooldown.containsKey(playerUUID) && System.currentTimeMillis() - voteCooldown.get(playerUUID) < cooldownTime) {
                    long timeLeft = (cooldownTime - (System.currentTimeMillis() - voteCooldown.get(playerUUID))) / 1000;
                    player.sendMessage(cooldownMessage.replace("%time%", String.valueOf(timeLeft)));
                    return;
                }

                ItemStack clickedItem = event.getCurrentItem();
                WeatherVote weatherVote = playerVotes.get(player);

                if (weatherVote != null) {
                    String voteType = getVoteTypeFromItem(clickedItem);
                    weatherVote.setVoteType(voteType);

                    player.sendMessage(voteSuccessMessage.replace("%weather%", voteType));
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
                        player.sendMessage(votesLeftMessage.replace("%votes%", String.valueOf(votesLeft)));
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
                player.sendMessage(unknownWeatherMessage.replace("%weather%", voteType));
        }
    }

    private static class VoteOption {
        private final String displayName;
        private final Material material;
        private final int slot;

        public VoteOption(String displayName, Material material, int slot) {
            this.displayName = displayName;
            this.material = material;
            this.slot = slot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getMaterial() {
            return material;
        }

        public int getSlot() {
            return slot;
        }
    }

    private static class WeatherVote {
        private String voteType;

        public String getVoteType() {
            return voteType;
        }

        public void setVoteType(String voteType) {
            this.voteType = voteType;
        }
    }
}
