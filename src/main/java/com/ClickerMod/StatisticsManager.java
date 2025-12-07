package com.ClickerMod;

import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsManager {
    
    private Map<String, Integer> triggerWordStats = new HashMap<String, Integer>();
    private Map<String, Integer> playerTriggerStats = new HashMap<String, Integer>();
    
    private long lastStatsSave = 0;
    private static final long STATS_SAVE_INTERVAL = 30000; // 30 seconds
    private boolean statsDirty = false;
    
    private boolean trackingEnabled = false;
    private Configuration config;
    
    public StatisticsManager(Configuration config) {
        this.config = config;
    }
    
    public void setTrackingEnabled(boolean enabled) {
        this.trackingEnabled = enabled;
    }
    
    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }
    
    public void recordTriggerWord(String trigger) {
        if (!trackingEnabled) return;
        
        Integer count = triggerWordStats.get(trigger);
        triggerWordStats.put(trigger, count == null ? 1 : count + 1);
        statsDirty = true;
        saveIfNeeded();
    }
    
    public void recordPlayerTrigger(String playerName) {
        if (!trackingEnabled) return;
        
        Integer count = playerTriggerStats.get(playerName);
        playerTriggerStats.put(playerName, count == null ? 1 : count + 1);
        statsDirty = true;
        saveIfNeeded();
    }
    
    private void saveIfNeeded() {
        long now = System.currentTimeMillis();
        if (statsDirty && (now - lastStatsSave > STATS_SAVE_INTERVAL)) {
            save();
            statsDirty = false;
            lastStatsSave = now;
        }
    }
    
    public void forceSave() {
        if (statsDirty) {
            save();
            statsDirty = false;
            lastStatsSave = System.currentTimeMillis();
        }
    }
    
    public void clear() {
        triggerWordStats.clear();
        playerTriggerStats.clear();
        save();
    }
    
    private void save() {
        if (config == null) {
            return;
        }
        
        String category = "statistics";
        StringBuilder sb = new StringBuilder(256);
        
        for (Map.Entry<String, Integer> entry : triggerWordStats.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        config.get(category, "triggerWordStats", "").set(sb.toString());
        
        sb.setLength(0);
        for (Map.Entry<String, Integer> entry : playerTriggerStats.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        config.get(category, "playerTriggerStats", "").set(sb.toString());
        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    public void load() {
        if (config == null) {
            return;
        }
        
        String category = "statistics";
        
        String triggerStatsStr = config.getString(
                "triggerWordStats",
                category,
                "",
                "Trigger word statistics (internal use)"
        );
        
        if (!triggerStatsStr.isEmpty()) {
            String[] entries = triggerStatsStr.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    try {
                        String trigger = parts[0];
                        int count = Integer.parseInt(parts[1]);
                        triggerWordStats.put(trigger, count);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }
        
        String playerStatsStr = config.getString(
                "playerTriggerStats",
                category,
                "",
                "Player trigger statistics (internal use)"
        );
        
        if (!playerStatsStr.isEmpty()) {
            String[] entries = playerStatsStr.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    try {
                        String player = parts[0];
                        int count = Integer.parseInt(parts[1]);
                        playerTriggerStats.put(player, count);
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
        }
    }
    
    public List<Map.Entry<String, Integer>> getSortedTriggerWordStats() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<Map.Entry<String, Integer>>(triggerWordStats.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        return sorted;
    }
    
    public List<Map.Entry<String, Integer>> getSortedPlayerStats() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<Map.Entry<String, Integer>>(playerTriggerStats.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        return sorted;
    }
    
    public int getTotalTriggerWordCount() {
        int total = 0;
        for (Integer count : triggerWordStats.values()) {
            total += count;
        }
        return total;
    }
    
    public int getTotalPlayerTriggerCount() {
        int total = 0;
        for (Integer count : playerTriggerStats.values()) {
            total += count;
        }
        return total;
    }
    
    public boolean hasTriggerWordStats() {
        return !triggerWordStats.isEmpty();
    }
    
    public boolean hasPlayerStats() {
        return !playerTriggerStats.isEmpty();
    }
}