package dev.itzsiden.ecoplus;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EcoPlus extends JavaPlugin {
    
    private DataManager dataManager;
    private CommandHandler commandHandler;
    private ConfigManager configManager;
    private MessageManager messageManager;
    
    @Override
    public void onEnable() {
        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Initialize config and messages
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        
        // Initialize data manager
        dataManager = new DataManager(this);
        dataManager.loadData();
        
        // Register command handler
        commandHandler = new CommandHandler(this, dataManager, messageManager);
        getCommand("ecoplus").setExecutor(commandHandler);
        getCommand("ecoplus").setTabCompleter(commandHandler);
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new PlayerListener(dataManager, configManager), this);
        
        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EcoPlusPlaceholders(this, dataManager).register();
            getLogger().info("PlaceholderAPI hook registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will not work.");
        }
        
        getLogger().info("EcoPlus has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all player data before shutdown
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("EcoPlus has been disabled!");
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
}