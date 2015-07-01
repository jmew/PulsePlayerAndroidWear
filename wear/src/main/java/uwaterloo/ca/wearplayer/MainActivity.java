package uwaterloo.ca.wearplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements WearableListView.ClickListener {

    private WearableListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                    listView = (WearableListView) stub.findViewById(R.id.sample_list_view);
                    loadAdapter();
            }
        });
    }

    private void loadAdapter() {
        List<SettingsItems> items = new ArrayList<>();
        items.add(new SettingsItems(R.drawable.ic_now_playing, getString(R.string.now_playing)));
        items.add(new SettingsItems(R.drawable.ic_songs, getString(R.string.songs)));
        items.add(new SettingsItems(R.drawable.ic_artists, getString(R.string.artists)));
        items.add(new SettingsItems(R.drawable.ic_playlists, getString(R.string.playlists)));
        items.add(new SettingsItems(R.drawable.ic_heartbeat, getString(R.string.pulse)));

        SettingsAdapter mAdapter = new SettingsAdapter(this, items);

        listView.setAdapter(mAdapter);

        listView.setClickListener(this);
        }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        switch (viewHolder.getPosition()) {
            case 0: //Now Playing
                break;
            case 1: //Songs
                startActivity(new Intent("uwaterloo.ca.wearplayer.SongsActivity"));
                break;
            case 2: //Artists
                startActivity(new Intent("uwaterloo.ca.wearplayer.ArtistsActivity"));
                break;
            case 3: //Playlists
                startActivity(new Intent("uwaterloo.ca.wearplayer.PlaylistsActivity"));
                break;
            case 4: //Pulse
                startActivity(new Intent("uwaterloo.ca.wearplayer.PulseActivity"));
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
            //Prevent NullPointerException
    }
}
//    private GoogleApiClient getGoogleApiClient(Context context) {
//        return new GoogleApiClient.Builder(context)
//                .addApi(Wearable.API)
//                .build();
//    }

