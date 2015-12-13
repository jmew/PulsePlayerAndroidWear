package jmew.ca.pulseplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by Mew on 2015-11-08.
 */
public class SettingsFragment extends Fragment {

    private SpotifyApi api = new SpotifyApi();
    private String token;
    private String userId;

    private SpotifyService spotify;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = SpotifyHelper.getUserId();

        token = SpotifyHelper.getAuthToken();

        api.setAccessToken(token);
        spotify = api.getService();

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
