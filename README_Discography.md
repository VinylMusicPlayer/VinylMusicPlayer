# Discography

## Features roadmap

- [x] Extract album artist from ID3 tags and use that for album grouping/sorting

- [ ] Handle compilation albums (with TCMP/cpil tag)

- [ ] Adopt play history database, in order to provide dynamic playlist

- [ ] Support multiple artists (X feat Y; X and Y; X/Y).

## Engineering

- [ ] Make unknown album/artist/genre display text localizable

- [ ] Refact the SortOrder to rely on enum/enum class, i.e. avoid doing string comparison

- [x] Provide observer for add/remove/update entries in the in-memory cache

## Misc todo

- [ ]  Investigate why the notification play/pause state doesnt reflect the current playback state

