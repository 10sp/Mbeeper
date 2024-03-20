package om.habibi.mbeeper;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "BeepApp";
  private static final Uri FILENAME = Uri.parse("beep.wav");
  private static final int SAMPLE_RATE = 44100; // Sample rate of the audio
  private AudioTrack player;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final byte[] audioData = loadAudioFromRaw(R.raw.beep);

    final int audioLength = audioData.length;

    final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

    final Context context = this; // Get context for Beeper

    final Beeper beeper = new Beeper(context); // Initialize Beeper

    // Button click listeners
    findViewById(R.id.buttonAudioTrack).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playAudioTrack(audioData, audioLength);
      }
    });

    findViewById(R.id.buttonBeeper).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        beeper.beep("hello", FILENAME); // Use Beeper class to play beep
      }
    });

    findViewById(R.id.buttonToneGenerator).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_S, 1000);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    releaseAudioTrack();
  }

  // Method to load audio data from file in the raw directory
  private byte[] loadAudioFromRaw(int rawResourceId) {
    try {
      InputStream inputStream = getResources().openRawResource(rawResourceId);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }
      inputStream.close();
      return outputStream.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new byte[0]; // Return empty array if loading fails
  }

  private void playAudioTrack(final byte[] audioData, final int audioLength) {
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                releaseAudioTrack(); // Release existing AudioTrack resources
                // Calculate buffer size based on audio data length and sample rate
                int bufferSize = audioLength * 2; // Assuming 16-bit PCM, 1 channel
                player =
                    new AudioTrack.Builder()
                        .setAudioAttributes(
                            new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                        .setAudioFormat(
                            new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(bufferSize)
                        .build();
                if (player.getState() == AudioTrack.STATE_INITIALIZED) {
                  player.write(audioData, 0, audioLength);
                  player.play();
                } else {
                  // Handle initialization failure
                  Log.e(TAG, "Failed to initialize AudioTrack");
                }
              }
            })
        .start();
  }

  // Method to release the AudioTrack resources
  private void releaseAudioTrack() {
    if (player != null) {
      player.stop();
      player.release();
      player = null;
    }
  }
}
