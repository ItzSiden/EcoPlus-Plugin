package dev.itzsiden.ecoplus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    
    private final EcoPlus plugin;
    private final File dataFile;
    private final Gson gson;
    private final Map<String, PlayerData> playerDataMap;
    
    public DataManager(EcoPlus plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/player_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerDataMap = new ConcurrentHashMap<>();
    }
    
    public void loadData() {
        // Create data directory if it doesn't exist
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        
        // Create file if it doesn't exist
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                saveData(); // Save empty map to create proper JSON structure
                plugin.getLogger().info("Created new player_data.json file");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create player_data.json: " + e.getMessage());
            }
            return;
        }
        
        // Load existing data
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                playerDataMap.clear();
                playerDataMap.putAll(loaded);
                plugin.getLogger().info("Loaded " + playerDataMap.size() + " player records");
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load player_data.json: " + e.getMessage());
        }
    }
    
    public void saveData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(playerDataMap, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player_data.json: " + e.getMessage());
        }
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getName(), player.getUniqueId().toString());
    }
    
    public PlayerData getPlayerData(String playerName, String uuid) {
        return playerDataMap.computeIfAbsent(playerName, k -> 
            new PlayerData(uuid, plugin.getConfigManager().getStartingBalance()));
    }
    
    public PlayerData getPlayerDataByName(String playerName) {
        return playerDataMap.get(playerName);
    }
    
    public double getBalance(String playerName) {
        PlayerData data = playerDataMap.get(playerName);
        return data != null ? data.getBalance() : 0;
    }
    
    public void setBalance(String playerName, double amount) {
        PlayerData data = playerDataMap.get(playerName);
        if (data != null) {
            // Clamp to max balance
            double maxBalance = plugin.getConfigManager().getMaxBalance();
            amount = Math.min(amount, maxBalance);
            amount = Math.max(0, amount); // Prevent negative
            
            data.setBalance(amount);
            saveData();
        }
    }
    
    public boolean addBalance(String playerName, double amount) {
        PlayerData data = playerDataMap.get(playerName);
        if (data != null) {
            double currentBalance = data.getBalance();
            double newBalance = currentBalance + amount;
            double maxBalance = plugin.getConfigManager().getMaxBalance();
            
            // Check if it would exceed max balance
            if (newBalance > maxBalance) {
                newBalance = maxBalance;
            }
            
            data.setBalance(newBalance);
            saveData();
            
            // Log transaction if enabled
            if (plugin.getConfigManager().isTransactionLoggingEnabled()) {
                plugin.getLogger().info(String.format("[ADD] %s: %.2f -> %.2f (+%.2f)", 
                    playerName, currentBalance, newBalance, amount));
            }
            
            return true;
        }
        return false;
    }
    
    public boolean removeBalance(String playerName, double amount) {
        PlayerData data = playerDataMap.get(playerName);
        if (data != null) {
            double currentBalance = data.getBalance();
            
            // Check if player has enough funds
            if (currentBalance < amount) {
                return false; // Insufficient funds
            }
            
            double newBalance = currentBalance - amount;
            data.setBalance(newBalance);
            saveData();
            
            // Log transaction if enabled
            if (plugin.getConfigManager().isTransactionLoggingEnabled()) {
                plugin.getLogger().info(String.format("[TAKE] %s: %.2f -> %.2f (-%.2f)", 
                    playerName, currentBalance, newBalance, amount));
            }
            
            return true;
        }
        return false;
    }
    
    public boolean hasBalance(String playerName, double amount) {
        PlayerData data = playerDataMap.get(playerName);
        return data != null && data.getBalance() >= amount;
    }
    
    public List<Map.Entry<String, PlayerData>> getTopBalances(int limit) {
        return playerDataMap.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getBalance(), e1.getValue().getBalance()))
                .limit(limit)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public boolean playerExists(String playerName) {
        return playerDataMap.containsKey(playerName);
    }
    
    // Formatting methods
    public String formatBalance(double balance) {
        ConfigManager config = plugin.getConfigManager();
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(config.getDecimalPlaces());
        df.setMaximumFractionDigits(config.getDecimalPlaces());
        df.setGroupingUsed(config.useSeparators());
        return df.format(balance);
    }
    
    public String formatBalanceShorthand(double balance) {
        if (balance < 1000) {
            return String.format("%.1f", balance);
        } else if (balance < 1000000) {
            return String.format("%.1fk", balance / 1000);
        } else if (balance < 1000000000) {
            return String.format("%.1fM", balance / 1000000);
        } else if (balance < 1000000000000L) {
            return String.format("%.1fB", balance / 1000000000);
        } else {
            return String.format("%.1fT", balance / 1000000000000L);
        }
    }
    
    public static class PlayerData {
        private String uuid;
        private double balance;
        
        public PlayerData(String uuid, double balance) {
            this.uuid = uuid;
            this.balance = balance;
        }
        
        public String getUuid() {
            return uuid;
        }
        
        public double getBalance() {
            return balance;
        }
        
        public void setBalance(double balance) {
            this.balance = balance;
        }
    }
}