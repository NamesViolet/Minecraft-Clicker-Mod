package com.ClickerMod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;

public class ChatSoundCommand extends CommandBase {

    // Chat color codes
    private static final String COLOR = "\u00A7b";
    private static final String RESET = "\u00A7r";

    @Override
    public String getCommandName() {
        return "clckrm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/clckrm Trg <triggers> | /clckrm Vol <0.0-2.0> | /clckrm Select <sound> | /clckrm Preview <sound> | /clckrm List | /clckrm Self <on|off> | /clckrm WL/BL <options>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        // Show status when no arguments provided
        if (args.length == 0) {
            String triggers = ChatSoundMod.triggerMessage == null ? "" : ChatSoundMod.triggerMessage;
            String wlState = ChatSoundMod.whitelistEnabled ? "ON" : "OFF";
            String blState = ChatSoundMod.blacklistEnabled ? "ON" : "OFF";
            String selfState = ChatSoundMod.ignoreSelf ? "ON" : "OFF";

            sender.addChatMessage(new ChatComponentText(COLOR + "Triggers: " + (triggers.isEmpty() ? "<none>" : triggers) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Volume: " + String.format("%.2f", ChatSoundMod.soundVolume) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Selected Sound: " + ChatSoundMod.selectedSound + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "IgnoreSelf: " + selfState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Whitelist: " + wlState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Blacklist: " + blState + RESET));
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Do /clckrm h for help" + RESET));
            return;
        }

        // Help command - show detailed usage information
        if ("help".equalsIgnoreCase(args[0]) || "h".equalsIgnoreCase(args[0])) {
            String triggers = ChatSoundMod.triggerMessage == null ? "" : ChatSoundMod.triggerMessage;
            String selfState = ChatSoundMod.ignoreSelf ? "ON (your messages are ignored)" : "OFF (your messages can trigger)";
            String wlState = ChatSoundMod.whitelistEnabled ? "ON (only whitelisted players can trigger)" : "OFF (any player can trigger)";
            String wlList = (ChatSoundMod.whitelistPlayers == null || ChatSoundMod.whitelistPlayers.trim().isEmpty())
                    ? "<none>"
                    : ChatSoundMod.whitelistPlayers;

            sender.addChatMessage(new ChatComponentText(COLOR + "Current triggers: " + (triggers.isEmpty() ? "<none>" : triggers) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Volume: " + String.format("%.2f", ChatSoundMod.soundVolume) + " (bypasses game volume)" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Selected Sound: " + ChatSoundMod.selectedSound + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Custom Sounds Directory: " + ChatSoundMod.customSoundsDir.getAbsolutePath() + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "IgnoreSelf: " + selfState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Whitelist: " + wlState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Commands:" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Trg <trigger1,trigger2,...> - set trigger words" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Vol <0.0-2.0> - set sound volume (independent of game volume)" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Select <sound> - select which sound to play" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Preview <sound> - preview a sound effect" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm List - list all available sounds" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Self <on|off> - ignore or include your own messages" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Do /clckrm WL h or /clckrm WL help for whitelist/blacklist settings" + RESET));
            return;
        }

        // Select sound command - choose which sound file to play
        if ("select".equalsIgnoreCase(args[0]) || "sel".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Select <sound_name>" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Current: " + ChatSoundMod.selectedSound + RESET));
                return;
            }

            String soundName = args[1].trim();
            if (soundName.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound name cannot be empty." + RESET));
                return;
            }

            // Check if sound exists in custom directory
            java.io.File customWav = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".wav");
            java.io.File customOgg = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".ogg");
            java.io.File customMp3 = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".mp3");
            
            boolean existsCustom = customWav.exists() || customOgg.exists() || customMp3.exists();
            
            // Check if sound exists in built-in resources
            boolean existsBuiltIn = false;
            try {
                java.io.InputStream wavStream = getClass().getResourceAsStream(
                    "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".wav");
                java.io.InputStream oggStream = getClass().getResourceAsStream(
                    "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".ogg");
                    
                existsBuiltIn = (wavStream != null || oggStream != null);
                
                if (wavStream != null) wavStream.close();
                if (oggStream != null) oggStream.close();
            } catch (Exception e) {
            }

            if (!existsCustom && !existsBuiltIn) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound not found: " + soundName + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Use /clckrm List to see available sounds" + RESET));
                return;
            }

            ChatSoundMod.setSelectedSound(soundName);
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Selected sound: " + soundName + RESET));
            return;
        }

        // Preview sound command - test play a sound effect
        if ("preview".equalsIgnoreCase(args[0]) || "test".equalsIgnoreCase(args[0]) || "play".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Preview <sound_name>" + RESET));
                return;
            }

            String soundName = args[1].trim();
            if (soundName.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound name cannot be empty." + RESET));
                return;
            }

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Playing preview: " + soundName + RESET));
            
            com.ClickerMod.ChatSoundHandler.playSound(soundName);
            return;
        }

        // List sounds command - show all available sound files
        if ("list".equalsIgnoreCase(args[0]) || "ls".equalsIgnoreCase(args[0]) || "sounds".equalsIgnoreCase(args[0])) {
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "=== Available Sounds ===" + RESET));
            
            // Show built-in sounds
            sender.addChatMessage(new ChatComponentText(COLOR + "Built-in sounds:" + RESET));
            sender.addChatMessage(new ChatComponentText("  - click"));
            
            // Show custom sounds from directory
            sender.addChatMessage(new ChatComponentText(COLOR + "Custom sounds:" + RESET));
            java.io.File[] files = ChatSoundMod.customSoundsDir.listFiles();
            boolean foundCustom = false;
            
            if (files != null && files.length > 0) {
                for (java.io.File file : files) {
                    if (file.isFile()) {
                        String name = file.getName();
                        if (name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3")) {
                            String soundName = name.substring(0, name.lastIndexOf('.'));
                            sender.addChatMessage(new ChatComponentText("  - " + soundName + " (" + name + ")"));
                            foundCustom = true;
                        }
                    }
                }
            }
            
            if (!foundCustom) {
                sender.addChatMessage(new ChatComponentText("  <none>"));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Add .wav, .ogg, or .mp3 files to:" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        "  " + ChatSoundMod.customSoundsDir.getAbsolutePath()));
            }
            
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Currently selected: " + ChatSoundMod.selectedSound + RESET));
            return;
        }

        // Set triggers command - configure trigger words/phrases
        if ("trg".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Trg <trigger1,trigger2,...>" + RESET));
                return;
            }

            // Join all arguments to preserve spaces in triggers
            String triggers = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            if (triggers.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Trigger list cannot be empty." + RESET));
                return;
            }

            ChatSoundMod.setTriggerMessage(triggers);

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Triggers set to: " + triggers + RESET));
            return;
        }

        // Set volume command - adjust sound volume level
        if ("vol".equalsIgnoreCase(args[0]) || "volume".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Vol <0.0-2.0>" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Current volume: " + String.format("%.2f", ChatSoundMod.soundVolume) + RESET));
                return;
            }

            float volume;
            try {
                volume = Float.parseFloat(args[1]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Invalid volume. Use a number between 0.0 and 2.0" + RESET));
                return;
            }

            if (volume < 0.0F || volume > 2.0F) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Volume must be between 0.0 and 2.0" + RESET));
                return;
            }

            ChatSoundMod.setSoundVolume(volume);

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Sound volume set to: " + String.format("%.2f", ChatSoundMod.soundVolume) + RESET));
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "(This volume bypasses Minecraft's in-game volume settings)" + RESET));
            return;
        }

        // Ignore self command - toggle whether own messages trigger sound
        if ("self".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Self <on|off>" + RESET));
                return;
            }

            String mode = args[1].toLowerCase();
            Boolean value = null;
            if ("on".equals(mode) || "true".equals(mode)) {
                value = Boolean.TRUE;
            } else if ("off".equals(mode) || "false".equals(mode)) {
                value = Boolean.FALSE;
            }

            if (value == null) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Invalid value. Use: /clckrm Self <on|off>" + RESET));
                return;
            }

            ChatSoundMod.setIgnoreSelf(value);

            if (value) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Your own chat messages will NOT trigger the sound." + RESET));
            } else {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Your own chat messages WILL trigger the sound." + RESET));
            }
            return;
        }

        // Whitelist management - control which players can trigger sounds
        if ("wl".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Do /clckrm WL h or /clckrm WL help for usage" + RESET));
                return;
            }

            String action = args[1].toLowerCase();

            // Show whitelist/blacklist help
            if ("h".equals(action) || "help".equals(action)) {
                sender.addChatMessage(new ChatComponentText(COLOR + "Whitelist settings:" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm WL on|off - enable/disable whitelist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm WL add <player> - add player to whitelist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm WL remove <player> - remove player from whitelist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm WL list|l - list whitelisted players" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "Blacklist settings:" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm BL on|off - enable/disable blacklist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm BL add <player> - add player to blacklist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm BL remove <player> - remove player from blacklist" + RESET));
                sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm BL list|l - list blacklisted players" + RESET));
                return;
            }

            // List whitelisted players
            if ("list".equals(action) || "l".equals(action)) {
                String wlRaw = ChatSoundMod.whitelistPlayers;
                String wlList = (wlRaw == null || wlRaw.trim().isEmpty()) ? "<none>" : wlRaw;
                String wlStateShort = ChatSoundMod.whitelistEnabled ? "ON" : "OFF";

                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Whitelist (" + wlStateShort + "): " + wlList + RESET));
                return;
            }

            // Enable or disable whitelist
            if ("on".equals(action) || "off".equals(action)) {
                boolean enable = "on".equals(action);
                ChatSoundMod.setWhitelistEnabled(enable);

                if (enable) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Whitelist ENABLED. Only whitelisted players can trigger the sound." + RESET));
                } else {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Whitelist DISABLED. Any player can trigger the sound." + RESET));
                }
                return;
            }

            // Add or remove player from whitelist
            if ("add".equals(action) || "remove".equals(action)) {
                if (args.length < 3) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Usage: /clckrm WL " + action + " <player>" + RESET));
                    return;
                }

                String player = args[2].trim();
                if (player.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Player name cannot be empty." + RESET));
                    return;
                }

                String current = ChatSoundMod.whitelistPlayers == null ? "" : ChatSoundMod.whitelistPlayers;
                String[] parts = current.split(",");
                StringBuilder sb = new StringBuilder();
                boolean found = false;

                // Rebuild list, excluding player if removing
                for (String part : parts) {
                    String name = part.trim();
                    if (name.isEmpty()) {
                        continue;
                    }

                    if (name.equalsIgnoreCase(player)) {
                        found = true;
                        if ("remove".equals(action)) {
                            continue;
                        }
                    }

                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(name);
                }

                if ("add".equals(action)) {
                    if (!found) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(player);
                    }
                    ChatSoundMod.setWhitelistPlayers(sb.toString());
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Added to whitelist: " + player + " | Now: " + ChatSoundMod.whitelistPlayers + RESET));
                    return;
                }

                ChatSoundMod.setWhitelistPlayers(sb.toString());
                if (found) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Removed from whitelist: " + player + " | Now: " + ChatSoundMod.whitelistPlayers + RESET));
                } else {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Player was not in whitelist: " + player + RESET));
                }
                return;
            }

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Do /clckrm WL h or /clckrm WL help for usage" + RESET));
            return;
        }

        // Blacklist management - control which players cannot trigger sounds
        if ("bl".equalsIgnoreCase(args[0]) || "blacklist".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm BL <on|off|add|remove> [player]" + RESET));
                return;
            }

            String action = args[1].toLowerCase();

            // List blacklisted players
            if ("list".equals(action) || "l".equals(action)) {
                String blRaw = ChatSoundMod.blacklistPlayers;
                String blList = (blRaw == null || blRaw.trim().isEmpty()) ? "<none>" : blRaw;
                String blStateShort = ChatSoundMod.blacklistEnabled ? "ON" : "OFF";

                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Blacklist (" + blStateShort + "): " + blList + RESET));
                return;
            }

            // Enable or disable blacklist
            if ("on".equals(action) || "off".equals(action)) {
                boolean enable = "on".equals(action);
                ChatSoundMod.setBlacklistEnabled(enable);

                if (enable) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Blacklist ENABLED. Blacklisted players cannot trigger the sound." + RESET));
                } else {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Blacklist DISABLED. Any non-whitelist rules apply normally." + RESET));
                }
                return;
            }

            // Add or remove player from blacklist
            if ("add".equals(action) || "remove".equals(action)) {
                if (args.length < 3) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Usage: /clckrm BL " + action + " <player>" + RESET));
                    return;
                }

                String player = args[2].trim();
                if (player.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Player name cannot be empty." + RESET));
                    return;
                }

                String current = ChatSoundMod.blacklistPlayers == null ? "" : ChatSoundMod.blacklistPlayers;
                String[] parts = current.split(",");
                StringBuilder sb = new StringBuilder();
                boolean found = false;

                // Rebuild list, excluding player if removing
                for (String part : parts) {
                    String name = part.trim();
                    if (name.isEmpty()) {
                        continue;
                    }

                    if (name.equalsIgnoreCase(player)) {
                        found = true;
                        if ("remove".equals(action)) {
                            continue;
                        }
                    }

                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(name);
                }

                if ("add".equals(action)) {
                    if (!found) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(player);
                    }
                    ChatSoundMod.setBlacklistPlayers(sb.toString());

                    // Remove from whitelist if present
                    String wlRaw = ChatSoundMod.whitelistPlayers == null ? "" : ChatSoundMod.whitelistPlayers;
                    if (!wlRaw.trim().isEmpty()) {
                        String[] wlParts = wlRaw.split(",");
                        StringBuilder wlSb = new StringBuilder();
                        boolean wlFound = false;
                        for (String part : wlParts) {
                            String name = part.trim();
                            if (name.isEmpty()) {
                                continue;
                            }
                            if (name.equalsIgnoreCase(player)) {
                                wlFound = true;
                                continue;
                            }
                            if (wlSb.length() > 0) {
                                wlSb.append(",");
                            }
                            wlSb.append(name);
                        }
                        if (wlFound) {
                            ChatSoundMod.setWhitelistPlayers(wlSb.toString());
                        }
                    }

                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Added to blacklist: " + player + " | Now: " + ChatSoundMod.blacklistPlayers + RESET));
                    return;
                }

                ChatSoundMod.setBlacklistPlayers(sb.toString());
                if (found) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Removed from blacklist: " + player + " | Now: " + ChatSoundMod.blacklistPlayers + RESET));
                } else {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Player was not in blacklist: " + player + RESET));
                }
                return;
            }

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Usage: /clckrm BL <on|off|add|remove> [player]" + RESET));
            return;
        }

        // Unknown command - show usage
        sender.addChatMessage(new ChatComponentText(
                COLOR + "Usage: " + getCommandUsage(sender) + RESET));
    }
}