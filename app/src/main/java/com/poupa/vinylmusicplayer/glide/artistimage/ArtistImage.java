package com.poupa.vinylmusicplayer.glide.artistimage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistImage {
    public final String artistName;
    public final boolean skipOkHttpCache;

    public ArtistImage(String artistName, boolean skipOkHttpCache) {
        this.artistName = artistName;
        this.skipOkHttpCache = skipOkHttpCache;
    }

  @Override
  public int hashCode() {
    return artistName.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof ArtistImage) {
      ArtistImage other = (ArtistImage) object;
      return artistName.equals(other.artistName) && skipOkHttpCache == other.skipOkHttpCache;
    }
    return false;
  }
}
