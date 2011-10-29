/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder;

import java.io.IOException;
import java.io.OutputStream;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.ProgressBar;

/**
 * @author horiuchi
 *
 */
public class MicRecordTask extends StopableTask {

	private static final String TAG = "VoiceChangerSample";

	private final int bufferSize;
	private final AudioRecord recorder;
	private final OutputStream rawOutput;

	public MicRecordTask(ProgressBar bar, int sampleRateInHz, int channelConfig, int audioFormat, OutputStream rawOutput) throws IllegalArgumentException {
		super(bar);
		this.rawOutput = rawOutput;
		this.bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat) * 2;
		this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);
	}

	/* (non Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		isRunning.set(true);
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		recorder.startRecording();
		try {
			byte[] buffer = new byte[bufferSize];
			recorder.read(buffer, 0, buffer.length);

			while (isRunning()) {
				int len = recorder.read(buffer, 0, buffer.length);
				try {
					rawOutput.write(buffer, 0, len);
				} catch (IOException ex) {
					Log.w(TAG, "Fail to write cache data.", ex);
				}
				addValue(len);

				if (getMax() <= getValue()) {
					break;
				}
			}

		} finally {
			recorder.stop();
			recorder.release();
			isRunning.set(false);
			if (rawOutput != null) {
				try {
					rawOutput.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
