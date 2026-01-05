package dev.itzsiden.ecoplus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageManager {
    
    private final EcoPlus plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;
    
    public MessageManager(EcoPlus plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Load defaults
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            messagesConfig.setDefaults(defConfig);
        }
    }
    
    public void reloadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public String getMessage(String path) {
        String message = messagesConfig.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        
        // Replace prefix
        message = message.replace("{prefix}", plugin.getConfigManager().getPrefix());
        message = message.replace("{currency}", plugin.getConfigManager().getCurrencyName());
        message = message.replace("{symbol}", plugin.getConfigManager().getCurrencySymbol());
        
        // Replace custom placeholders in pairs
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}