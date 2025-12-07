# Chat Sound Mod

A Minecraft 1.8.9 mod that plays sounds when specific trigger words appear in chat.

## Features

- ✨ Play custom sounds when trigger words appear in chat
- 🔊 Volume control that bypasses Minecraft's audio settings
- 🎲 Random sound selection from a customizable pool
- 📊 Statistics tracking for triggers and players
- 👥 Whitelist/blacklist player filtering
- 🎵 Support for custom sound files
- ⚙️ Easy in-game configuration

## Installation

1. Install Minecraft Forge for version 1.8.9
2. Download the Chat Sound Mod `.jar` file
3. Place the `.jar` file in your `mods` folder
4. Launch Minecraft with the Forge profile

## Quick Start

1. Join a server or world
2. Type `/clckrm` to see current settings
3. Set trigger words: `/clckrm Trg word1,word2`
4. Adjust volume: `/clckrm Vol 1.0`
5. When someone types a trigger word in chat, you'll hear a sound!

## Commands

All commands use `/clckrm` as the base command.

### Basic Commands

- `/clckrm` - Display current settings
- `/clckrm h` or `/clckrm help` - Show help menu

### Trigger Configuration

- `/clckrm Trg <trigger1,trigger2,...>` - Set trigger words (comma-separated)

### Sound Settings

- `/clckrm Vol <0.0-2.0>` - Set sound volume (bypasses Minecraft volume)
- `/clckrm Select <sound>` - Select which sound to play
- `/clckrm Random <on|off>` - Toggle random sound selection
- `/clckrm Pool <sound1,sound2,...>` - Set pool of sounds for randomization
- `/clckrm Preview <sound>` - Test/preview a sound
- `/clckrm List` - List all available sounds

### Filter Settings

- `/clckrm Self <on|off>` - Toggle ignoring your own messages

### Statistics

- `/clckrm Stats <on|off>` - Enable/disable statistics tracking
- `/clckrm Stats word` - Show trigger word statistics
- `/clckrm Stats pl [page]` - Show player statistics (paginated)
- `/clckrm Stats clear` - Clear all statistics

### Whitelist

- `/clckrm WL on|off` - Enable/disable whitelist
- `/clckrm WL add <player>` - Add player to whitelist
- `/clckrm WL remove <player>` - Remove player from whitelist
- `/clckrm WL list` - Show whitelisted players

### Blacklist

- `/clckrm BL on|off` - Enable/disable blacklist
- `/clckrm BL add <player>` - Add player to blacklist
- `/clckrm BL remove <player>` - Remove player from blacklist
- `/clckrm BL list` - Show blacklisted players

## Custom Sounds

Place `.wav`, `.ogg`, or `.mp3` files in: `config/chatsoundmod/sounds/`

Built-in sounds: `click`, `click1`, `click2`, `click3`

**Compatibility Note:** Use `.wav` format for best compatibility. While `.ogg` and `.mp3` are supported, they probably won't work in general. It is **recommended to convert** `.mp3` or `.ogg` files to `.wav` format to ensure reliable playback.

## Configuration File

Settings are automatically saved to: `config/chatsoundmod.cfg`

You can manually edit this file, but it's recommended to use the in-game commands for easier configuration.

## Use Cases

- **Party/Guild Chat**: Get notified when your party members need you
- **Mention Alerts**: Set your username as a trigger to never miss when someone mentions you
- **Keyword Monitoring**: Track important words like "raid", "boss", "event"
- **Trading**: Get alerted when someone says "buying" or "selling"
- **Custom Notifications**: Create your own notification system with custom sounds

## Tips

- Use short, unique trigger words to avoid false positives
- Set volume between 0.5-1.5 for comfortable listening
- Enable statistics to see which triggers are most common
- Use the blacklist to ignore spam bots or annoying players
- Test sounds with `/clckrm Preview` before using them

## Troubleshooting

**Sound not playing?**
- Check if triggers are set correctly: `/clckrm`
- Verify the mod is enabled (it is by default)
- Make sure your volume isn't set to 0 (system volume and make sure /clckrm vol isnt at 0)
- Try using `/clckrm Preview click` to test

**Custom sound not working?**
- Ensure the file is in `.wav` format
- Check the file is in `config/chatsoundmod/sounds/`
- Verify the filename doesn't have spaces or special characters
- Use `/clckrm List` to see if it's detected

**Triggers not matching?**
- Triggers are case-insensitive
- Check for typos in your trigger list

## Coming Soon
- If you want features added that I havent thought of join the [discord!](https://discord.gg/3F7GkEwnPq)
- My plan is for full customization or almost/near full customization!
- Multiple profiles.
- Server profiles.
- Per-sound volume.
- GUI (When I learn)


## Changelog
[Full changelog here.](https://github.com/NamesViolet/Minecraft-Clicker-Mod/blob/main/Changelog.MD)

For bug reports join the [discord.](https://discord.gg/3F7GkEwnPq)


