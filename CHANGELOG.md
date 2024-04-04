# Changelog

## [1.10.0] - 2024-04-04

### Fixes
* Keep the queue in sync with changes from MediaStore (removal, update) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/969
* Fix regression on discography (album) introduced by #992 (multi-artist-navigation) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/1007
* Fix crash described by #1008 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/1009
* Fix issue #974 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/1010
* Snackbar tweaks by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/991

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.9.0...1.10.0

## [1.9.0] - 2024-03-30

### Features
* Multi artists navigation by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/992
* Improve unknown artist/album/genre/song title display by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/994

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.8.4...1.9.0

## [1.8.4] - 2024-03-23

### Fixes
* Hotfix for regression introduced by PR #989 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/996

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.8.3...1.8.4

## [1.8.3] - 2024-03-23

### Fixes
* Refactor multi select by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/958
* Dont collect the stack trace if the underlying library  cannot read the media file by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/989
* End the DB transaction properly (in case of failure) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/990
* Clean obsolete DB columns by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/993

### Other Changes
* feat(translations): add Dutch translation by @AnonymousWP in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/986
* Apply build pipeline on PR as well by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/987

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.8.2...1.8.3

## [1.8.2] - 2024-03-13

### Fixes
* Monochrome icon by @ByteHamster in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/971
* Fix crash on init with API 19 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/982
* Resolve "Android 8: Unnecessary "Vinyl is running" notification #952" by @AutomaticUpdates in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/978

## New Contributors
* @ByteHamster made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/971
* @AutomaticUpdates made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/978
* @AnonymousWP made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/977

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.8.1...1.8.2

## [1.8.1] - 2024-03-04

### Fixes
* Fix save playlist android9 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/944
* Fix dangling unknown artist by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/956
* Fix crash on select dupe song in playlist by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/950
* Fix stale notification by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/961
* Silence logcat warning about deprecated use of stream type by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/963

### Other Changes

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.8.0...1.8.1

## [1.8.0] - 2024-02-08

### Features
* Opus support by @drizzt in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/896
* History import + Refactor playlist menu by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/922

### Fixes
* Refactor {card|flat} fragments, move common code to base class and fix NPE by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/926
* Avoid NPE (related to https://github.com/VinylMusicPlayer/VinylMusicPlayer/issues/931 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/932
* Allow tag editor to function on API 30+ by @gaycodegal in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/929
* Fix multi genres by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/939
* Fix NPE (on getSongsForGenre) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/933

### Other Changes
* Upgrade Github Actions plugins by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/934

## New Contributors
* @drizzt made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/896

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.7.0...1.8.0

## [1.7.0] - 2024-01-30

### Features
* Add context menu to genre activity by @gaycodegal in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/907
* Not recently played/Last added - group by album by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/752
* add multi-line genre editing by @gaycodegal in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/928 and in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/904

### Fixes
* fixes #853, the system UI notification crash by @ellisonch in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/902
* Fixes #855 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/925
* Blacklist error on android 12 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/924
* Report original filename with error by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/923

## New Contributors
* @ellisonch made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/902

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.6.3...1.7.0

## [1.6.3] - 2023-12-24

### Fixes
* Fix https://github.com/VinylMusicPlayer/VinylMusicPlayer/issues/884 (NPE while the app is in idle/background) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/888

### Other Changes
* Updated German translations by @tschlegeldigos in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/897
* Remove unused strings by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/900
* Tools upgrade by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/887

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.6.2...1.6.3

## [1.6.2] - 2023-12-07

### Fixes
* Fix compatibility with Poweramp by @MageFroh in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/875

### Other Changes
* Upgrade dependencies and gradle by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/867

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.6.1...1.6.2

## [1.6.1] - 2023-11-27

### Fixes
* Avoid copying an audio file to read its tags as much as possible by @MageFroh in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/856
* Oops handler v2 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/860

### Other Changes
* Correct and update russian translation by @developersu in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/861
* Revert non-intended change by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/865
* Oops, disable oops_handler by default by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/868

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.6.0...1.6.1

## [1.6.0] - 2023-11-20

### Features
* Use DynamicsProcessing to apply positive replay gains in a robust way by @MageFroh in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/828
* Add settings to opt-in for crash report by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/857

### Fixes
* Fix NPE by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/843
* Fix race cond - 2 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/842
* Fix equalizer settings being marked unavailable in the settings by @MageFroh in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/840

### Other Changes
* Update russian translation by @developersu in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/850

## New Contributors
* @MageFroh made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/840
* @developersu made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/850

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.6...1.6.0

## [1.5.6] - 2023-10-28

### Fixes
* App context fixes by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/830
* Restore cover art loading for songs in the Folder view by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/832
* Thread safe toast by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/834
* Fix race condition by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/824
* Android auto fixes by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/829
* Clean up Android API compat by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/833
* Further fix for https://github.com/VinylMusicPlayer/VinylMusicPlayer/issues/707 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/838

### Other Changes
* Support SD card by @krebsd and @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/825

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.5...1.5.6

## [1.5.5] - 2023-10-20

### Fixes
* Dont crash if OopsHandler is called from a background (non-UI) thread by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/808
* Fix repeat setting not being restored by @toolstack in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/811
* Attempt to fix https://github.com/VinylMusicPlayer/VinylMusicPlayer/issues/707 by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/821

### Other Changes
* Refrain from rushing out a new release on every new tag by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/820

## New Contributors
* @toolstack made their first contribution in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/811

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.4...1.5.5

## [1.5.4] - 2023-10-13

### What's changed
None, this is a bug fix release

### Fixes
* Fix crash by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/766
* Fix crashes that occur when the user has a widget set up by @Darandos in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/786
* Fix exception report spam on unsupported file type by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/795
* Fix permission request music folder crash by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/793
* Fix Folder view, where files with non-latin characters not showing by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/796

### Other Changes
* Tools update by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/763
* feat(ci): Setup PlayStore build by @AdrienPoupa in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/783
* Fix local build by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/791

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.3-test-ci.3...1.5.4

## [1.5.3] - 2023-10-09

### What's changed
* Major change to support Android 13, by @soncaokim and @Octoton
* Catalan translation updated by @albertgasset
* German translation updated by @tschlegeldigos

### Fixes
* Fix orientation bug by @Osiris-Team

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.2...1.5.3-test-ci.3

## [1.5.2] - 2023-07-01

This version is the same as 1.5.1, but with correct versioning number.

## [1.5.1] - 2023-07-01

### What's changed
* Catalan translation updated by @albertgasset in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/709
* Use MarkdownViewDialog to improve visualising LastFM artist bio and album wiki by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/703
* Enqueue song action can now be choosen in preference by @Octoton in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/715

### Fixes
* Misc changes for artist cover image by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/697
* Fix snackbar unreadable text by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/706
* Avoid crash while loading top tracks by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/728
* Cab icon tint by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/726
* Play next color by @Octoton in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/723

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.5.0...1.5.1

## [1.5.0] - 2023-02-17

### What's changed
* Make song detail info selectable and copyable by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/645
* Folders view sort options by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/657
* Queue change confirmation by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/658
* Make the drawer and now playing screen shadow over the cover image darker (improve for white/bright cover image) by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/664
* Cleaner markdown visualisation by @Octoton in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/684
* Tag editor: Use darker shadow for toolbar to improve button visibility on white cover art by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/682
* Visual song action by @Octoton in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/679

### Fixes
* Fix start service crash by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/644
* Fix bug in StaticQueue implementation by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/651
* Fix NPE on Discog by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/656
* Fix crash unfavorite songs by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/666
* Broader exception catch on restoring saved queue by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/669
* Fix artist sorting by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/686
* The back navigation button on the bug report screen was not responding by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/687
* Refactor Skipped songs implementation by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/672

### Other Changes
* Oops handler by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/646
* Tag editor trim spaces by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/647
* Upgrade jaudiotagger + java by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/648
* Upgrade MaterialCab by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/649
* New About dialog by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/660
* Missing french translation by @Octoton in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/678
* Refactor Licenses dialog by @soncaokim in https://github.com/VinylMusicPlayer/VinylMusicPlayer/pull/680

**Full Changelog**: https://github.com/VinylMusicPlayer/VinylMusicPlayer/compare/1.4.1...1.5.0

## [1.4.1] - 2023-01-25
### What's Changed
* Feat: Ignore "the" and "a" prefixes when sorting artists by @louis-prudhomme in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/630

### Translation updates
* fix typo by @Longway22 in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/637
* French translation followup by @GladiusTM in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/634

### Fixes
* Fix a crash due to #620 by @gaycodegal in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/639

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
* Add search for genre and playlist, by @gaycodegal in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/619

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
* Fix crash by by @poolborges in https://github.com/AdrienPoupa/VinylMusicPlayer/pull/617

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
