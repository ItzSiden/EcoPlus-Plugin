package dev.itzsiden.ecoplus;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final EcoPlus plugin;
    private FileConfiguration config;
    
    public ConfigManager(EcoPlus plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public String getPrefix() {
        return config.getString("prefix", "&6[&eEcoPlus&6]&r");
    }
    
    public String getCurrencyName() {
        return config.getString("currency.name", "Stars");
    }
    
    public String getCurrencySymbol() {
        return config.getString("currency.symbol", "⭐");
    }
    
    public double getStartingBalance() {
        return config.getDouble("starting-balance", 0);
    }
    
    public boolean useSeparators() {
        return config.getBoolean("formatting.use-separators", true);
    }
    
    public int getDecimalPlaces() {
        return config.getInt("formatting.decimal-places", 2);
    }
    
    public int getBaltopCount() {
        return config.getInt("baltop.top-count", 10);
    }
    
    public int getCacheDuration() {
        return config.getInt("baltop.cache-duration", 300);
    }
    
    public double getMaxBalance() {
        return config.getDouble("security.max-balance", 1000000000);
    }
    
    public double getMaxTransaction() {
        return config.getDouble("security.max-transaction", 100000000);
    }
    
    public double getMinTransaction() {
        return config.getDouble("security.min-transaction", 0.01);
    }
    
    public boolean isTransactionLoggingEnabled() {
        return config.getBoolean("security.log-transactions", true);
    }
}