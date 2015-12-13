package jmew.ca.pulseplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.echonest.api.v4.ArtistCatalog;
import com.echonest.api.v4.ArtistCatalogItem;
import com.echonest.api.v4.CatalogUpdater;
import com.echonest.api.v4.DynamicPlaylistParams;
import com.echonest.api.v4.DynamicPlaylistSession;
import com.echonest.api.v4.Song;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Playlist;
import com.echonest.api.v4.PlaylistParams;
import com.echonest.api.v4.TrackAnalysis;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Mew on 2015-11-16.
 */
public class MusicPlayer extends MainActivity implements PlayerNotificationCallback, ConnectionStateCallback {

    protected SpotifyApi api = new SpotifyApi();
    protected SpotifyService spotify;

    private Handler trackProgressHandler = new Handler();
    private static EchoNestAPI en;

    public MusicPlayer(Player player) {
        mPlayer = player;
        mPlayer.addConnectionStateCallback(MusicPlayer.this);
        mPlayer.addPlayerNotificationCallback(MusicPlayer.this);

        api.setAccessToken(SpotifyHelper.getAuthToken());
        spotify = api.getService();
        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                SpotifyHelper.setUserId(userPrivate.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Playlist Fragment", "Could not get userId: " + error.getMessage());
            }
        });
    }

    private static class EchoNestPlayer extends AsyncTask<Integer, Integer, ArrayList> {
        private ArrayList<com.echonest.api.v4.Track> tracks = new ArrayList<>();

        @Override
        protected ArrayList doInBackground(Integer... integers) {
            en = new EchoNestAPI("TMYLDGXSQAL7JAPZQ");
            DynamicPlaylistParams params = new DynamicPlaylistParams();

            params.addIDSpace("spotify-CA");
            params.setType(PlaylistParams.PlaylistType.GENRE_RADIO);
            params.addGenre("pop");
            params.addGenre("hip hop");
            params.addGenre("edm");
            params.addGenre("rock");
            params.addGenre("country");
            params.addSongType(Song.SongType.studio, Song.SongTypeFlag.True);
            params.setArtistMinFamiliarity(0.825f);
            params.setMinDuration(120);
            params.setMaxTempo(integers[0] + 5);
            params.setMinTempo(integers[0] - 5);
            params.includeTracks();
            params.setLimit(true);
            try {
                DynamicPlaylistSession session = en.createDynamicPlaylist(params);
                Playlist playlist = session.next();
                tracks.add(playlist.getSongs().get(0).getTrack("spotify-CA"));
                mPlayer.play(playlist.getSongs().get(0).getTrack("spotify-CA").getForeignID().replace("-CA", "").replace("-AD", ""));
                playlist = session.next(10, 1);
                Log.e("Track", playlist.getSongs().get(0).getTrack("spotify-CA").getForeignID().replace("-CA", "").replace("-AD", ""));
                for (Song song : playlist.getSongs().subList(1, playlist.getSongs().size())) {
                    com.echonest.api.v4.Track track = song.getTrack("spotify-CA");
                    tracks.add(track);
                    mPlayer.queue(track.getForeignID().replace("-CA", "").replace("-AD", ""));
                    Log.e("EchoNest", track.getForeignID() + " " + song.getTitle() + " by " + song.getArtistName());

                }
            } catch (EchoNestException e) {
                Log.e("EchoNest", e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // TODO: check this.exception
            // TODO: do something with the feed
            HomeFragment.putTracksIntoList(tracks);
        }
    }

    public static void createPulsePlaylist(float heartRate) throws EchoNestException {
        new EchoNestPlayer().execute(new Integer((int)heartRate));
    }

    public static void playTrack(String uri) {
    }

    public static void playPlaylist(List<String> uris, boolean shuffle) {
        if (shuffle) {
            mPlayer.play(uris);
            mPlayer.setShuffle(true);
        } else {
            mPlayer.play(uris);
        }
    }

    public static void clearTracks() {
        mPlayer.clearQueue();
    }

    public static void playPause(final ImageView button) {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                if (playerState.playing) {
                    mPlayer.pause();
                    button.setImageResource(R.drawable.ic_action_av_play_arrow); //TODO make it change from play to pause programaticlly
                } else {
                    mPlayer.resume();
                    button.setImageResource(R.drawable.ic_action_av_pause);
                }
            }
        });
    }

    public void setTrackProgressBar() {
        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                trackProgressBar.setMax(playerState.durationInMs);
                trackProgressBar.setProgress(playerState.positionInMs);
            }
        });
        trackProgressHandler.postDelayed(run, 75);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        switch(eventType) {
            case TRACK_CHANGED:
                Log.d("track", "Track Changed");
                spotify.getTrack(playerState.trackUri.replace("spotify:track:", ""), new Callback<Track>() {
                    @Override
                    public void success(Track track, Response response) {
                        Log.d("IMAGE URL", track.album.images.get(0).url);
                        new LoadImage().execute(track.album.images.get(0).url);
                        noTracksMessage.setVisibility(View.GONE);
                        trackName.setVisibility(View.VISIBLE);
                        artistName.setVisibility(View.VISIBLE);
                        trackName.setText(track.name);
                        artistName.setText(track.artists.get(0).name);
                        setTrackProgressBar();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("Image Error", error.getMessage());
                    }
                });
                break;
        }
    }

    @Override
    public void onLoggedIn() { Log.d("MainActivity", "User logged in"); }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        Bitmap bitmap;

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                albumArt.setImageBitmap(image);

            }else{

                Toast.makeText(MusicPlayer.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            setTrackProgressBar();
        }
    };

//    public boolean addArtists(ArtistCatalog tp, String[] names) throws EchoNestException {
//        CatalogUpdater updater = new CatalogUpdater();
//        int id = 1;
//
//        for (String name : names) {
//            ArtistCatalogItem item = new ArtistCatalogItem("id-" + id);
//            item.setArtistName(name);
//            updater.update(item);
//            id++;
//        }
//        String ticket = tp.update(updater);
//        return tp.waitForUpdates(ticket, 30000);
//    }
//
//    public ArtistCatalog createTasteProfile(String name) throws EchoNestException {
//        System.out.println("Creating Taste Profile " + name);
//        ArtistCatalog tp = en.createArtistCatalog(name);
//        String[] artists = {"weezer", "the beatles", "ben folds", "explosions in the sky",
//                "this will destroy you", "muse", "bjork"};
//        addArtists(tp, artists);
//        return tp;
//    }
//
//    public ArtistCatalog findTasteProfile(String name) throws EchoNestException {
//        for (ArtistCatalog ac : en.listArtistCatalogs()) {
//            if (ac.getName().equals(name)) {
//                return ac;
//            }
//        }
//        return null;
//    }
//
//    public void createPlaylist(ArtistCatalog tp) throws EchoNestException {
//        PlaylistParams p = new PlaylistParams();
//        p.setType(PlaylistParams.PlaylistType.CATALOG_RADIO);
//        p.addSeedCatalog(tp.getID());
//        Playlist playlist = en.createStaticPlaylist(p);
//
//        for (Song song : playlist.getTracks()) {
//            System.out.println(song.getArtistName() + " " + song.getTitle());
//        }
//    }
}
