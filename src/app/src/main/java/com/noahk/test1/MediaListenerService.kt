package com.noahk.NowPlayingCarView

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaListenerService : NotificationListenerService() {

    companion object {
        private val _trackState = MutableStateFlow<TrackInfo?>(null)
        val trackState: StateFlow<TrackInfo?> = _trackState

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying

        private val _controller = MutableStateFlow<MediaController?>(null)
        val controller: StateFlow<MediaController?> = _controller
    }

    private var activeController: MediaController? = null
    private var positionJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateTrack(metadata, activeController?.playbackState)
        }
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateTrack(activeController?.metadata, state)
            restartPositionPolling(state)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshController()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshController()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshController()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        positionJob?.cancel()
        serviceScope.cancel()
    }

    private fun refreshController() {
        val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val controllers = manager.getActiveSessions(
            android.content.ComponentName(this, MediaListenerService::class.java)
        )

        activeController?.unregisterCallback(callback)

        activeController = controllers.firstOrNull()
        activeController?.registerCallback(callback)

        updateTrack(activeController?.metadata, activeController?.playbackState)
        restartPositionPolling(activeController?.playbackState)

        _controller.value = activeController
    }

    private fun restartPositionPolling(state: PlaybackState?) {
        positionJob?.cancel()
        val playing = state?.state == PlaybackState.STATE_PLAYING
        if (!playing) return

        positionJob = serviceScope.launch {
            while (isActive) {
                val ps = activeController?.playbackState
                val pos = ps?.position ?: 0L
                val dur = activeController?.metadata
                    ?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L

                // Update existing track info with latest position
                _trackState.value?.let { current ->
                    _trackState.value = current.copy(position = pos, duration = dur)
                }

                delay(500L)
            }
        }
    }

    private fun getAppLabel(packageName: String?): String {
        if (packageName.isNullOrEmpty()) return ""
        return try {
            val pm = applicationContext.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }

    private fun updateTrack(metadata: MediaMetadata?, state: PlaybackState?) {
        if (metadata == null) {
            _trackState.value = null
            _isPlaying.value = false
            return
        }

        val playing = state?.state == PlaybackState.STATE_PLAYING

        _isPlaying.value = playing
        _trackState.value = TrackInfo(
            title   = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown",
            artist  = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: "Unknown",
            album   = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: "",
            albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART),
            isPlaying = playing,
            sourceApp = getAppLabel(activeController?.packageName),
            position = state?.position ?: 0L,
            duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        )
    }
}