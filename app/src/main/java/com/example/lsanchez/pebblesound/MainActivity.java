package com.example.lsanchez.pebblesound;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FileChooserExampleActivity";
    private static final int REQUEST_CODE = 6384;
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 6789;
    ArrayList<Uri> uris;
    ArrayList<String> names;
    ListView mListView;
    PebbleKit.PebbleDataReceiver dataReceiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.soundlist);

        uris = new ArrayList<>();
        names = new ArrayList<>();

        SoundAdapter adapter = new SoundAdapter(this, names, uris);
        mListView.setAdapter(adapter);

        // Assume thisActivity is the current activity
        int permissionCheck = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        // Here, thisActivity is the current activity
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (this.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        Button button = (Button) findViewById(R.id.buttonAdd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the file chooser dialog
                showChooser();
            }
        });

        // Create a new dictionary
        PebbleDictionary dict = new PebbleDictionary();

        final int AppKeyStatus = 0;
        final int AppKeyMessage = 1;

        // Get data from the app
        final String message = "getContact()";
        final int status = 2; //getAge();

        // Add data to the dictionary
        dict.addInt32(AppKeyStatus, status);
        dict.addString(AppKeyMessage, message);

        final UUID appUuid = UUID.fromString("4ac89219-82e4-4548-994b-638dec2c3250");

        // Send the dictionary
        PebbleKit.sendDataToPebble(getApplicationContext(), appUuid, dict);

        // Create a new receiver to get AppMessages from the C app
        dataReceiver = new PebbleKit.PebbleDataReceiver(appUuid) {

            @Override
            public void receiveData(Context context, int transaction_id,
                                    PebbleDictionary dict) {
                // A new AppMessage was received, tell Pebble
                String string = dict.getString(1);
                PebbleKit.sendAckToPebble(context, transaction_id);
                if(!Objects.equals(string, "getContact()")) {
                    Uri myUri = uris.get(Integer.parseInt(string)); // initialize Uri here
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.setDataSource(MainActivity.this, myUri);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

    }

    @Override
    public void onResume() {
        super.onResume();

        // Register the receiver
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                int ok = RESULT_OK;
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String name = FileUtils.getFile(this, uri).getName();
                            Toast.makeText(MainActivity.this,
                                    "File Selected: " + name, Toast.LENGTH_LONG).show();

                            uris.add(uri);
                            names.add(name);

                            SoundAdapter adapter = new SoundAdapter(this, names, uris);
                            mListView.setAdapter(adapter);
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
