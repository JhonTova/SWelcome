package me.sirius.swelcome.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import me.sirius.swelcome.SWelcome;
import me.sirius.swelcome.managers.ConfigManager;
import me.sirius.swelcome.managers.MessageManager;
import me.sirius.swelcome.managers.PlayerDataManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class PlayerQuitListener implements Listener {
    
    private final SWelcome plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;
    
    public PlayerQuitListener(SWelcome plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        
        Player player = event.getPlayer();
        
        // Si el jugador tiene desactivado el mensaje global, tampoco se anuncia su salida
        if (playerDataManager.hasJoinMessageDisabled(player.getName())) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String rank = getRank(player);
            
            if (configManager.shouldShowRankQuitMessage(rank)) {
                String quitMessage = configManager.getRankQuitMessage(rank);
                quitMessage = messageManager.processMessage(quitMessage, player, rank);
                
                String colorized = messageManager.colorize(quitMessage);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.broadcastMessage(colorized);
                });
            }
        });
    }
    
    private String getRank(Player player) {
        if (player.hasPermission("displayname.tenno")) {
            return "tenno";
        } else if (player.hasPermission("displayname.media+")) {
            return "media+";
        } else if (player.hasPermission("displayname.media")) {
            return "media";
        } else if (player.hasPermission("displayname.kitsune")) {
            return "kitsune";
        } else if (player.hasPermission("displayname.daimyo")) {
            return "daimyo";
        } else if (player.hasPermission("displayname.ronin")) {
            return "ronin";
        }
        
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            
            if (user != null) {
                return user.getPrimaryGroup();
            }
        } catch (ClassNotFoundException e) {
            return "default";
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting rank for " + player.getName() + ": " + e.getMessage());
        }
        return "default";
    }
}