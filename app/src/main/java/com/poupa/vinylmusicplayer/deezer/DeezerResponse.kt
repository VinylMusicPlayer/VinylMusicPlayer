package com.poupa.vinylmusicplayer.deezer

/**
 * @author Paolo Valerdi
 */
data class DeezerResponse(
        val `data`: List<Data>,
        val next: String,
        val total: Int
)