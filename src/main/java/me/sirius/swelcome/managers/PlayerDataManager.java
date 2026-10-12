package me.sirius.swelcome.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.sirius.swelcome.SWelcome;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class PlayerDataManager {
    
    private final SWelcome plugin;
    private final File dataFile;
    private final Gson gson;
    private final Object writeLock = new Object();
    private PlayerData playerData;
    private boolean dirty;
    
    public PlayerDataManager(SWelcome plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerData = new PlayerData();
    }
    
    public synchronized void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            dirty = true;
            return;
        }

        try (Reader reader = Files.newBufferedReader(dataFile.toPath(), StandardCharsets.UTF_8)) {
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
            plugin.getLogger().log(Level.SEVERE, "Error al cargar datos de jugadores", e);
        }
    }
    
    // Se serializa dentro del candado y se escribe fuera: el JSON sale de una foto
    // coherente y el disco no bloquea a quien consulta los datos mientras tanto.
    // El segundo candado evita que dos guardados escriban el fichero a la vez.
    public void saveData() {
        String json;
        int jugadores;
        synchronized (this) {
            json = gson.toJson(playerData);
            jugadores = playerData.joinedPlayers.size();
            dirty = false;
        }
        synchronized (writeLock) {
            try (Writer writer = Files.newBufferedWriter(dataFile.toPath(), StandardCharsets.UTF_8)) {
                writer.write(json);
            } catch (IOException e) {
                synchronized (this) {
                    dirty = true;
                }
                plugin.getLogger().log(Level.SEVERE, "Error al guardar datos de jugadores (" + jugadores + ")", e);
            }
        }
    }

    // El autoguardado solo toca el disco si algo cambio desde la ultima vez.
    public void saveIfDirty() {
        boolean pendiente;
        synchronized (this) {
            pendiente = dirty;
        }
        if (pendiente) {
            saveData();
        }
    }

    public synchronized boolean hasJoined(String playerName) {
        return playerData.joinedPlayers.contains(playerName.toLowerCase());
    }

    public int addPlayer(String playerName) {
        String lowerName = playerName.toLowerCase();
        int playerNumber;
        synchronized (this) {
            if (playerData.joinedPlayers.contains(lowerName)) {
                return playerData.playerNumbers.getOrDefault(lowerName, 0);
            }
            playerData.joinedPlayers.add(lowerName);
            playerNumber = ++playerData.totalPlayerCount;
            playerData.playerNumbers.put(lowerName, playerNumber);
            dirty = true;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveIfDirty);
        plugin.getLogger().info("Nuevo jugador agregado: " + playerName + " - Número: #" + playerNumber);
        return playerNumber;
    }

    public synchronized int getPlayerNumber(String playerName) {
        return playerData.playerNumbers.getOrDefault(playerName.toLowerCase(), 0);
    }

    public synchronized int getTotalPlayerCount() {
        return playerData.totalPlayerCount;
    }

    public synchronized boolean hasJoinMessageDisabled(String playerName) {
        return playerData.joinMessagesDisabled.contains(playerName.toLowerCase());
    }

    public boolean toggleJoinMessage(String playerName) {
        String lowerName = playerName.toLowerCase();
        boolean nowDisabled;
        synchronized (this) {
            nowDisabled = !playerData.joinMessagesDisabled.remove(lowerName);
            if (nowDisabled) {
                playerData.joinMessagesDisabled.add(lowerName);
            }
            dirty = true;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveIfDirty);
        return nowDisabled;
    }

    private static class PlayerData {
        Set<String> joinedPlayers = new HashSet<>();
        Map<String, Integer> playerNumbers = new HashMap<>();
        int totalPlayerCount = 0;
        Set<String> joinMessagesDisabled = new HashSet<>();
    }
}