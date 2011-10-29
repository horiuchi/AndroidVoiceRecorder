/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder;

import java.io.IOException;
import java.io.InputStream;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * @author horiuchi
 *
 */
public class AudioPlayTask extends StopableTask {

	private static final String TAG = "VoiceChangerSample";

	private final int bufferSize;
	private final AudioTrack player;
	private final InputStream rawInput;

	public AudioPlayTask(ProgressBar bar, int sampleRateInHz, int channelConfig, int audioFormat, InputStream rawInput) {
		super(bar);
		this.rawInput = rawInput;
		this.bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		this.player = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
	}

	@Override
	public void run() {
		isRunning.set(true);

		player.play();
		try {
			byte[] buffer = new byte[bufferSize];

			int len = 0;
			while (isRunning() && (len = rawInput.read(buffer)) != -1) {
				player.write(buffer, 0, len);
				addValue(len);
			}

		} catch (IOException ex) {
			Log.e(TAG, "Fail to read input data.", ex);
		} finally {
			player.stop();
			player.release();
			isRunning.set(false);
		}
	}
}
