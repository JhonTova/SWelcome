package me.sirius.swelcome.managers;

import me.sirius.core.text.Text;
import org.bukkit.entity.Player;
import me.sirius.swelcome.SWelcome;

public class MessageManager {

    public MessageManager(SWelcome plugin) {
    }
    
    public String processMessage(String message, Player player, String rank) {
        if (message == null) return "";
        
        message = message.replace("{player}", player.getName());
        message = message.replace("{rank}", rank);
        message = message.replace("{displayname}", player.getDisplayName());
        
        return message;
    }
    
    public String processMessageWithPlayerNumber(String message, Player player, String rank, int playerNumber) {
        if (message == null) return "";
        
        message = message.replace("{player}", player.getName());
        message = message.replace("{rank}", rank);
        message = message.replace("{displayname}", player.getDisplayName());
        message = message.replace("{playernumber}", String.valueOf(playerNumber));
        message = message.replace("{number}", String.valueOf(playerNumber));
        
        return message;
    }
    
    public String processMessageWithSkin(String message, Player player, String rank, String skin) {
        if (message == null) return "";
        
        message = message.replace("{player}", player.getName());
        message = message.replace("{rank}", rank);
        message = message.replace("{displayname}", player.getDisplayName());
        
        if (skin != null && !skin.isEmpty()) {
            String[] skinLines = skin.split("\n");
            
            for (int i = 0; i < 8; i++) {
                String skinLine = (i < skinLines.length) ? skinLines[i] : "";
                message = message.replace("{skin_line_" + i + "}", skinLine);
            }
            
            message = message.replace("{skin}", skin);
        } else {
            for (int i = 0; i < 8; i++) {
                message = message.replace("{skin_line_" + i + "}", "        ");
            }
            message = message.replace("{skin}", "");
        }
        
        return message;
    }
    
    public String colorize(String message) {
        if (message == null) {
            return "";
        }
        return Text.legacy(message);
    }
}