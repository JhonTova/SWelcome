package me.sirius.swelcome;

import me.sirius.core.SiriusPlugin;
import org.bukkit.scheduler.BukkitTask;
import me.sirius.swelcome.commands.SWelcomeCommand;
import me.sirius.swelcome.listeners.PlayerJoinListener;
import me.sirius.swelcome.listeners.PlayerQuitListener;
import me.sirius.swelcome.managers.ConfigManager;
import me.sirius.swelcome.managers.PlayerDataManager;
import me.sirius.swelcome.managers.MessageManager;
import me.sirius.swelcome.managers.SkinManager;

public class SWelcome extends SiriusPlugin {
    
    private static SWelcome instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private MessageManager messageManager;
    private SkinManager skinManager;
    
    private BukkitTask autoSaveTask;
    private BukkitTask cacheCleanTask;
    
    @Override
    protected void onStart() {
        instance = this;
        
        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        messageManager = new MessageManager(this);
        skinManager = new SkinManager();
        
        configManager.loadConfig();
        playerDataManager.loadData();
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        
        // Registrar el comando /swelcome (requiere estar declarado en plugin.yml)
        SWelcomeCommand swelcomeCommand = new SWelcomeCommand(this);
        if (getCommand("swelcome") != null) {
            getCommand("swelcome").setExecutor(swelcomeCommand);
            getCommand("swelcome").setTabCompleter(swelcomeCommand);
        } else {
            getLogger().severe("¡El comando 'swelcome' no está declarado en plugin.yml! Los comandos no funcionarán.");
        }
        
        checkLuckPerms();
        startAutoSave();
        startCacheCleanup();
        
        getLogger().info("SWelcome enabled successfully!");
    }
    
    @Override
    protected void onStop() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (cacheCleanTask != null) {
            cacheCleanTask.cancel();
        }
        
        playerDataManager.saveData();
        skinManager.clearCache();
        
        getLogger().info("SWelcome disabled successfully!");
    }
    
    private void checkLuckPerms() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            getLogger().info("LuckPerms detected! Rank features enabled.");
        } catch (ClassNotFoundException e) {
            getLogger().warning("LuckPerms not found. Using default rank for all players.");
            getLogger().warning("Install LuckPerms to enable rank-based messages.");
        }
    }
    
    private void startAutoSave() {
        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                playerDataManager.saveData();
                getLogger().info("Auto-save completed");
            } catch (Exception e) {
                getLogger().severe("Error during auto-save: " + e.getMessage());
                e.printStackTrace();
            }
        }, 6000L, 6000L);
    }
    
    private void startCacheCleanup() {
        cacheCleanTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                int sizeBefore = skinManager.getCacheSize();
                skinManager.cleanExpiredCache();
                int sizeAfter = skinManager.getCacheSize();
                
                if (sizeBefore != sizeAfter) {
                    getLogger().info("Cache cleanup: removed " + (sizeBefore - sizeAfter) + " expired entries");
                }
            } catch (Exception e) {
                getLogger().severe("Error during cache cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }, 36000L, 36000L);
    }
    
    public static SWelcome getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public SkinManager getSkinManager() {
        return skinManager;
    }
}