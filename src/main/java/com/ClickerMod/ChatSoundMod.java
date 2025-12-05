package com.ClickerMod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = ChatSoundMod.MODID,
     name = ChatSoundMod.NAME,
     version = ChatSoundMod.VERSION,
     clientSideOnly = true,
     acceptedMinecraftVersions = "[1.8.9]")
public class ChatSoundMod {

    public static final String MODID = "chatsoundmod";
    public static final String NAME = "Chat Sound Mod";
    public static final String VERSION = "1.0";

    // Configuration variables
    public static boolean enabled = true;
    public static String triggerMessage = "~click~";
    public static float soundVolume = 1.0F;
    public static boolean ignoreSelf = false;
    public static boolean whitelistEnabled = false;
    public static String whitelistPlayers = "";
    public static boolean blacklistEnabled = false;
    public static String blacklistPlayers = "";
    public static String selectedSound = "click";
    
    public static File customSoundsDir;

    private static ChatSoundMod INSTANCE;
    private Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        // Load configuration file
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        
        // Create custom sounds directory
        customSoundsDir = new File(event.getModConfigurationDirectory(), "chatsoundmod/sounds");
        if (!customSoundsDir.exists()) {
            customSoundsDir.mkdirs();
        }
        
        syncConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register event handler
        MinecraftForge.EVENT_BUS.register(new ChatSoundHandler());

        // Register command
        ClientCommandHandler.instance.registerCommand(new ChatSoundCommand());
    }

    // Save trigger message to config
    public static void setTriggerMessage(String value) {
        triggerMessage = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "triggerMessage", "~click~").set(triggerMessage);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    // Save sound volume to config
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

    // Save selected sound to config
    public static void setSelectedSound(String value) {
        selectedSound = value != null ? value : "click";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "selectedSound", "click").set(selectedSound);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    // Save ignore self setting to config
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

    // Save whitelist enabled state to config
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

    // Save whitelist players to config
    public static void setWhitelistPlayers(String value) {
        whitelistPlayers = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "whitelistPlayers", "").set(whitelistPlayers);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    // Save blacklist enabled state to config
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

    // Save blacklist players to config
    public static void setBlacklistPlayers(String value) {
        blacklistPlayers = value != null ? value : "";

        if (INSTANCE != null && INSTANCE.config != null) {
            String category = "general";
            INSTANCE.config.get(category, "blacklistPlayers", "").set(blacklistPlayers);
            if (INSTANCE.config.hasChanged()) {
                INSTANCE.config.save();
            }
        }
    }

    // Load configuration from file
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

        selectedSound = config.getString(
                "selectedSound",
                category,
                "click",
                "Name of the sound file to play (without extension). Looks for this file in the mod's sounds or in config/chatsoundmod/sounds/"
        );

        ignoreSelf = config.getBoolean(
                "ignoreSelf",
                category,
                false,
                "If true, your own chat messages will NOT trigger the sound; only other players' messages will."
        );

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