# Changelog

## [1.4.0] - 2022-08-12
### What's Changed
* Sort orders by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/467
* Fast scroll popup: Show relative date for recent ones by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/494
* When parsing release year from metadata, only consider the first `yyyy` part by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/491
* Fixed Image Flicker on notifyDataSetChanged() by @prathameshmm02 in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/508
* tweak song sorting for Song and Genre tabs by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/506
* Disc number fallback by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/498
* Better menuItem visual queue for delete action by @Octoton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/520
* Set imageText visibility to INVISIBLE from GONE to allow reordering current song in queue by @bertin0 in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/523
* Adding missing red menu item delete in all multiselecion menu by @Octoton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/532
* add darkmode to BaseAppWidget by @newhinton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/522
* Most album covers are square, not rectangle -> show as square on the nav drawer by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/546
* More info in the song's Details dialog by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/561
* Fix Black (OLED) theme to actually be black. by @Sai-P in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/588

### Translation updates
* Update Italian translation by @auanasgheps in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/476
* Brazilian Portuguese translation updated by @DeltaInsight in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/475
* Catalan translation updated by @albertgasset in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/471
* Update italian translation by @auanasgheps in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/477
* Fix italian build error by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/478
* Updated Brazilian Portuguese translation by @DeltaInsight in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/488
* Catalan translation updated by @albertgasset in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/496
* Fixed or added German translations by @tschlegeldigos in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/499
* Update Korean translation by @yurical in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/505
* Unified period usage in settings  by @tschlegeldigos in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/510
* English traduction for CA and GB removal by @Octoton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/519
* Fixed some German translations by @tschlegeldigos in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/507
* Update French translation by @GladiusTM in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/538
* Updated Brazilian Portuguese translation by @DeltaInsight in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/601

### Fixes
* Fix NPE on artist name splitting by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/474
* Fix crash on tapping/dragging the very fist item of the orderable playlist by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/486
* Prevent repeating last track when gapless is enabled (fixes #435) by @albertgasset in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/495
* Fix crash if the songs/albums collection is empty by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/497
* Fixes https://github.com/AdrienPoupa/VinylMusicPlayer/issues/431 by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/550
* Fix Android Auto regression  by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/551
* Fix crash on restoring queue after song removal by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/547
* Resolve IMMUTABLE crash on android 12 following a change in android specification by @Octoton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/549
* Fix crash launching from Google Assistant by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/567
* Queue restore crash by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/570
* Use song title to stabilize the sorting if disc+track are equals or missing by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/583
* Fix data race by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/610

### Engineering
* Lint by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/426
* Flush the Discog task queue on stop by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/470
* Playing hide and seek with jaudiotagger - contain the VerifyError with FLAC by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/487
* Add missing null check by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/481
* Lint by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/473
* Align to recent strings rename by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/493
* Spitting playingQueue from musicservice by @Octoton in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/509
* Drop ComparatorUtil.compareLongInts... by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/558
* Tweak text transparency to improve readability by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/559
* Tweak navbar album cover text transparency by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/560
* Stick to API 29, avoid regression in song deletion by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/552
* Upgrade gradle by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/578
* Upgrade to new CircleCI image by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/585
* Update build to large resource class in config.yml by @soncaokim in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/612

### New Contributors
* @auanasgheps made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/476
* @DeltaInsight made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/475
* @tschlegeldigos made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/499
* @yurical made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/505
* @prathameshmm02 made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/508
* @bertin0 made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/523
* @newhinton made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/522
* @GladiusTM made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/538
* @Sai-P made their first contribution in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/588

## [1.3.0] - 2021-06-24
### Added
- Whitelist
- Switch theme based on OS setting
- Ability to fast forward miss in fragment flat and rewind by holding next and previous song buttons
### Fixed
- "Shuffle All" button will now start a new playback
- Crash when reading MP3 tags on Nougat

## [1.2.0] - 2021-04-25
### Added
- Add "delete from device" choice menu in playlist view (both dumb and smart ones)
- ID3v1 tag support
- Added fast forward and rewind by holding next and previous song buttons
### Fixed
- Optimized library rescan and startup
- Fixed Replay Gain not applied if gapless is enabled 
- Fixed sleep timer not applied if gapless enabled
- Fixed Notification progress slider
### Changed
- Updated Android Auto UI
- Skip to the next track when the currently playing one has an error
- Smart Playlists: lots of improvements

## [1.1.2] - 2021-03-11
### Added
- "Delete from device" choice menu in playlist view (both dumb and smart ones)
### Fixed
- Auto queue stuck
- Finish Last Song checkbox position on sleep timer
### Changed
- When a song is added to queue, discard existing position (i.e. move the existing song in queue)

## [1.1.1] - 2021-02-23
### Added
- Merged album with same name/same artist
### Fixed
- Android Auto mode
- Changelog popup not showing
### Changed
- Reduced overhead on the main thread during scan

## [1.1.0] - 2021-02-12
### Added
- Support for multiple artists per track
- Extract album artist from ID3 tags and use that for album grouping/sorting
- Given a song with album artist A and artist "A & B", only "A & B" is shown on artist tab.
- Show album artist on top of album detail page if there is one, else fallback to first song's artist
- Multi-disc track sorting
- Support ReplayGain in MP4 files
### Fixed
- Unknown artist is shown as empty on artist tab
- Performance optimization
- Remove songs from detail activities after they are deleted
- Fix sort order
- Fix a crash after extended sleep
- Fix crash due to iterating on a modified collection
- Fix crash when a song is removed from queue

## [1.0.0] - 2020-10-15
### Added
- Library handling with a local database, circumventing the MediaStore
- Setting to show/hide track number
### Fixed
- White line around app icon
- Playlist bug on Android 10

## [0.24.1] - 2020-10-01
### Fixed
- Filename not showing for songs without tags

## [0.24.0] - 2020-09-24
### Fixed
- Compatible with Android 11
- Fix ringtone sharing
- Detect and avoid fetching deezer place holder image
- Fix scanning large folders
- Fix the ellipsize bug
- Fix long-pressing on title starting playback
- Fix default album cover is not consistent

### Changed
- Made Vinyl resizable 

## [0.23.1] - 2020-01-19
### Fixed
- Release typo

## [0.23.0] - 2020-01-19
### Changed
- Updated Kotlin, organize dependencies
- Removed dependency on legacy preferences and fragments
- Updated German translation
- Animated playing indicator icon

### Fixed
- Crash while scrolling on artist list
- Playing a folder results in unexpected sorting of all songs from subfolders
- Album tag editor deleting 'artist' tag if 'album artist' is empty
- Playing wrong song when restoring to play queue

## [0.22.1] - 2019-05-14
### Fixed
- Next track not playing when gapless playback is enabled

## [0.22.0] - 2019-05-12
### Added
- Splash screen
- Play queue progress
- Favorite button to notifications
- Highlight current song

### Fixed
- Null exception
- ANR

### Changed
- Allow only one task at a time
- Artist and Album cover not loading (LastFM API replaced by Deezer's)

## [0.21.1] - 2019-04-13
### Fixed
- Songs not playing in the folder view
- Crash during first launch

## [0.21.0] - 2019-04-10
### Added
- Experimental Android Auto support
- Continue playing on song removal

### Fixed
- Songs not playing after a tap on the title
- Transparent widget losing its transparency randomly

### Changed
- Place the not played tracks first in the not recently played list

## [0.20.2] - 2019-02-25
### Fixed
- Titles not showing for Android 6 and below

## [0.20.1] - 2019-02-22
### Fixed
- Bump database version

## [0.20.0] - 2019-02-22
### Added
- Songs and Albums: sort by date added
- Put song title text view into a horizontal scrollview
- Swipe to remove song from playing queue
- SD card write access using SAF API

### Fixed
- Set the top tracks number to 100
- Shorter labels for launcher name
- Fix transparent widget update
- Improve ReplayGain

## [0.19.2] - 2019-01-22
### Fixed
- Crash when tapping on "Library Categories" setting

## [0.19.1] - 2019-01-21
### Fixed
- Introduction crashing on some devices

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

## [0.16.4.3] - 2018-01-02
### Added
- Initial version.

[Unreleased]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.23.1...HEAD
[0.23.1]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.23.0...0.23.1
[0.23.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.22.1...0.23.0
[0.22.1]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.22.0...0.22.1
[0.22.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.21.1...0.22.0
[0.21.1]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.21.0...0.21.1
[0.21.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.20.2...0.21.0
[0.20.2]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.20.1...0.20.2
[0.20.1]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.20.0...0.20.1
[0.20.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.19.2...0.20.0
[0.19.2]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.19.1...0.19.2
[0.19.1]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.19.0...0.19.1
[0.19.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.18.0...0.19.0
[0.18.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.17.0...0.18.0
[0.17.0]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.5.2...0.17.0
[0.16.5.2]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.4.4...0.16.5.2
[0.16.4.4]: https://github.com/AdrienPoupa/VinylMusicPlayer/compare/0.16.4.3...0.16.4.4
