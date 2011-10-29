/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.ProgressBar;

/**
 * @author horiuchi
 *
 */
public class MicRecordTask extends StopableTask {

	private final WaveDataStore store;
	private final int bufferSize;
	private final AudioRecord recorder;

	public MicRecordTask(ProgressBar bar, WaveDataStore store, int sampleRateInHz, int channelConfig, int audioFormat) throws IllegalArgumentException {
		super(bar);
		this.store = store;
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

		store.clearWaveData();
		recorder.startRecording();
		try {
			byte[] buffer = new byte[bufferSize];
			recorder.read(buffer, 0, buffer.length);

			while (isRunning()) {
				int len = recorder.read(buffer, 0, buffer.length);
				store.addWaveData(buffer, 0, len);
				addValue(len);

				if (getMax() <= getValue()) {
					break;
				}
			}
			store.closeWaveData();

		} finally {
			recorder.stop();
			recorder.release();
			isRunning.set(false);
		}
	}
}
