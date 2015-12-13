package jmew.ca.pulseplayer;

import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Track;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private ArrayList<Track> tracks;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;

        public ViewHolder(View v) {
            super(v);
            txtHeader = (TextView) v.findViewById(R.id.firstLine);
            txtFooter = (TextView) v.findViewById(R.id.secondLine);
        }
    }

    public void add(int position, Track item) {
        tracks.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(String item) {
        int position = tracks.indexOf(item);
        tracks.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecycleViewAdapter(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pulse_playlist_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Track name = tracks.get(position);
        try {
            holder.txtHeader.setText(tracks.get(position).getTitle());
            holder.txtFooter.setText(tracks.get(position).getArtistName());
        } catch (EchoNestException e) {
            e.printStackTrace();
        }
        holder.txtHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return tracks.size();
    }
}
