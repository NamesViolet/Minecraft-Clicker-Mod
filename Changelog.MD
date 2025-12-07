# Changelog

## Version 2.1.0

**TL;DR**: The mod has been mostly overhauled with improved performance, more features, and numerous bug fixes. It now runs better and provides more control over sound triggers.

### Added
- **Sound Randomization**: Random sound selection from customizable pool
- **Statistics Tracking**: Track trigger word usage and player trigger counts with persistent storage
- **Manager Classes**: Separated sound and statistics logic into dedicated manager classes
- **Performance Optimizations**: HashSet caching for whitelist/blacklist lookups (O(1) instead of O(n))
- **Sound Pool**: Configure multiple sounds for randomization
- **Shutdown Hook**: Gracefully saves statistics on game exit

### Changed
- **Exact Word Matching**: Triggers now match complete words only, not substrings (e.g., "click" won't match "clicked")
- **Thread Pool**: Sound playback now uses ExecutorService with fixed thread pool instead of creating new threads
- **Better Error Handling**: Improved cleanup for audio resources

### Commands
- `/clckrm Random <on|off>` - Toggle sound randomization
- `/clckrm Pool <sound1,sound2,...>` - Set random sound pool
- `/clckrm Stats <on|off|word|pl|clear>` - Manage statistics tracking
- `/clckrm List` - Now shows built-in vs custom sounds

### Bug Fixes
- Fixed server messages triggering sounds when whitelist is disabled
- Fixed blacklist and whitelist not working properly
- Fixed self ignore feature not functioning
- Fixed bug where Hypixel guild chat would incorrectly capture guild rank as part of username
- Fixed sender name extraction for various chat formats (brackets, ranks, prefixes)
- Fixed memory leaks from unclosed audio streams
- Fixed whitelist/blacklist mutual exclusion (adding to one now removes from the other)
- Improved trigger parsing to handle edge cases and whitespace

### Technical
- Version bumped: 1.0 → 2.1.0
- Refactored code into SoundManager and StatisticsManager
- Added proper resource cleanup and shutdown handling
