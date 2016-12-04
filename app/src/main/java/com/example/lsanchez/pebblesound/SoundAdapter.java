package com.example.lsanchez.pebblesound;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;

import javax.sql.DataSource;

/**
 * Created by lsanchez on 12/3/16.
 */

public class SoundAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mDataSource;
    private ArrayList<Uri> mDataLink;

    public SoundAdapter(Context context, ArrayList<String> items, ArrayList<Uri> paths) {
        mContext = context;
        mDataSource = items;
        mDataLink = paths;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.sound_item, viewGroup, false);

        // Get title element
        TextView titleTextView = (TextView) rowView.findViewById(R.id.textViewSound);

        // Get subtitle element
        Button subtitleTextView = (Button) rowView.findViewById(R.id.buttonSound);

        titleTextView.setText(mDataSource.get(i));
        subtitleTextView.setTag(mDataLink.get(i));
        subtitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "" + FileUtils.getPath(view.getContext(), (Uri) view.getTag()), Toast.LENGTH_SHORT).show();
                Uri myUri = (Uri) view.getTag(); // initialize Uri here
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(view.getContext(), myUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return rowView;
    }
}
