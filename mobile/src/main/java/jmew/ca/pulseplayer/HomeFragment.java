package jmew.ca.pulseplayer;

/**
 * Created by Mew on 2015-11-08.
 */

import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.util.ArrayList;
import java.util.Collections;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


public class HomeFragment extends Fragment {

    private SpotifyApi api = new SpotifyApi();
    private String userId;

    private SpotifyService spotify;

    private Context mContext;
    private SensorEventListener heartRateSensor;
    private DecoView mDecoView;
    private SeriesItem backSeriesItem;

    private ValueAnimator colorAnimation;
    private static RecyclerView mRecyclerView;

    private int mHeartRateSeriesIndex;
    private int mBackIndex;
    private final float mSeriesMax = 150f;
    private ArrayList<Integer> lastHeartRates;
    private static View mCardView;

    private Sensor mHeartRateSensor;
    private SensorManager mSensorManager;

    private TextView mHeartRateText;

    public HomeFragment() {
        mContext = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mSensorManager = (SensorManager) rootView.getContext().getSystemService(mContext.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        heartRateSensor = new HeartRateSensor();

        mHeartRateText = (TextView) rootView.findViewById(R.id.heartRateText);
        mDecoView = (DecoView) rootView.findViewById(R.id.dynamicArcView);
        createBackSeries();
        createBackSeriesEvent();

        mDecoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashCircleEvent(1000);

                RelativeLayout mRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.heart_rate_fragment_info);
                LineGraphView graph = new LineGraphView(rootView.getContext(), 400);
                RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
                relativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                relativeParams.addRule(RelativeLayout.ABOVE, R.id.heartRateText);
                graph.setLayoutParams(relativeParams);
                mRelativeLayout.addView(graph);
                graph.setVisibility(View.VISIBLE);

                graph.bringToFront();
                for (int i = 0; i < 400; i++) {
                    graph.addPoint(0);
                }

                lastHeartRates = new ArrayList<>();
                heartRateSensor = new HeartRateSensor();
                getActivity().findViewById(R.id.heartRateLabel).setVisibility(View.GONE);
                mSensorManager.registerListener(heartRateSensor, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mHeartRateText.setTextSize(25);
                mHeartRateText.setText("Measuring. Keep your finger still.");
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        userId = SpotifyHelper.getUserId();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(heartRateSensor);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void createBackSeries() {
        backSeriesItem = new SeriesItem.Builder(Color.parseColor("#FFE0E0E0"))
                .setRange(0, mSeriesMax, mSeriesMax)
                .setInitialVisibility(true)
                .build();

        mBackIndex = mDecoView.addSeries(backSeriesItem);
    }

    private void createAnimationSeries(Integer color) {
        backSeriesItem.setColor(color);
        mDecoView.executeReset();
        mBackIndex = mDecoView.addSeries(backSeriesItem);
        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .build());
    }

    private void createDataSeries() {
        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE91E63"))
                .setRange(0, mSeriesMax, 0)
                .setInitialVisibility(false)
                .build();

        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                int BPM = (int) currentPosition;
                if (BPM >= 100) {
                    mHeartRateText.setText(String.format("%03d", BPM));
                } else {
                    mHeartRateText.setText(Html.fromHtml("0<b>" + String.format("%02d", BPM) + "</b>"));
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        mHeartRateSeriesIndex = mDecoView.addSeries(seriesItem);
    }

    private void createBackSeriesEvent() {
        mDecoView.addEvent(new DecoEvent.Builder(mSeriesMax)
                .setIndex(mBackIndex)
                .build());
    }

    private void showHeartRateCircleEvent(float heartRate) {
        mDecoView.executeReset();
        createBackSeriesEvent();

        mDecoView.addEvent(new DecoEvent.Builder(heartRate)
                .setIndex(mHeartRateSeriesIndex)
                .setDuration(1750)
                .build());
    }

    private void flashCircleEvent(int heartBeat) {
//        int animationTime = heartBeat / 60;
        Integer colorFrom = Color.parseColor("#FFE0E0E0");
        Integer colorTo = Color.parseColor("#FFE91E63");
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(800);
        colorAnimation.setRepeatCount(99); //TODO change from 99
        colorAnimation.setRepeatMode(2);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                createAnimationSeries((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private void countUp(int start, int end) {
        ValueAnimator animator = new ValueAnimator();
        animator.setObjectValues(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                mHeartRateText.setText(String.valueOf(animation.getAnimatedValue()) + "%");
            }
        });
        animator.setEvaluator(new TypeEvaluator<Integer>() {
            public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    public static void putTracksIntoList(ArrayList<com.echonest.api.v4.Track> tracks) {
        Log.d("Recycle", "YESS");
        mCardView.setVisibility(View.VISIBLE);
//        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.pulse_playlist);
//        mRecyclerView.setHasFixedSize(true); //TODO remove
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(mLayoutManager);
//
//        mCardView = getActivity().findViewById(R.id.pulse_playlist_card_view);
//        RecyclerView.Adapter mAdapter = new RecycleViewAdapter(tracks);
//        mRecyclerView.setAdapter(mAdapter);
    }

    class HeartRateSensor implements SensorEventListener {

        private int dataPoints = 0;

        @Override
        public void onAccuracyChanged(Sensor s, int i) {
        }

        @Override
        public void onSensorChanged(SensorEvent se) {
            if (dataPoints == 6) {
                float averageHeartRate = 0;
                mSensorManager.unregisterListener(heartRateSensor);
                lastHeartRates.remove(0);
                lastHeartRates.remove(1);
                Collections.sort(lastHeartRates);
                averageHeartRate = (lastHeartRates.get(1) + lastHeartRates.get(2)) / 2;
                try {
                    MusicPlayer.createPulsePlaylist(averageHeartRate);
                } catch (EchoNestException e) {
                    Log.e("EchoNest", e.getMessage());
                }

                colorAnimation.end();
                mHeartRateText.setTextSize(70);
                mHeartRateText.setTypeface(null, Typeface.NORMAL);
                mHeartRateText.setText(String.valueOf(averageHeartRate));
                getActivity().findViewById(R.id.heartRateLabel).setVisibility(View.VISIBLE);
                createBackSeries();
                createDataSeries();
                showHeartRateCircleEvent(averageHeartRate);
                api.setAccessToken(SpotifyHelper.getAuthToken());
                spotify = api.getService();
            } else {
                if (se.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    if ((int) se.values[0] > 0) {
                        if (dataPoints == 1) {
                            mHeartRateText.setTextSize(70);
                            mHeartRateText.setTypeface(null, Typeface.BOLD);
                            countUp(0, 20);
                        } else if (dataPoints == 2) {
                            countUp(20, 40);
                        } else if (dataPoints == 3) {
                            countUp(40, 60);
                        } else if (dataPoints == 4) {
                            countUp(60, 80);
                        } else if (dataPoints == 5) {
                            countUp(80, 100);
                        }
                        lastHeartRates.add((int) se.values[0]);
                        Log.d("Heart Rate", String.valueOf(se.values[0]));
                        dataPoints++;
                    }
                }
            }
        }
    }
}