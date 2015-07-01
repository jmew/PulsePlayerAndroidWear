package uwaterloo.ca.wearplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

/**
 * Created by Mew on 2015-06-04.
 */
public class ArtistsActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView songs = new TextView(getApplicationContext());
                songs.setGravity(17);
                songs.setText("Artists");
                stub.addView(songs);
            }
        });
    }
}