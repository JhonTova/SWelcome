package me.sirius.swelcome.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.sirius.swelcome.SWelcome;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final SWelcome plugin;
    private FileConfiguration config;
    
    private String firstJoinGlobalMessage;
    private String firstJoinPersonalMessage;
    private String welcomeBackMessage;
    private boolean showSkinOnJoin;
    private int skinSize;
    
    private final Map<String, RankMessages> rankMessages = new HashMap<>();
    
    public ConfigManager(SWelcome plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        firstJoinGlobalMessage = config.getString("messages.first-join.global", 
            "&a&l¡Bienvenido por primera vez &e{player} &a&lal servidor!");
        firstJoinPersonalMessage = config.getString("messages.first-join.personal", 
            "&7&m-----------------------------------\n&a¡Hola &e{player}&a!\n&7¡Gracias por unirte a nuestro servidor!\n&7&m-----------------------------------");
        welcomeBackMessage = config.getString("messages.welcome-back", 
            "&a¡Bienvenido de vuelta &e{player}&a!");
        
        showSkinOnJoin = config.getBoolean("settings.show-skin-on-join", true);
        skinSize = config.getInt("settings.skin-size", 8);
        
        ConfigurationSection ranksSection = config.getConfigurationSection("ranks");
        if (ranksSection != null) {
            for (String rank : ranksSection.getKeys(false)) {
                String joinGlobal = config.getString("ranks." + rank + ".join.global", null);
                String joinPersonal = config.getString("ranks." + rank + ".join.personal", null);
                String quitMessage = config.getString("ranks." + rank + ".quit", null);
                
                rankMessages.put(rank.toLowerCase(), new RankMessages(joinGlobal, joinPersonal, quitMessage));
            }
        }
    }
    
    public String getFirstJoinGlobalMessage() {
        return firstJoinGlobalMessage;
    }
    
    public String getFirstJoinPersonalMessage() {
        return firstJoinPersonalMessage;
    }
    
    public String getWelcomeBackMessage() {
        return welcomeBackMessage;
    }
    
    public boolean shouldShowRankJoinMessage(String rank) {
        return rankMessages.containsKey(rank.toLowerCase()) && 
               rankMessages.get(rank.toLowerCase()).joinGlobal != null;
    }
    
    public String getRankJoinGlobalMessage(String rank) {
        RankMessages messages = rankMessages.get(rank.toLowerCase());
        return messages != null ? messages.joinGlobal : null;
    }
    
    public boolean hasRankJoinPersonalMessage(String rank) {
        RankMessages messages = rankMessages.get(rank.toLowerCase());
        return messages != null && messages.joinPersonal != null;
    }
    
    public String getRankJoinPersonalMessage(String rank) {
        RankMessages messages = rankMessages.get(rank.toLowerCase());
        return messages != null ? messages.joinPersonal : null;
    }
    
    public boolean shouldShowRankQuitMessage(String rank) {
        return rankMessages.containsKey(rank.toLowerCase()) && 
               rankMessages.get(rank.toLowerCase()).quitMessage != null;
    }
    
    public String getRankQuitMessage(String rank) {
        RankMessages messages = rankMessages.get(rank.toLowerCase());
        return messages != null ? messages.quitMessage : null;
    }
    
    public boolean shouldShowSkinOnJoin() {
        return showSkinOnJoin;
    }
    
    public int getSkinSize() {
        return skinSize;
    }
    
    private static class RankMessages {
        final String joinGlobal;
        final String joinPersonal;
        final String quitMessage;
        
        RankMessages(String joinGlobal, String joinPersonal, String quitMessage) {
            this.joinGlobal = joinGlobal;
            this.joinPersonal = joinPersonal;
            this.quitMessage = quitMessage;
        }
    }
}