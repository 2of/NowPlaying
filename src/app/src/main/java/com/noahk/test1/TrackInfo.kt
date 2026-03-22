package com.noahk.NowPlayingCarView

import android.graphics.Bitmap

data class TrackInfo(
    val title: String,
    val artist: String,
    val album: String,
    val nextTrack: String = "",
    val nextArtist: String = "",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val sourceApp: String = "",
    val position: Long = 0L,
    val duration: Long = 0L
)