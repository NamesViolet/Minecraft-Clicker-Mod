package com.ClickerMod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatSoundHandler {

    private static final ExecutorService soundExecutor = Executors.newFixedThreadPool(2);

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        if (!ChatSoundMod.enabled) {
            return;
        }

        if (event.type != 0) {
            return;
        }

        if (ChatSoundMod.cachedTriggers == null || ChatSoundMod.cachedTriggers.length == 0) {
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

        String senderName = extractSenderName(message);
        if (senderName == null) {
            return;
        }

        String senderLower = senderName.toLowerCase();

        if (ChatSoundMod.ignoreSelf) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                String playerName = mc.thePlayer.getName();
                if (playerName != null && senderLower.equals(playerName.toLowerCase())) {
                    return;
                }
            }
        }

        if (ChatSoundMod.whitelistEnabled && !ChatSoundMod.cachedWhitelistSet.isEmpty()) {
            if (!ChatSoundMod.cachedWhitelistSet.contains(senderLower)) {
                return;
            }
        }

        if (ChatSoundMod.blacklistEnabled && !ChatSoundMod.cachedBlacklistSet.isEmpty()) {
            if (ChatSoundMod.cachedBlacklistSet.contains(senderLower)) {
                return;
            }
        }

        String lowerMessage = message.toLowerCase();
        boolean matched = false;
        String matchedTrigger = null;

        for (String trigger : ChatSoundMod.cachedTriggers) {
            if (containsWord(lowerMessage, trigger)) {
                matched = true;
                matchedTrigger = trigger;
                break;
            }
        }

        if (matched) {
            playChatSound();
            
            if (matchedTrigger != null) {
                ChatSoundMod.statsManager.recordTriggerWord(matchedTrigger);
            }
            ChatSoundMod.statsManager.recordPlayerTrigger(senderName);
        }
    }

    private boolean containsWord(String message, String word) {
        int index = 0;
        int wordLen = word.length();
        int msgLen = message.length();
        
        while (index <= msgLen - wordLen) {
            int foundIndex = message.indexOf(word, index);
            
            if (foundIndex == -1) {
                return false;
            }
            
            char firstChar = word.charAt(0);
            char lastChar = word.charAt(wordLen - 1);
            
            boolean validStart = foundIndex == 0 || 
                                !isWordCharacter(message.charAt(foundIndex - 1)) || 
                                !isWordCharacter(firstChar);
            
            boolean validEnd = (foundIndex + wordLen == msgLen) || 
                              !isWordCharacter(message.charAt(foundIndex + wordLen)) || 
                              !isWordCharacter(lastChar);
            
            if (validStart && validEnd) {
                return true;
            }
            
            index = foundIndex + 1;
        }
        
        return false;
    }
    
    private boolean isWordCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    private String extractSenderName(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        if (message.charAt(0) == '<') {
            int end = message.indexOf("> ");
            if (end > 1 && end < 17) {
                return message.substring(1, end);
            }
        }

        int colonIndex = message.indexOf(": ");
        if (colonIndex > 0 && colonIndex < 50) {
            String beforeColon = message.substring(0, colonIndex);
            
            String cleaned = beforeColon;
            int openBracket;
            while ((openBracket = cleaned.indexOf('[')) >= 0) {
                int closeBracket = cleaned.indexOf(']', openBracket);
                if (closeBracket > openBracket) {
                    cleaned = (cleaned.substring(0, openBracket) + " " + 
                              cleaned.substring(closeBracket + 1)).trim();
                } else {
                    break;
                }
            }
            
            if (cleaned.length() > 0 && cleaned.length() <= 16 && cleaned.indexOf(' ') == -1) {
                return cleaned;
            }
        }

        int startPos = 0;
        while (startPos < message.length() && message.charAt(startPos) == '[') {
            int closeBracket = message.indexOf(']', startPos);
            if (closeBracket > 0 && closeBracket < message.length() - 1) {
                startPos = closeBracket + 1;
                while (startPos < message.length() && message.charAt(startPos) == ' ') {
                    startPos++;
                }
            } else {
                break;
            }
        }
        
        if (startPos < message.length() && message.charAt(startPos) == '<') {
            int end = message.indexOf("> ", startPos);
            if (end > startPos + 1) {
                return message.substring(startPos + 1, end);
            }
        }

        return null;
    }

    private void playChatSound() {
        String soundToPlay = ChatSoundMod.soundManager.getSoundToPlay();
        playSound(soundToPlay);
    }
    
    public static void playSound(final String soundName) {
        soundExecutor.submit(new Runnable() {
            public void run() {
                Clip clip = null;
                AudioInputStream audioStream = null;
                AudioInputStream decodedStream = null;
                InputStream audioSrc = null;
                
                try {
                    String sourceName = "";
                    
                    File customFile = ChatSoundMod.soundManager.getCustomSoundFile(soundName);
                    
                    if (customFile != null) {
                        audioSrc = new java.io.FileInputStream(customFile);
                        sourceName = "custom: " + customFile.getName();
                    } else {
                        String soundPath = "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".wav";
                        audioSrc = ChatSoundHandler.class.getResourceAsStream(soundPath);
                        
                        if (audioSrc == null) {
                            soundPath = "/assets/" + ChatSoundMod.MODID + "/sounds/" + soundName + ".ogg";
                            audioSrc = ChatSoundHandler.class.getResourceAsStream(soundPath);
                        }
                        
                        if (audioSrc == null) {
                            System.err.println("[ChatSoundMod] Could not find sound: " + soundName);
                            System.err.println("[ChatSoundMod] Searched in: " + ChatSoundMod.soundManager.getCustomSoundsDir().getAbsolutePath());
                            System.err.println("[ChatSoundMod] And in mod resources");
                            return;
                        }
                        
                        sourceName = "built-in: " + soundName;
                    }
                    
                    BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                    
                    audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                    AudioFormat baseFormat = audioStream.getFormat();
                    
                    AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                    );
                    
                    decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
                    
                    DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
                    clip = (Clip) AudioSystem.getLine(info);
                    
                    clip.open(decodedStream);
                    
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
                    
                    final Clip finalClip = clip;
                    final AudioInputStream finalDecodedStream = decodedStream;
                    final AudioInputStream finalBaseStream = audioStream;
                    final InputStream finalAudioSrc = audioSrc;
                    
                    clip.addLineListener(new LineListener() {
                        public void update(LineEvent event) {
                            if (event.getType() == LineEvent.Type.STOP) {
                                finalClip.close();
                                try {
                                    if (finalDecodedStream != null) finalDecodedStream.close();
                                    if (finalBaseStream != null) finalBaseStream.close();
                                    if (finalAudioSrc != null) finalAudioSrc.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    
                    clip.start();
                    
                } catch (UnsupportedAudioFileException e) {
                    System.err.println("[ChatSoundMod] Unsupported audio format for: " + soundName);
                    System.err.println("[ChatSoundMod] Supported formats: WAV, OGG (if codec available), MP3 (if codec available)");
                    System.err.println("[ChatSoundMod] Recommended: Convert to WAV format");
                    cleanup(clip, decodedStream, audioStream, audioSrc);
                } catch (Exception e) {
                    System.err.println("[ChatSoundMod] Error playing sound: " + soundName);
                    e.printStackTrace();
                    cleanup(clip, decodedStream, audioStream, audioSrc);
                }
            }
        });
    }
    
    private static void cleanup(Clip clip, AudioInputStream decodedStream, AudioInputStream audioStream, InputStream audioSrc) {
        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if (decodedStream != null) {
                decodedStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if (audioStream != null) {
                audioStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if (audioSrc != null) {
                audioSrc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void shutdown() {
        if (soundExecutor != null && !soundExecutor.isShutdown()) {
            soundExecutor.shutdown();
        }
    }
}