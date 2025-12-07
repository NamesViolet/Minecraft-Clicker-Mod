package com.ClickerMod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Mod(modid = ChatSoundMod.MODID,
     name = ChatSoundMod.NAME,
     version = ChatSoundMod.VERSION,
     clientSideOnly = true,
     acceptedMinecraftVersions = "[1.8.9]")
public class ChatSoundMod {

    public static final String MODID = "chatsoundmod";
    public static final String NAME = "Chat Sound Mod";
    public static final String VERSION = "2.1.0";

    // Configuration variables
    public static boolean enabled = true;
    public static String triggerMessage = "~click~";
    public static float soundVolume = 1.0F;
    public static boolean ignoreSelf = false;
    public static boolean whitelistEnabled = false;
    public static String whitelistPlayers = "";
    public static boolean blacklistEnabled = false;
    public static String blacklistPlayers = "";

    // HashSet for O(1) player lookups
    public static Set<String> cachedWhitelistSet = new HashSet<String>();
    public static Set<String> cachedBlacklistSet = new HashSet<String>();
    
    // Keep triggers as array
    public static String[] cachedTriggers = new String[0];

    // Pagination for statistics
    public static int currentStatsPage = 1;
    public static String currentStatsType = "";

    private static ChatSoundMod INSTANCE;
    private Configuration config;
    
    // Managers
    public static StatisticsManager statsManager;
    public static SoundManager soundManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        
        File customSoundsDir = new File(event.getModConfigurationDirectory(), "chatsoundmod/sounds");
        if (!customSoundsDir.exists()) {
            customSoundsDir.mkdirs();
        }
        
        // Initialize managers
        statsManager = new StatisticsManager(config);
        soundManager = new SoundManager(customSoundsDir);
        
        syncConfig();
        statsManager.load();
        rebuildCaches();
        soundManager.refreshSoundCache();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChatSoundHandler());
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ChatSoundCommand());
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                statsManager.forceSave();
                ChatSoundHandler.shutdown();
            }
        }));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isGamePaused() || mc.theWorld == null) {
            return;
        }
    }

    public static void rebuildCaches() {
        // Cache triggers
        if (triggerMessage != null && !triggerMessage.trim().isEmpty()) {
            String[] split = triggerMessage.split(",");
            int count = 0;
            for (String s : split) {
                if (!s.trim().isEmpty()) count++;
            }
            cachedTriggers = new String[count];
            int i = 0;
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    cachedTriggers[i++] = trimmed.toLowerCase();
                }
            }
        } else {
            cachedTriggers = new String[0];
        }
        
        // Cache whitelist as HashSet
        cachedWhitelistSet.clear();
        if (whitelistPlayers != null && !whitelistPlayers.trim().isEmpty()) {
            String[] split = whitelistPlayers.split(",");
            int capacity = (int)(split.length / 0.75f) + 1;
            Set<String> newSet = new HashSet<String>(capacity);
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    newSet.add(trimmed.toLowerCase());
                }
            }
            cachedWhitelistSet = newSet;
        }
        
        // Cache blacklist as HashSet
        cachedBlacklistSet.clear();
        if (blacklistPlayers != null && !blacklistPlayers.trim().isEmpty()) {
            String[] split = blacklistPlayers.split(",");
            int capacity = (int)(split.length / 0.75f) + 1;
            Set<String> newSet = new HashSet<String>(capacity);
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    newSet.add(trimmed.toLowerCase());
                }
            }
            cachedBlacklistSet = newSet;
        }
    }

    public static void setTriggerMessage(String value) {
        triggerMessage = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "triggerMessage", "~click~").set(triggerMessage);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
        
        rebuildCaches();
    }

    public static void setSoundVolume(float value) {
        soundVolume = Math.max(0.0F, Math.min(2.0F, value));

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "soundVolume", 1.0F).set(soundVolume);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setSelectedSound(String value) {
        soundManager.setSelectedSound(value);

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "selectedSound", "click").set(soundManager.getSelectedSound());
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }
    
    public static void setRandomizeSounds(boolean value) {
        soundManager.setRandomizeSounds(value);

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "randomizeSounds", false).set(soundManager.isRandomizeSounds());
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }
    
    public static void setSoundPool(String value) {
        soundManager.setSoundPool(value);

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "soundPool", "click,click1,click2,click3").set(soundManager.getSoundPool());
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setIgnoreSelf(boolean value) {
        ignoreSelf = value;

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "ignoreSelf", false).set(ignoreSelf);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setTrackStats(boolean value) {
        statsManager.setTrackingEnabled(value);

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "trackStats", false).set(statsManager.isTrackingEnabled());
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setWhitelistEnabled(boolean value) {
        whitelistEnabled = value;

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "whitelistEnabled", false).set(whitelistEnabled);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setWhitelistPlayers(String value) {
        whitelistPlayers = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "whitelistPlayers", "").set(whitelistPlayers);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
        
        rebuildCaches();
    }

    public static void setBlacklistEnabled(boolean value) {
        blacklistEnabled = value;

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "blacklistEnabled", false).set(blacklistEnabled);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    public static void setBlacklistPlayers(String value) {
        blacklistPlayers = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "blacklistPlayers", "").set(blacklistPlayers);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
        
        rebuildCaches();
    }

    private void syncConfig() {
        String category = "general";

        enabled = config.getBoolean(
                "enabled",
                category,
                true,
                "Enable or disable playing a sound when sending a chat message."
        );

        triggerMessage = config.getString(
                "triggerMessage",
                category,
                "~click~",
                "Comma-separated list of trigger texts. If the chat message contains ANY of them (case-insensitive), a sound will be played."
        );

        soundVolume = config.getFloat(
                "soundVolume",
                category,
                1.0F,
                0.0F,
                2.0F,
                "Volume for the sound (0.0 - 2.0). This bypasses Minecraft's volume settings. Values above 1.0 will be louder than normal."
        );

        String selectedSound = config.getString(
                "selectedSound",
                category,
                "click",
                "Name of the sound file to play (without extension). Looks for this file in the mod's sounds or in config/chatsoundmod/sounds/"
        );
        soundManager.setSelectedSound(selectedSound);
        
        boolean randomizeSounds = config.getBoolean(
                "randomizeSounds",
                category,
                false,
                "If true, randomly select from sound pool instead of using selected sound."
        );
        soundManager.setRandomizeSounds(randomizeSounds);
        
        String soundPool = config.getString(
                "soundPool",
                category,
                "click,click1,click2,click3",
                "Comma-separated list of sound names to randomly choose from when randomization is enabled."
        );
        soundManager.setSoundPool(soundPool);

        ignoreSelf = config.getBoolean(
                "ignoreSelf",
                category,
                false,
                "If true, your own chat messages will NOT trigger the sound; only other players' messages will."
        );

        boolean trackStats = config.getBoolean(
                "trackStats",
                category,
                false,
                "If true, track statistics about trigger words and players. Statistics are saved every 30 seconds."
        );
        statsManager.setTrackingEnabled(trackStats);

        whitelistEnabled = config.getBoolean(
                "whitelistEnabled",
                category,
                false,
                "If true, only players in the whitelist can trigger the sound."
        );

        whitelistPlayers = config.getString(
                "whitelistPlayers",
                category,
                "",
                "Comma-separated list of player names allowed to trigger the sound when whitelist is enabled."
        );

        blacklistEnabled = config.getBoolean(
                "blacklistEnabled",
                category,
                false,
                "If true, players in the blacklist cannot trigger the sound (even if they match triggers)."
        );

        blacklistPlayers = config.getString(
                "blacklistPlayers",
                category,
                "",
                "Comma-separated list of player names that are blocked from triggering the sound when blacklist is enabled."
        );

        if (config.hasChanged()) {
            config.save();
        }
    }
}