package dev.itzsiden.ecoplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final EcoPlus plugin;
    private final DataManager dataManager;
    private final MessageManager messageManager;
    
    public CommandHandler(EcoPlus plugin, DataManager dataManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.messageManager = messageManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "balance":
            case "bal":
                return handleBalance(sender, args);
                
            case "baltop":
            case "top":
                return handleBaltop(sender);
                
            case "add":
                return handleAdd(sender, args);
                
            case "take":
            case "remove":
                return handleTake(sender, args);
                
            case "reload":
                return handleReload(sender);
                
            default:
                sender.sendMessage(messageManager.getMessage("unknown-command"));
                return true;
        }
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecoplus.use")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        String targetName;
        
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageManager.getMessage("console-specify-player"));
                return true;
            }
            targetName = sender.getName();
        } else {
            targetName = args[1];
        }
        
        if (!dataManager.playerExists(targetName)) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "{player}", targetName));
            return true;
        }
        
        double balance = dataManager.getBalance(targetName);
        String formattedBalance = dataManager.formatBalance(balance);
        
        if (targetName.equals(sender.getName())) {
            sender.sendMessage(messageManager.getMessage("balance-self", 
                "{amount}", formattedBalance));
        } else {
            sender.sendMessage(messageManager.getMessage("balance-other",
                "{player}", targetName,
                "{amount}", formattedBalance));
        }
        
        return true;
    }
    
    private boolean handleBaltop(CommandSender sender) {
        if (!sender.hasPermission("ecoplus.use")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        int topCount = plugin.getConfigManager().getBaltopCount();
        List<Map.Entry<String, DataManager.PlayerData>> topPlayers = dataManager.getTopBalances(topCount);
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage(messageManager.getMessage("baltop-empty"));
            return true;
        }
        
        String header = messageManager.getMessage("baltop-header", "{count}", String.valueOf(topCount));
        sender.sendMessage(header);
        
        int rank = 1;
        for (Map.Entry<String, DataManager.PlayerData> entry : topPlayers) {
            String playerName = entry.getKey();
            double balance = entry.getValue().getBalance();
            String formattedBalance = dataManager.formatBalance(balance);
            
            String rankColor;
            switch (rank) {
                case 1:
                    rankColor = "&6";
                    break;
                case 2:
                    rankColor = "&7";
                    break;
                case 3:
                    rankColor = "&e";
                    break;
                default:
                    rankColor = "&f";
            }
            
            String entry_msg = messageManager.getMessage("baltop-entry",
                "{rank_color}", rankColor,
                "{rank}", String.valueOf(rank),
                "{player}", playerName,
                "{amount}", formattedBalance);
            
            sender.sendMessage(entry_msg);
            rank++;
        }
        
        sender.sendMessage(messageManager.getMessage("baltop-footer"));
        return true;
    }
    
    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecoplus.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(messageManager.getMessage("usage-add"));
            return true;
        }
        
        String targetName = args[1];
        double amount;
        
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messageManager.getMessage("invalid-amount"));
            return true;
        }
        
        // Validate amount
        ConfigManager config = plugin.getConfigManager();
        
        if (amount < config.getMinTransaction()) {
            sender.sendMessage(messageManager.getMessage("must-be-positive"));
            return true;
        }
        
        if (amount > config.getMaxTransaction()) {
            sender.sendMessage(messageManager.getMessage("amount-too-large",
                "{max}", dataManager.formatBalance(config.getMaxTransaction())));
            return true;
        }
        
        if (!dataManager.playerExists(targetName)) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "{player}", targetName));
            return true;
        }
        
        double currentBalance = dataManager.getBalance(targetName);
        double maxBalance = config.getMaxBalance();
        
        // Check if adding would exceed max balance
        if (currentBalance + amount > maxBalance) {
            amount = maxBalance - currentBalance;
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Player has reached maximum balance limit!");
                return true;
            }
        }
        
        boolean success = dataManager.addBalance(targetName, amount);
        
        if (!success) {
            sender.sendMessage(ChatColor.RED + "Failed to add balance. Please try again.");
            return true;
        }
        
        double newBalance = dataManager.getBalance(targetName);
        String formattedAmount = dataManager.formatBalance(amount);
        String formattedBalance = dataManager.formatBalance(newBalance);
        
        sender.sendMessage(messageManager.getMessage("add-success",
            "{player}", targetName,
            "{amount}", formattedAmount));
        sender.sendMessage(messageManager.getMessage("add-new-balance",
            "{balance}", formattedBalance));
        
        Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            target.sendMessage(messageManager.getMessage("add-notification",
                "{amount}", formattedAmount));
        }
        
        return true;
    }
    
    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ecoplus.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(messageManager.getMessage("usage-take"));
            return true;
        }
        
        String targetName = args[1];
        double amount;
        
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(messageManager.getMessage("invalid-amount"));
            return true;
        }
        
        // Validate amount
        ConfigManager config = plugin.getConfigManager();
        
        if (amount < config.getMinTransaction()) {
            sender.sendMessage(messageManager.getMessage("must-be-positive"));
            return true;
        }
        
        if (amount > config.getMaxTransaction()) {
            sender.sendMessage(messageManager.getMessage("amount-too-large",
                "{max}", dataManager.formatBalance(config.getMaxTransaction())));
            return true;
        }
        
        if (!dataManager.playerExists(targetName)) {
            sender.sendMessage(messageManager.getMessage("player-not-found", "{player}", targetName));
            return true;
        }
        
        double currentBalance = dataManager.getBalance(targetName);
        
        // CHECK: Insufficient funds validation
        if (!dataManager.hasBalance(targetName, amount)) {
            sender.sendMessage(messageManager.getMessage("insufficient-funds",
                "{player}", targetName,
                "{balance}", dataManager.formatBalance(currentBalance)));
            return true;
        }
        
        boolean success = dataManager.removeBalance(targetName, amount);
        
        if (!success) {
            sender.sendMessage(messageManager.getMessage("insufficient-funds",
                "{player}", targetName,
                "{balance}", dataManager.formatBalance(currentBalance)));
            return true;
        }
        
        double newBalance = dataManager.getBalance(targetName);
        String formattedAmount = dataManager.formatBalance(amount);
        String formattedBalance = dataManager.formatBalance(newBalance);
        
        sender.sendMessage(messageManager.getMessage("take-success",
            "{player}", targetName,
            "{amount}", formattedAmount));
        sender.sendMessage(messageManager.getMessage("take-new-balance",
            "{balance}", formattedBalance));
        
        Player target = Bukkit.getPlayer(targetName);
        if (target != null && target.isOnline()) {
            target.sendMessage(messageManager.getMessage("take-notification",
                "{amount}", formattedAmount));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ecoplus.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        plugin.getConfigManager().reloadConfig();
        messageManager.reloadMessages();
        dataManager.loadData();
        
        sender.sendMessage(messageManager.getMessage("plugin-reloaded"));
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messageManager.getMessage("help-header"));
        sender.sendMessage(messageManager.getMessage("help-balance"));
        sender.sendMessage(messageManager.getMessage("help-baltop"));
        
        if (sender.hasPermission("ecoplus.admin")) {
            sender.sendMessage(messageManager.getMessage("help-add"));
            sender.sendMessage(messageManager.getMessage("help-take"));
            sender.sendMessage(messageManager.getMessage("help-reload"));
        }
        
        sender.sendMessage(messageManager.getMessage("help-footer"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("balance", "baltop", "add", "take", "reload");
            return subCommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("balance") || 
                                  args[0].equalsIgnoreCase("add") || 
                                  args[0].equalsIgnoreCase("take"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}