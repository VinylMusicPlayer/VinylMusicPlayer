# Discography

## Features roadmap

- [ ] First run UI to give visual feedback to the potentially slow import of MediaStore data to Discog database

- [x] Genre editor.
  For some select song (ex Evanescence / Fallen album), changing genre to 'Heavy Metal' always turn it into 137 (instead of the text inserted).
  As if the change is redacted by JAudioTagger or the OS.

- [ ] Extract album artist from ID3 tags and use that for album grouping/sorting

- [ ] Adopt play history database, in order to provide dynamic playlist

- [ ] Support multiple artists

## Engineering

- [ ] The by-year ordering in the Songs tab is messed up since it relies on the year info provided by MediaStore (which is buggy)

- [ ] Provide observer for add/remove/update entries in the in-memory cache

- [ ] Refact the SortOrder to rely on enum/enum class, i.e. avoid doing string comparison

- [ ] Replace DelayedTaskThread by the standard AsyncTask (or any more modern alternative)

## Misc todo

- [ ]  Investigate why the notification play/pause state doesnt reflect the current playback state

