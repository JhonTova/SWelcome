package me.sirius.swelcome.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import me.sirius.swelcome.SWelcome;
import me.sirius.swelcome.managers.MessageManager;
import me.sirius.swelcome.managers.PlayerDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SWelcomeCommand implements CommandExecutor, TabCompleter {
    
    private final SWelcome plugin;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messageManager;
    
    public SWelcomeCommand(SWelcome plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.messageManager = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "reload":
                return handleReload(sender);
            case "toggle":
                return handleToggle(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(messageManager.colorize("&cSubcomando desconocido. Usa &e/swelcome help"));
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("swelcome.admin")) {
            sender.sendMessage(messageManager.colorize("&cNo tienes permiso para usar este comando."));
            return true;
        }
        
        try {
            plugin.getConfigManager().loadConfig();
            sender.sendMessage(messageManager.colorize("&a&l[SWelcome] &aConfiguración recargada correctamente."));
            plugin.getLogger().info("Configuración recargada por " + sender.getName());
        } catch (Exception e) {
            sender.sendMessage(messageManager.colorize("&c&l[SWelcome] &cError al recargar la configuración: " + e.getMessage()));
            plugin.getLogger().severe("Error al recargar la configuración: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean handleToggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.colorize("&cEste comando solo puede ser usado por jugadores."));
            return true;
        }
        
        Player player = (Player) sender;
        boolean nowDisabled = playerDataManager.toggleJoinMessage(player.getName());
        
        if (nowDisabled) {
            player.sendMessage(messageManager.colorize(
                "&7&m-----------------------------------"));
            player.sendMessage(messageManager.colorize(
                "&c&l✘ &cMensajes globales &lDESACTIVADOS"));
            player.sendMessage(messageManager.colorize(
                "&7Los demás jugadores ya no verán tus mensajes de entrada ni de salida."));
            player.sendMessage(messageManager.colorize(
                "&7Tu mensaje personal seguirá apareciendo."));
            player.sendMessage(messageManager.colorize(
                "&7&m-----------------------------------"));
        } else {
            player.sendMessage(messageManager.colorize(
                "&7&m-----------------------------------"));
            player.sendMessage(messageManager.colorize(
                "&a&l✔ &aMensajes globales &lACTIVADOS"));
            player.sendMessage(messageManager.colorize(
                "&7Los demás jugadores volverán a ver tus mensajes de entrada y salida."));
            player.sendMessage(messageManager.colorize(
                "&7&m-----------------------------------"));
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messageManager.colorize("&7&m-----------------------------------"));
        sender.sendMessage(messageManager.colorize("&e&lSWelcome &7- Comandos disponibles"));
        sender.sendMessage(messageManager.colorize("&7&m-----------------------------------"));
        sender.sendMessage(messageManager.colorize("&e/swelcome toggle &7- Activa/desactiva tus mensajes globales (entrada y salida)"));
        
        if (sender.hasPermission("swelcome.admin")) {
            sender.sendMessage(messageManager.colorize("&e/swelcome reload &7- Recarga la configuración del plugin"));
        }
        
        sender.sendMessage(messageManager.colorize("&e/swelcome help &7- Muestra esta ayuda"));
        sender.sendMessage(messageManager.colorize("&7&m-----------------------------------"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("toggle", "help"));
            if (sender.hasPermission("swelcome.admin")) {
                subcommands.add("reload");
            }
            
            List<String> filtered = new ArrayList<>();
            String partial = args[0].toLowerCase();
            for (String sub : subcommands) {
                if (sub.startsWith(partial)) {
                    filtered.add(sub);
                }
            }
            return filtered;
        }
        
        return Collections.emptyList();
    }
}