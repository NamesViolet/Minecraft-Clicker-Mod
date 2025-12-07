package com.ClickerMod;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SoundManager {
    
    private Set<String> cachedAvailableSounds = new HashSet<String>();
    private String[] cachedSoundPool = new String[0];
    private Random random = new Random();
    
    private File customSoundsDir;
    private String selectedSound = "click";
    private boolean randomizeSounds = false;
    private String soundPool = "click,click1,click2,click3";
    
    public SoundManager(File customSoundsDir) {
        this.customSoundsDir = customSoundsDir;
        refreshSoundCache();
    }
    
    public void refreshSoundCache() {
        cachedAvailableSounds.clear();
        
        File[] files = customSoundsDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName();
                    if (name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3")) {
                        cachedAvailableSounds.add(name.substring(0, name.lastIndexOf('.')));
                    }
                }
            }
        }
        
        // Add default built-in sounds
        cachedAvailableSounds.add("click");
        cachedAvailableSounds.add("click1");
        cachedAvailableSounds.add("click2");
        cachedAvailableSounds.add("click3");
    }
    
    public boolean isSoundAvailable(String soundName) {
        return cachedAvailableSounds.contains(soundName);
    }
    
    public Set<String> getAvailableSounds() {
        return new HashSet<String>(cachedAvailableSounds);
    }
    
    public File getCustomSoundFile(String soundName) {
        File wav = new File(customSoundsDir, soundName + ".wav");
        if (wav.exists()) return wav;
        
        File ogg = new File(customSoundsDir, soundName + ".ogg");
        if (ogg.exists()) return ogg;
        
        File mp3 = new File(customSoundsDir, soundName + ".mp3");
        if (mp3.exists()) return mp3;
        
        return null;
    }
    
    public String getSoundToPlay() {
        if (!randomizeSounds || cachedSoundPool.length == 0) {
            return selectedSound;
        }
        
        // Pick random sound from pool
        int index = random.nextInt(cachedSoundPool.length);
        return cachedSoundPool[index];
    }
    
    public void setSelectedSound(String sound) {
        this.selectedSound = sound != null ? sound : "click";
    }
    
    public String getSelectedSound() {
        return selectedSound;
    }
    
    public void setRandomizeSounds(boolean randomize) {
        this.randomizeSounds = randomize;
    }
    
    public boolean isRandomizeSounds() {
        return randomizeSounds;
    }
    
    public void setSoundPool(String pool) {
        this.soundPool = pool != null ? pool : "click,click1,click2,click3";
        rebuildSoundPoolCache();
    }
    
    public String getSoundPool() {
        return soundPool;
    }
    
    public void rebuildSoundPoolCache() {
        if (soundPool != null && !soundPool.trim().isEmpty()) {
            String[] split = soundPool.split(",");
            int count = 0;
            for (String s : split) {
                if (!s.trim().isEmpty()) count++;
            }
            cachedSoundPool = new String[count];
            int i = 0;
            for (String s : split) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    cachedSoundPool[i++] = trimmed;
                }
            }
        } else {
            cachedSoundPool = new String[0];
        }
    }
    
    public File getCustomSoundsDir() {
        return customSoundsDir;
    }
}