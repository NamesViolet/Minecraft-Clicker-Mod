package com.ClickerMod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatSoundCommand extends CommandBase {

    private static final String COLOR = "\u00A7b";
    private static final String RESET = "\u00A7r";

    @Override
    public String getCommandName() {
        return "clckrm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/clckrm | /clckrm h for help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            String triggers = ChatSoundMod.triggerMessage == null ? "" : ChatSoundMod.triggerMessage;
            String wlState = ChatSoundMod.whitelistEnabled ? "ON" : "OFF";
            String blState = ChatSoundMod.blacklistEnabled ? "ON" : "OFF";
            String selfState = ChatSoundMod.ignoreSelf ? "ON" : "OFF";
            String statsState = ChatSoundMod.statsManager.isTrackingEnabled() ? "ON" : "OFF";
            String randomState = ChatSoundMod.soundManager.isRandomizeSounds() ? "ON" : "OFF";

            sender.addChatMessage(new ChatComponentText(COLOR + "Triggers: " + (triggers.isEmpty() ? "<none>" : triggers) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Volume: " + String.format("%.2f", ChatSoundMod.soundVolume) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Selected Sound: " + ChatSoundMod.soundManager.getSelectedSound() + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Randomize: " + randomState + RESET));
            if (ChatSoundMod.soundManager.isRandomizeSounds()) {
                sender.addChatMessage(new ChatComponentText(COLOR + "Sound Pool: " + ChatSoundMod.soundManager.getSoundPool() + RESET));
            }
            sender.addChatMessage(new ChatComponentText(COLOR + "IgnoreSelf: " + selfState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Track Stats: " + statsState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Whitelist: " + wlState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Blacklist: " + blState + RESET));
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Do /clckrm h for help" + RESET));
            return;
        }

        if ("help".equalsIgnoreCase(args[0]) || "h".equalsIgnoreCase(args[0])) {
            String triggers = ChatSoundMod.triggerMessage == null ? "" : ChatSoundMod.triggerMessage;
            String selfState = ChatSoundMod.ignoreSelf ? "ON (your messages are ignored)" : "OFF (your messages can trigger)";
            String statsState = ChatSoundMod.statsManager.isTrackingEnabled() ? "ON (statistics are being tracked)" : "OFF (statistics are not tracked)";
            String randomState = ChatSoundMod.soundManager.isRandomizeSounds() ? "ON (random sounds enabled)" : "OFF (using selected sound)";

            sender.addChatMessage(new ChatComponentText(COLOR + "Current triggers: " + (triggers.isEmpty() ? "<none>" : triggers) + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Volume: " + String.format("%.2f", ChatSoundMod.soundVolume) + " (bypasses game volume)" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Selected Sound: " + ChatSoundMod.soundManager.getSelectedSound() + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Randomize: " + randomState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Custom Sounds Directory: " + ChatSoundMod.soundManager.getCustomSoundsDir().getAbsolutePath() + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "IgnoreSelf: " + selfState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Track Stats: " + statsState + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Commands:" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Trg <trigger1,trigger2,...> - set trigger words" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Vol <0.0-2.0> - set sound volume" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Select <sound> - select which sound to play" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Random <on|off> - toggle sound randomization" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Pool <sound1,sound2,...> - set random sound pool" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Preview <sound> - preview a sound effect" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm List - list all available sounds" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Self <on|off> - ignore or include your own messages" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "/clckrm Stats <on|off|word|pl|clear> - manage statistics" + RESET));
            sender.addChatMessage(new ChatComponentText(COLOR + "Do /clckrm WL h or /clckrm WL help for whitelist/blacklist settings" + RESET));
            return;
        }

        // Select sound command
        if ("select".equalsIgnoreCase(args[0]) || "sel".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Select <sound_name>" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Current: " + ChatSoundMod.soundManager.getSelectedSound() + RESET));
                return;
            }

            String soundName = args[1].trim();
            if (soundName.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound name cannot be empty." + RESET));
                return;
            }

            if (!ChatSoundMod.soundManager.isSoundAvailable(soundName)) {
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

        // Random command - toggle randomization
        if ("random".equalsIgnoreCase(args[0]) || "rand".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Random <on|off>" + RESET));
                String state = ChatSoundMod.soundManager.isRandomizeSounds() ? "ON" : "OFF";
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Current: " + state + RESET));
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
                        COLOR + "Invalid value. Use: /clckrm Random <on|off>" + RESET));
                return;
            }

            ChatSoundMod.setRandomizeSounds(value.booleanValue());

            if (value.booleanValue()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound randomization ENABLED." + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound pool: " + ChatSoundMod.soundManager.getSoundPool() + RESET));
            } else {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound randomization DISABLED. Using: " + ChatSoundMod.soundManager.getSelectedSound() + RESET));
            }
            return;
        }

        // Pool command - set sound pool for randomization
        if ("pool".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Pool <sound1,sound2,sound3,...>" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Current pool: " + ChatSoundMod.soundManager.getSoundPool() + RESET));
                return;
            }

            String pool = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            if (pool.isEmpty()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound pool cannot be empty." + RESET));
                return;
            }

            ChatSoundMod.setSoundPool(pool);

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Sound pool set to: " + pool + RESET));
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Randomization: " + (ChatSoundMod.soundManager.isRandomizeSounds() ? "ON" : "OFF (use /clckrm Random on)") + RESET));
            return;
        }

        // Preview sound command
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
            
            ChatSoundHandler.playSound(soundName);
            return;
        }

        // List sounds command
        if ("list".equalsIgnoreCase(args[0]) || "ls".equalsIgnoreCase(args[0]) || "sounds".equalsIgnoreCase(args[0])) {
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "=== Available Sounds ===" + RESET));
            
            Set<String> allSounds = ChatSoundMod.soundManager.getAvailableSounds();
            
            if (allSounds.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("  <none>"));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Add .wav, .ogg, or .mp3 files to:" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        "  " + ChatSoundMod.soundManager.getCustomSoundsDir().getAbsolutePath()));
            } else {
                for (String sound : allSounds) {
                    if ("click".equals(sound) || "click1".equals(sound) || 
                        "click2".equals(sound) || "click3".equals(sound)) {
                        sender.addChatMessage(new ChatComponentText("  - " + sound + " (built-in)"));
                    } else {
                        sender.addChatMessage(new ChatComponentText("  - " + sound + " (custom)"));
                    }
                }
            }
            
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Currently selected: " + ChatSoundMod.soundManager.getSelectedSound() + RESET));
            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Randomization: " + (ChatSoundMod.soundManager.isRandomizeSounds() ? "ON" : "OFF") + RESET));
            if (ChatSoundMod.soundManager.isRandomizeSounds()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Sound pool: " + ChatSoundMod.soundManager.getSoundPool() + RESET));
            }
            return;
        }

        // Set triggers command
        if ("trg".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Trg <trigger1,trigger2,...>" + RESET));
                return;
            }

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

        // Set volume command
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

        // Ignore self command
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

            ChatSoundMod.setIgnoreSelf(value.booleanValue());

            if (value.booleanValue()) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Your own chat messages will NOT trigger the sound." + RESET));
            } else {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Your own chat messages WILL trigger the sound." + RESET));
            }
            return;
        }

        // Statistics command
        if ("stats".equalsIgnoreCase(args[0]) || "statistics".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm Stats <on|off|word|pl|clear> [page]" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "  on/off - enable/disable statistics tracking" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "  word - show trigger word statistics" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "  pl [page] - show player statistics" + RESET));
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "  clear - reset all statistics" + RESET));
                return;
            }

            String statType = args[1].toLowerCase();

            if ("on".equals(statType) || "off".equals(statType)) {
                boolean enable = "on".equals(statType);
                ChatSoundMod.setTrackStats(enable);

                if (enable) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Statistics tracking ENABLED. Statistics will be saved every 30 seconds." + RESET));
                } else {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Statistics tracking DISABLED. Existing statistics are preserved." + RESET));
                }
                return;
            }

            if ("word".equals(statType) || "words".equals(statType) || "trigger".equals(statType)) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "=== Trigger Word Statistics ===" + RESET));

                if (!ChatSoundMod.statsManager.isTrackingEnabled()) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Statistics tracking is currently OFF. Use /clckrm stats on to enable." + RESET));
                }

                if (!ChatSoundMod.statsManager.hasTriggerWordStats()) {
                    sender.addChatMessage(new ChatComponentText("No triggers have been activated yet."));
                    return;
                }

                List<Map.Entry<String, Integer>> sortedEntries = ChatSoundMod.statsManager.getSortedTriggerWordStats();

                for (Map.Entry<String, Integer> entry : sortedEntries) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + entry.getKey() + RESET + ": " + entry.getValue() + " times"));
                }

                int total = ChatSoundMod.statsManager.getTotalTriggerWordCount();
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Total triggers: " + total + RESET));
                return;
            }

            if ("pl".equals(statType) || "player".equals(statType) || "players".equals(statType)) {
                int page = 1;
                if (args.length >= 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.addChatMessage(new ChatComponentText(
                                COLOR + "Invalid page number. Using page 1." + RESET));
                        page = 1;
                    }
                }
                
                ChatSoundMod.currentStatsType = "pl";
                ChatSoundMod.currentStatsPage = page;

                sender.addChatMessage(new ChatComponentText(
                        COLOR + "=== Player Trigger Statistics ===" + RESET));

                if (!ChatSoundMod.statsManager.isTrackingEnabled()) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Statistics tracking is currently OFF. Use /clckrm stats on to enable." + RESET));
                }

                if (!ChatSoundMod.statsManager.hasPlayerStats()) {
                    sender.addChatMessage(new ChatComponentText("No players have triggered sounds yet."));
                    return;
                }

                List<Map.Entry<String, Integer>> sortedEntries = ChatSoundMod.statsManager.getSortedPlayerStats();

                int totalEntries = sortedEntries.size();
                int pageSize = 10;
                int totalPages = (int) Math.ceil((double) totalEntries / pageSize);
                
                if (page < 1) page = 1;
                if (page > totalPages) page = totalPages;
                
                int startIndex = (page - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalEntries);

                for (int i = startIndex; i < endIndex; i++) {
                    Map.Entry<String, Integer> entry = sortedEntries.get(i);
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + (i + 1) + ". " + entry.getKey() + RESET + ": " + entry.getValue() + " times"));
                }

                int total = ChatSoundMod.statsManager.getTotalPlayerTriggerCount();
                
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Page " + page + "/" + totalPages + " | Total: " + total + " triggers" + RESET));
                
                if (page < totalPages) {
                    sender.addChatMessage(new ChatComponentText(
                            COLOR + "Use /clckrm stats pl " + (page + 1) + " for next page" + RESET));
                }
                return;
            }

            if ("clear".equals(statType) || "reset".equals(statType)) {
                ChatSoundMod.statsManager.clear();
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "All statistics have been cleared." + RESET));
                return;
            }

            sender.addChatMessage(new ChatComponentText(
                    COLOR + "Usage: /clckrm Stats <on|off|word|pl|clear> [page]" + RESET));
            return;
        }

        // Whitelist management
        if ("wl".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Do /clckrm WL h or /clckrm WL help for usage" + RESET));
                return;
            }

            String action = args[1].toLowerCase();

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

            if ("list".equals(action) || "l".equals(action)) {
                String wlRaw = ChatSoundMod.whitelistPlayers;
                String wlList = (wlRaw == null || wlRaw.trim().isEmpty()) ? "<none>" : wlRaw;
                String wlStateShort = ChatSoundMod.whitelistEnabled ? "ON" : "OFF";

                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Whitelist (" + wlStateShort + "): " + wlList + RESET));
                return;
            }

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

                    // Remove from blacklist if present
                    String blRaw = ChatSoundMod.blacklistPlayers == null ? "" : ChatSoundMod.blacklistPlayers;
                    if (!blRaw.trim().isEmpty()) {
                        String[] blParts = blRaw.split(",");
                        StringBuilder blSb = new StringBuilder();
                        boolean blFound = false;
                        for (String part : blParts) {
                            String name = part.trim();
                            if (name.isEmpty()) {
                                continue;
                            }
                            if (name.equalsIgnoreCase(player)) {
                                blFound = true;
                                continue;
                            }
                            if (blSb.length() > 0) {
                                blSb.append(",");
                            }
                            blSb.append(name);
                        }
                        if (blFound) {
                            ChatSoundMod.setBlacklistPlayers(blSb.toString());
                        }
                    }

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

        // Blacklist management
        if ("bl".equalsIgnoreCase(args[0]) || "blacklist".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Usage: /clckrm BL <on|off|add|remove> [player]" + RESET));
                return;
            }

            String action = args[1].toLowerCase();

            if ("list".equals(action) || "l".equals(action)) {
                String blRaw = ChatSoundMod.blacklistPlayers;
                String blList = (blRaw == null || blRaw.trim().isEmpty()) ? "<none>" : blRaw;
                String blStateShort = ChatSoundMod.blacklistEnabled ? "ON" : "OFF";

                sender.addChatMessage(new ChatComponentText(
                        COLOR + "Blacklist (" + blStateShort + "): " + blList + RESET));
                return;
            }

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

        // Unknown command
        sender.addChatMessage(new ChatComponentText(
                COLOR + "Usage: " + getCommandUsage(sender) + RESET));
    }
}