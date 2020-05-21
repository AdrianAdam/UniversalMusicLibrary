package com.example.adrianadam.universalmusiclibrary

import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.music_controller_free.*
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote



class MusicControllerFree : YouTubeBaseActivity() {

    var bIsSongPlaying = false
    var bNeedToResumeSong = false
    var bIsSpotifyMusicPlaying = false
    lateinit var youtubePlayerView: YouTubePlayerView
    lateinit var youtubeListener: YouTubePlayer.OnInitializedListener
    var globalYouTubePlayer: YouTubePlayer? = null
    lateinit var btnPlay: Button

    val CLIENT_ID: String = "SPOTIFY_CLIENT_ID"
    val REDIRECT_URI: String = "SPOTIFY_REDIRECT_ID"
    var mSpotifyAppRemote: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.music_controller_free)

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams, object: Connector.ConnectionListener {
            override fun onFailure(p0: Throwable?) {
                Log.e("SpotifyError", p0.toString())
            }

            override fun onConnected(p0: SpotifyAppRemote?) {
                mSpotifyAppRemote = p0
            }

        })

        youtubePlayerView = findViewById(R.id.ytPlayer)

        youtubeListener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, youTubePlayer: YouTubePlayer?, p2: Boolean) {
                globalYouTubePlayer = youTubePlayer

                globalYouTubePlayer?.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
                        override fun onAdStarted() {}
                        override fun onLoading() {}
                        override fun onVideoStarted() {}
                        override fun onLoaded(p0: String?) {}
                        override fun onError(p0: YouTubePlayer.ErrorReason?) {}

                        override fun onVideoEnded() {
                            btnNext.performClick()
                        }
                })
            }

            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                Toast.makeText(this@MusicControllerFree, "Error loading YouTube player", Toast.LENGTH_SHORT).show()
            }
        }
        youtubePlayerView.initialize("YOUTUBE_API_KEY", youtubeListener)

        var nCurrentSong = 0

        var listView: ListView = findViewById(R.id.lv_list_posts)
        var linkAdapter = LinkAdapter(this)

        var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("links/" + FirebaseAuth.getInstance().currentUser?.uid.toString())

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                linkAdapter.addLink(p0.child("link").value.toString(), p0.key.toString())
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })

        var etLinkAddress: EditText = findViewById(R.id.etLink)
        var btnAddLink: Button = findViewById(R.id.btnAddLink)

        btnAddLink.setOnClickListener({
            if(!etLinkAddress.text.toString().equals(""))
            {
                val key: String = databaseReference.push().getKey().toString()
                databaseReference.child(key).child("link").setValue(etLinkAddress.text.toString())
                etLinkAddress.text.clear()
            }
        })

        var btnPrevious: Button = findViewById(R.id.btnPrevious)
        btnPlay = findViewById(R.id.btnPlay)
        var btnNext: Button = findViewById(R.id.btnNext)

        btnPlay.setOnClickListener({
            if(bIsSongPlaying)
            {
                var strLink = linkAdapter.getItem(nCurrentSong).link

                if(strLink.matches("^https:\\/\\/youtu\\.be\\/[A-Za-z0-9]+\$".toRegex()))
                {
                    pauseSong(true)
                }
                else if(strLink.matches("^https:\\/\\/open\\.spotify\\.com\\/playlist\\/[A-Za-z0-9?=_]+\$".toRegex()))
                {
                    pauseSong(false)
                }
            }
            else
            {
                nCurrentSong = checkCurrentSongIndex(linkAdapter.links.size, nCurrentSong)

                playSong(linkAdapter, nCurrentSong)
            }
        })

        btnPrevious.setOnClickListener({
            nCurrentSong--

            nCurrentSong = checkCurrentSongIndex(linkAdapter.links.size, nCurrentSong)

            playSong(linkAdapter, nCurrentSong)
        })

        btnNext.setOnClickListener({
            nCurrentSong++

            nCurrentSong = checkCurrentSongIndex(linkAdapter.links.size, nCurrentSong)

            mSpotifyAppRemote?.playerApi?.pause()

            playSong(linkAdapter, nCurrentSong)
        })

        listView.adapter = linkAdapter
    }

    fun playSong(linkAdapter: LinkAdapter, nCurrentSong: Int)
    {
        btnPlay.setBackgroundResource(R.drawable.ic_pause)

        bIsSongPlaying = true

        var strLink: String = linkAdapter.getItem(nCurrentSong).link

        if(bNeedToResumeSong)
        {
            if(strLink.matches("^https:\\/\\/youtu\\.be\\/[A-Za-z0-9]+\$".toRegex()))
            {
                bIsSpotifyMusicPlaying = false
                globalYouTubePlayer?.play()
            }
            else if(strLink.matches(Regex("^https:\\/\\/open\\.spotify\\.com\\/playlist\\/[A-Za-z0-9?=_]+\$")))
            {
                bIsSpotifyMusicPlaying = true
                mSpotifyAppRemote?.playerApi?.resume()
            }

            bNeedToResumeSong = false
        }
        else
        {
            if(strLink.matches("^https:\\/\\/youtu\\.be\\/[A-Za-z0-9]+\$".toRegex()))
            {
                bIsSpotifyMusicPlaying = false
                globalYouTubePlayer?.loadVideo(strLink.substringAfterLast("/"))
            }
            else if(strLink.matches(Regex("^https:\\/\\/open\\.spotify\\.com\\/playlist\\/[A-Za-z0-9?=_]+\$")))
            {
                bIsSpotifyMusicPlaying = true

                var strActualLink = strLink.substringAfterLast("/").split("?")[0]

                mSpotifyAppRemote?.playerApi?.play("spotify:playlist:" + strActualLink)

                mSpotifyAppRemote?.getPlayerApi()
                    ?.subscribeToPlayerState()
                    ?.setEventCallback { playerState ->
                        val track = playerState.track
                        if (track != null) {
                            Toast.makeText(this@MusicControllerFree, "Artist: " + track.artist.name + " Song: " + track.name, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    fun pauseSong(bIsYoutubeSong: Boolean)
    {
        btnPlay.setBackgroundResource(R.drawable.ic_play)

        bIsSongPlaying = false
        bNeedToResumeSong = true

        if(bIsYoutubeSong)
        {
            globalYouTubePlayer?.pause()
        }
        else
        {
            mSpotifyAppRemote?.playerApi?.pause()
        }
    }

    fun checkCurrentSongIndex(maxIndex: Int, currentIndex: Int): Int
    {
        if(currentIndex >= maxIndex)
        {
            return 0
        }
        else
        {
            if(currentIndex < 0)
            {
                return maxIndex - 1
            }
            else
            {
                return currentIndex
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if(bIsSpotifyMusicPlaying)
        {
            // No need to resume it
            // mSpotifyAppRemote?.playerApi?.resume()
        }
        else
        {
            globalYouTubePlayer?.play()
        }
    }
}

class Link {
    lateinit var link: String
    lateinit var key: String
}

class LinkTag {
    lateinit var link: TextView
    lateinit var remove: Button
}
