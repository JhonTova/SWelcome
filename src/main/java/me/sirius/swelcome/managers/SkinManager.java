package me.sirius.swelcome.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SkinManager {
    
    private final Map<String, CachedSkin> skinCache = new HashMap<>();
    private static final String SYMBOL = "█";
    private static final int HEAD_SIZE = 8;
    private static final long CACHE_DURATION = TimeUnit.HOURS.toMillis(6);
    private static final int TIMEOUT_MS = 3000;
    
    private static class CachedSkin {
        final String skinArt;
        final long timestamp;
        
        CachedSkin(String skinArt) {
            this.skinArt = skinArt;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
    
    public CompletableFuture<String> getSkinArtAsync(Player player, boolean isBedrockPlayer) {
        return CompletableFuture.supplyAsync(() -> getSkinArt(player, isBedrockPlayer));
    }
    
    public String getSkinArt(Player player, boolean isBedrockPlayer) {
        String playerName = player.getName();
        
        CachedSkin cached = skinCache.get(playerName);
        if (cached != null && !cached.isExpired()) {
            return cached.skinArt;
        }
        
        if (isBedrockPlayer) {
            String skinArt = createBedrockSkinArt();
            skinCache.put(playerName, new CachedSkin(skinArt));
            return skinArt;
        }
        
        try {
            String skinArt = generateSkinArt(playerName);
            if (skinArt != null && !skinArt.isEmpty()) {
                skinCache.put(playerName, new CachedSkin(skinArt));
                return skinArt;
            }
        } catch (Exception e) {
            System.err.println("[SWelcome] Error generating skin for " + playerName + ": " + e.getMessage());
        }
        
        String defaultSkin = createDefaultSkinArt();
        skinCache.put(playerName, new CachedSkin(defaultSkin));
        return defaultSkin;
    }
    
    private String generateSkinArt(String playerName) {
        HttpURLConnection connection = null;
        
        try {
            URL url = new URL("https://minotar.net/helm/" + playerName + "/" + HEAD_SIZE);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "SWelcome-Plugin/1.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("[SWelcome] HTTP " + responseCode + " for player " + playerName);
                return null;
            }
            
            BufferedImage image = ImageIO.read(connection.getInputStream());
            
            if (image == null) {
                return null;
            }
            
            return imageToSkinArt(image);
            
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("[SWelcome] Timeout getting skin for " + playerName);
            return null;
        } catch (Exception e) {
            System.err.println("[SWelcome] Error generating skin art for " + playerName + ": " + e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ignored) {}
            }
        }
    }
    
    private String imageToSkinArt(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int size = Math.min(HEAD_SIZE, Math.min(height, width));
        
        double mult1 = (double) height / size;
        double mult2 = (double) width / size;
        
        StringBuilder result = new StringBuilder(size * size * 10);
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int pixelX = Math.min((int) (x * mult2), width - 1);
                int pixelY = Math.min((int) (y * mult1), height - 1);
                
                int rgb = image.getRGB(pixelX, pixelY);
                Color color = new Color(rgb, true);
                
                if (color.getAlpha() < 10) {
                    result.append("&#000000").append(SYMBOL);
                    continue;
                }
                
                String hex = String.format("%02x%02x%02x", 
                    color.getRed(), 
                    color.getGreen(), 
                    color.getBlue());
                result.append("&#").append(hex).append(SYMBOL);
            }
            if (y < size - 1) {
                result.append("\n");
            }
        }
        
        return result.toString();
    }
    
    private String createBedrockSkinArt() {
        return "&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█\n" +
               "&#7EC8E3█&#000000█&#000000█&#7EC8E3█&#7EC8E3█&#000000█&#000000█&#7EC8E3█\n" +
               "&#7EC8E3█&#000000█&#000000█&#7EC8E3█&#7EC8E3█&#000000█&#000000█&#7EC8E3█\n" +
               "&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█\n" +
               "&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█\n" +
               "&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█\n" +
               "&#7EC8E3█&#7EC8E3█&#FFFFFF█&#7EC8E3█&#7EC8E3█&#FFFFFF█&#7EC8E3█&#7EC8E3█\n" +
               "&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█&#7EC8E3█";
    }
    
    private String createDefaultSkinArt() {
        return "&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█\n" +
               "&#8B7355█&#000000█&#000000█&#8B7355█&#8B7355█&#000000█&#000000█&#8B7355█\n" +
               "&#8B7355█&#000000█&#000000█&#8B7355█&#8B7355█&#000000█&#000000█&#8B7355█\n" +
               "&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█\n" +
               "&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█\n" +
               "&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█\n" +
               "&#8B7355█&#8B7355█&#FF0000█&#8B7355█&#8B7355█&#FF0000█&#8B7355█&#8B7355█\n" +
               "&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█&#8B7355█";
    }
    
    public void cleanExpiredCache() {
        skinCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public void clearCache() {
        skinCache.clear();
    }
    
    public int getCacheSize() {
        return skinCache.size();
    }
}