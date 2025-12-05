package com.ClickerMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;

public class ChatSoundHandler {

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        // Check if mod is enabled
        if (!ChatSoundMod.enabled) {
            return;
        }

        // Only process normal chat messages
        if (event.type != 0) {
            return;
        }

        // Check if triggers are configured
        String rawTriggers = ChatSoundMod.triggerMessage;
        if (rawTriggers == null || rawTriggers.trim().isEmpty()) {
            return;
        }

        IChatComponent component = event.message;
        if (component == null) {
            return;
        }

        String message = component.getUnformattedText();
        if (message == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        
        // Optionally ignore own messages
        if (ChatSoundMod.ignoreSelf && mc.thePlayer != null) {
            String playerName = mc.thePlayer.getName();
            if (playerName != null) {
                String selfPrefix = "<" + playerName + "> ";
                if (message.startsWith(selfPrefix)) {
                    return;
                }
            }
        }

        // Extract sender name from message
        String senderName = extractSenderName(message);

        // Only allow messages from identifiable players
        if (senderName == null) {
            return;
        }

        // Check whitelist if enabled
        if (ChatSoundMod.whitelistEnabled) {
            String wlRaw = ChatSoundMod.whitelistPlayers;
            
            if (wlRaw == null || wlRaw.trim().isEmpty()) {
                return;
            }

            boolean allowed = false;
            String[] wlEntries = wlRaw.split(",");
            for (String entry : wlEntries) {
                String name = entry.trim();
                if (name.isEmpty()) {
                    continue;
                }

                if (senderName.equalsIgnoreCase(name)) {
                    allowed = true;
                    break;
                }
            }

            if (!allowed) {
                return;
            }
        }

        // Check blacklist if enabled
        if (ChatSoundMod.blacklistEnabled) {
            String blRaw = ChatSoundMod.blacklistPlayers;
            
            if (senderName != null && blRaw != null && !blRaw.trim().isEmpty()) {
                String[] blEntries = blRaw.split(",");
                for (String entry : blEntries) {
                    String name = entry.trim();
                    if (name.isEmpty()) {
                        continue;
                    }

                    if (senderName.equalsIgnoreCase(name)) {
                        return;
                    }
                }
            }
        }

        // Check if message contains any triggers
        String lowerMessage = message.toLowerCase();
        boolean matched = false;

        String[] split = rawTriggers.split(",");
        for (String part : split) {
            String trigger = part.trim();
            if (trigger.isEmpty()) {
                continue;
            }

            if (lowerMessage.contains(trigger.toLowerCase())) {
                matched = true;
                break;
            }
        }

        // Play sound if trigger matched
        if (matched) {
            playChatSound();
        }
    }

    // Extract sender username from various chat formats
    private String extractSenderName(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        // Format: <PlayerName> message
        if (message.startsWith("<")) {
            int end = message.indexOf("> ");
            if (end > 1) {
                return message.substring(1, end);
            }
        }

        // Format: PlayerName: message or [Rank] PlayerName: message
        int colonIndex = message.indexOf(": ");
        if (colonIndex > 0) {
            String beforeColon = message.substring(0, colonIndex);
            
            // Check for rank prefix
            int lastBracket = beforeColon.lastIndexOf("] ");
            if (lastBracket > 0 && lastBracket < beforeColon.length() - 2) {
                return beforeColon.substring(lastBracket + 2).trim();
            }
            
            // No rank, check length
            if (beforeColon.length() <= 16) {
                return beforeColon.trim();
            }
        }

        // Format: [Rank] <PlayerName> message
        int bracketEnd = message.indexOf("] ");
        if (bracketEnd > 0 && message.length() > bracketEnd + 2) {
            String afterBracket = message.substring(bracketEnd + 2);
            if (afterBracket.startsWith("<")) {
                int end = afterBracket.indexOf("> ");
                if (end > 1) {
                    return afterBracket.substring(1, end);
                }
            }
        }

        return null;
    }

    // Play the currently selected sound
    private void playChatSound() {
        playSound(ChatSoundMod.selectedSound);
    }
    
    // Play a sound file by name
    public static void playSound(final String soundName) {
        // Run in separate thread to avoid blocking game
        new Thread(() -> {
            Clip clip = null;
            AudioInputStream audioStream = null;
            
            try {
                java.io.InputStream audioSrc = null;
                String sourceName = "";
                
                // Check custom sounds directory
                java.io.File customWav = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".wav");
                java.io.File customOgg = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".ogg");
                java.io.File customMp3 = new java.io.File(ChatSoundMod.customSoundsDir, soundName + ".mp3");
                
                if (customWav.exists()) {
                    audioSrc = new java.io.FileInputStream(customWav);
                    sourceName = "custom: " + customWav.getName();
                } else if (customOgg.exists()) {
                    audioSrc = new java.io.FileInputStream(customOgg);
                    sourceName = "custom: " + customOgg.getName();
                } else if (customMp3.exists()) {
                    audioSrc = new java.io.FileInputStream(customMp3);
                    sourceName = "custom: " + customMp3.getName();
                } else {
                    // Fall back to built-in resources
                    String soundPath = "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".wav";
                    audioSrc = ChatSoundHandler.class.getResourceAsStream(soundPath);
                    
                    if (audioSrc == null) {
                        soundPath = "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".ogg";
                        audioSrc = ChatSoundHandler.class.getResourceAsStream(soundPath);
                    }
                    
                    if (audioSrc == null) {
                        System.err.println("[ChatSoundMod] Could not find sound: " + soundName);
                        System.err.println("[ChatSoundMod] Searched in: " + ChatSoundMod.customSoundsDir.getAbsolutePath());
                        System.err.println("[ChatSoundMod] And in mod resources");
                        return;
                    }
                    
                    sourceName = "built-in: " + soundName;
                }
                
                BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                
                // Get audio stream
                audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                AudioFormat baseFormat = audioStream.getFormat();
                
                // Convert to PCM format
                AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
                );
                
                AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
                
                // Create and open clip
                DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
                clip = (Clip) AudioSystem.getLine(info);
                
                clip.open(decodedStream);
                
                // Set volume
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    
                    float dB;
                    if (ChatSoundMod.soundVolume <= 0.01F) {
                        dB = volume.getMinimum();
                    } else if (ChatSoundMod.soundVolume <= 1.0F) {
                        dB = volume.getMinimum() + (0.0F - volume.getMinimum()) * ChatSoundMod.soundVolume;
                    } else {
                        dB = Math.min(6.0F, (ChatSoundMod.soundVolume - 1.0F) * 6.0F);
                    }
                    
                    dB = Math.max(volume.getMinimum(), Math.min(volume.getMaximum(), dB));
                    volume.setValue(dB);
                }
                
                // Set up cleanup on stop
                final Clip finalClip = clip;
                final AudioInputStream finalStream = decodedStream;
                final AudioInputStream finalBaseStream = audioStream;
                
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        finalClip.close();
                        try {
                            if (finalStream != null) finalStream.close();
                            if (finalBaseStream != null) finalBaseStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                
                // Start playback
                clip.start();
                
            } catch (UnsupportedAudioFileException e) {
                System.err.println("[ChatSoundMod] Unsupported audio format for: " + soundName);
                System.err.println("[ChatSoundMod] Supported formats: WAV, OGG (if codec available), MP3 (if codec available)");
                System.err.println("[ChatSoundMod] Recommended: Convert to WAV format");
            } catch (Exception e) {
                System.err.println("[ChatSoundMod] Error playing sound: " + soundName);
                e.printStackTrace();
                
                // Cleanup on error
                if (clip != null && clip.isOpen()) {
                    clip.close();
                }
                if (audioStream != null) {
                    try {
                        audioStream.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, "ChatSoundMod-Audio").start();
    }
}