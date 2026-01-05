package dev.itzsiden.ecoplus;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EcoPlusPlaceholders extends PlaceholderExpansion {
    
    private final EcoPlus plugin;
    private final DataManager dataManager;
    
    public EcoPlusPlaceholders(EcoPlus plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    
    @Override
    @NotNull
    public String getAuthor() {
        return "ItzSiden";
    }
    
    @Override
    @NotNull
    public String getIdentifier() {
        return "ecoplus";
    }
    
    @Override
    @NotNull
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        String playerName = player.getName();
        if (playerName == null || !dataManager.playerExists(playerName)) {
            return "0";
        }
        
        double balance = dataManager.getBalance(playerName);
        
        // %ecoplus_eco%
        if (params.equalsIgnoreCase("eco")) {
            return String.valueOf((long) balance);
        }
        
        // %ecoplus_eco_formatted%
        if (params.equalsIgnoreCase("eco_formatted")) {
            return dataManager.formatBalance(balance);
        }
        
        // %ecoplus_eco_shorthand%
        if (params.equalsIgnoreCase("eco_shorthand")) {
            return dataManager.formatBalanceShorthand(balance);
        }
        
        return null;
    }
}