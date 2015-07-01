package uwaterloo.ca.wearplayer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Mew on 2015-06-04.
 */
public class PulseActivity extends Activity {

    private SensorManager sensormanager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView songs = new TextView(getApplicationContext());
                songs.setGravity(17);
                songs.setText("Pulse");
                stub.addView(songs);

                TextView pulse = new TextView(getApplicationContext());
                sensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
                Sensor heartRateSensor = sensormanager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                heartRate heartRateListener = new heartRate(pulse);
                stub.addView(pulse);
                sensormanager.registerListener(heartRateListener, heartRateSensor, SensorManager.SENSOR_DELAY_UI);
            }
        });
    }

    class heartRate implements SensorEventListener {
        TextView output;

        public heartRate(TextView outputView){
            output = outputView;
        }

        @Override
        public void onAccuracyChanged(Sensor s, int i) {}

        @Override
        public void onSensorChanged(SensorEvent se) {
            if (se.sensor.getType() == Sensor.TYPE_HEART_RATE && se.values.length > 0) {
                output.setText(String.format("%d", se.values[0]));
            }
        }
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        sensormanager.unregisterListener(this);
//    }
}
