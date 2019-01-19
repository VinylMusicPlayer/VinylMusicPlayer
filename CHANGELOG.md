# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [0.19.0] - 2019-01-19
### Added
- Add a new smart playlist "Not played lately" playlist
- Add choice of 7 days to settings for dynamic playlists
- Smart playlist decoration
- Finish current music when Sleep Timer stops

### Changed
- Disable clear menu item on NotRecentlyPlayed

### Fixed
- App dies on rotate
- Audio cover fallback list to include "folder.png"

## [0.18.0] - 2018-09-10
### Added
- ReplayGain feature. This is still considered experimental at this point.
Thanks to [@knacky34](https://github.com/knacky34)!
- Add a transparent widget.
- Preference to turn off shuffle mode when selecting new list of songs.
- Select all items in a list.
- Export multiple playlists at once.

### Changed
- Show unknown year consistently everywhere.
- Also look for png album covers in the folder.
- Show "-" instead of "0" when the album year is not available.
- Show "Unknown Artist" when the artist name is unknown.
- Navigation bar button colors for light themes.

### Fixed
- Crash with custom artist images.
- App intro crash.
- Crash for some artist names which contain special characters.
- Loading of very large embedded album art.
- Broken layout for super long artist names.

## [0.17.0] - 2018-05-01
### Added
- Album redesign thanks to Adrian (that's not me! :-)).
- New "Scan" option thanks to [@kabouzeid](https://github.com/kabouzeid).
- Sorting feature thanks to [@soren121](https://github.com/soren21).

## [0.16.5.2] - 2018-04-28
### Changed
- Upgrade to Glide 4.

## [0.16.4.4] - 2018-01-18
### Changed
- Hide the tab bar when only one tab is activated.

## 0.16.4.3 - 2018-01-02
### Added
- Initial version.

[Unreleased]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.19.0...HEAD
[0.18.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.18.0...0.19.0
[0.18.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.17.0...0.18.0
[0.17.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.5.2...0.17.0
[0.16.5.2]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.4.4...0.16.5.2
[0.16.4.4]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.4.3...0.16.4.4
