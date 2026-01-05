package dev.itzsiden.ecoplus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final DataManager dataManager;
    private final ConfigManager configManager;
    
    public PlayerListener(DataManager dataManager, ConfigManager configManager) {
        this.dataManager = dataManager;
        this.configManager = configManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        String uuid = event.getPlayer().getUniqueId().toString();
        
        // Load or create player data with starting balance from config
        boolean isNewPlayer = !dataManager.playerExists(playerName);
        DataManager.PlayerData data = dataManager.getPlayerData(playerName, uuid);
        
        // If new player, save immediately
        if (isNewPlayer) {
            dataManager.saveData();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save data when player leaves (safety measure)
        dataManager.saveData();
    }
}