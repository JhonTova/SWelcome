package me.sirius.swelcome.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.sirius.swelcome.SWelcome;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerDataManager {
    
    private final SWelcome plugin;
    private final File dataFile;
    private final Gson gson;
    private PlayerData playerData;
    
    public PlayerDataManager(SWelcome plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerData = new PlayerData();
    }
    
    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            saveData();
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            PlayerData loaded = gson.fromJson(reader, PlayerData.class);
            if (loaded != null) {
                playerData = loaded;
                // Compatibilidad con archivos viejos: inicializar campos que podrían ser null
                if (playerData.joinedPlayers == null) playerData.joinedPlayers = new HashSet<>();
                if (playerData.playerNumbers == null) playerData.playerNumbers = new HashMap<>();
                if (playerData.joinMessagesDisabled == null) playerData.joinMessagesDisabled = new HashSet<>();
                
                plugin.getLogger().info("Datos cargados: " + playerData.joinedPlayers.size() + " jugadores, contador en " + playerData.totalPlayerCount);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Error al cargar datos de jugadores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(playerData, writer);
            writer.flush();
            plugin.getLogger().info("Datos guardados: " + playerData.joinedPlayers.size() + " jugadores, contador en " + playerData.totalPlayerCount);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar datos de jugadores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean hasJoined(String playerName) {
        return playerData.joinedPlayers.contains(playerName.toLowerCase());
    }
    
    public int addPlayer(String playerName) {
        String lowerName = playerName.toLowerCase();
        if (!playerData.joinedPlayers.contains(lowerName)) {
            playerData.joinedPlayers.add(lowerName);
            playerData.totalPlayerCount++;
            
            int playerNumber = playerData.totalPlayerCount;
            playerData.playerNumbers.put(lowerName, playerNumber);
            
            saveData();
            plugin.getLogger().info("Nuevo jugador agregado: " + playerName + " - Número: #" + playerNumber);
            return playerNumber;
        }
        return playerData.playerNumbers.getOrDefault(lowerName, 0);
    }
    
    public int getPlayerNumber(String playerName) {
        return playerData.playerNumbers.getOrDefault(playerName.toLowerCase(), 0);
    }
    
    public int getTotalPlayerCount() {
        return playerData.totalPlayerCount;
    }
    
    public boolean hasJoinMessageDisabled(String playerName) {
        return playerData.joinMessagesDisabled.contains(playerName.toLowerCase());
    }
    
    public boolean toggleJoinMessage(String playerName) {
        String lowerName = playerName.toLowerCase();
        boolean nowDisabled;
        
        if (playerData.joinMessagesDisabled.contains(lowerName)) {
            playerData.joinMessagesDisabled.remove(lowerName);
            nowDisabled = false;
        } else {
            playerData.joinMessagesDisabled.add(lowerName);
            nowDisabled = true;
        }
        
        // Guardar asíncronamente para no bloquear el hilo principal
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveData);
        
        return nowDisabled;
    }
    
    private static class PlayerData {
        Set<String> joinedPlayers = new HashSet<>();
        Map<String, Integer> playerNumbers = new HashMap<>();
        int totalPlayerCount = 0;
        Set<String> joinMessagesDisabled = new HashSet<>();
    }
}