package me.sirius.swelcome.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.sirius.swelcome.SWelcome;
import me.sirius.swelcome.managers.ConfigManager;
import me.sirius.swelcome.managers.MessageManager;
import me.sirius.swelcome.managers.PlayerDataManager;
import me.sirius.swelcome.managers.SkinManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;


public class PlayerJoinListener implements Listener {
    
    private final SWelcome plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messageManager;
    private final SkinManager skinManager;
    
    public PlayerJoinListener(SWelcome plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.messageManager = plugin.getMessageManager();
        this.skinManager = plugin.getSkinManager();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        
        Player player = event.getPlayer();
        String playerName = player.getName();
        boolean isBedrockPlayer = playerName.startsWith(".");
        // Si el jugador desactivó su mensaje global, no se hace broadcast (el personal sí se muestra)
        boolean globalDisabled = playerDataManager.hasJoinMessageDisabled(playerName);

        // Todo esto es memoria y permisos: va en el hilo principal. Antes se mandaba
        // al pool comun y dos entradas a la vez se peleaban por el contador de jugadores.
        String rank = getRank(player);
        if (!playerDataManager.hasJoined(playerName)) {
            int playerNumber = playerDataManager.addPlayer(playerName);
            handleFirstJoin(player, rank, isBedrockPlayer, playerNumber, globalDisabled);
        } else {
            handleRegularJoin(player, rank, isBedrockPlayer, globalDisabled);
        }
    }
    
    private void handleFirstJoin(Player player, String rank, boolean isBedrockPlayer, int playerNumber, boolean globalDisabled) {
        String globalMessage = configManager.getFirstJoinGlobalMessage();
        String personalMessage = configManager.getFirstJoinPersonalMessage();
        
        globalMessage = messageManager.processMessageWithPlayerNumber(globalMessage, player, rank, playerNumber);
        personalMessage = messageManager.processMessageWithPlayerNumber(personalMessage, player, rank, playerNumber);
        
        // Solo se hace broadcast si el jugador NO tiene desactivado su mensaje global
        if (!globalDisabled) {
            broadcastMessage(globalMessage);
        }
        // El mensaje personal siempre se muestra
        sendPersonalMessage(player, personalMessage);
        
        plugin.getLogger().info("First join: " + player.getName() + " - Player #" + playerNumber
            + (globalDisabled ? " (mensaje global desactivado por el jugador)" : ""));
    }
    
    private void handleRegularJoin(Player player, String rank, boolean isBedrockPlayer, boolean globalDisabled) {
        final String playerRank = rank;
        
        if (configManager.shouldShowRankJoinMessage(playerRank)) {
            // Solo se hace broadcast si el jugador NO tiene desactivado su mensaje global
            if (!globalDisabled) {
                String globalMessage = configManager.getRankJoinGlobalMessage(playerRank);
                globalMessage = messageManager.processMessage(globalMessage, player, playerRank);
                broadcastMessage(globalMessage);
            }
            
            // El mensaje personal de rango siempre se muestra
            if (configManager.hasRankJoinPersonalMessage(playerRank)) {
                String personalMessage = configManager.getRankJoinPersonalMessage(playerRank);
                personalMessage = messageManager.processMessage(personalMessage, player, playerRank);
                sendPersonalMessage(player, personalMessage);
            }
        }
        
        if (!configManager.shouldShowSkinOnJoin()) {
            String welcomeBack = configManager.getWelcomeBackMessage();
            welcomeBack = messageManager.processMessage(welcomeBack, player, playerRank);
            sendPersonalMessage(player, welcomeBack);
            return;
        }
        
        skinManager.getSkinArtAsync(player, isBedrockPlayer)
            .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .thenAccept(skinArt -> {
                String welcomeBack = configManager.getWelcomeBackMessage();
                String processedMessage = messageManager.processMessageWithSkin(welcomeBack, player, playerRank, skinArt);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        String[] lines = processedMessage.split("\n");
                        for (String line : lines) {
                            player.sendMessage(messageManager.colorize(line));
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                plugin.getLogger().warning("Failed to get skin for " + player.getName() + ", using default message");
                
                String welcomeBack = configManager.getWelcomeBackMessage();
                welcomeBack = messageManager.processMessage(welcomeBack, player, playerRank);
                sendPersonalMessage(player, welcomeBack);
                
                return null;
            });
    }
    
    private String getRank(Player player) {
        if (player.hasPermission("displayname.tenno")) return "tenno";
        if (player.hasPermission("displayname.media+")) return "media+";
        if (player.hasPermission("displayname.media")) return "media";
        if (player.hasPermission("displayname.kitsune")) return "kitsune";
        if (player.hasPermission("displayname.daimyo")) return "daimyo";
        if (player.hasPermission("displayname.ronin")) return "ronin";
        
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            
            if (user != null) {
                String primaryGroup = user.getPrimaryGroup();
                return primaryGroup != null ? primaryGroup : "default";
            }
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting rank for " + player.getName() + ": " + e.getMessage());
        }
        
        return "default";
    }
    
    private void broadcastMessage(String message) {
        if (message == null || message.isEmpty()) return;
        
        String colorized = messageManager.colorize(message);
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.broadcastMessage(colorized);
        });
    }
    
    private void sendPersonalMessage(Player player, String message) {
        if (message == null || message.isEmpty()) return;
        
        String colorized = messageManager.colorize(message);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(colorized);
            }
        });
    }
}