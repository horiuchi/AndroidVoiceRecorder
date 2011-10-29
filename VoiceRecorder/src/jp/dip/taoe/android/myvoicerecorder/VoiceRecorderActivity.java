package jp.dip.taoe.android.myvoicerecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.dip.taoe.android.myvoicerecorder.util.NormalizeWaveData;
import jp.dip.taoe.android.myvoicerecorder.util.WaveFileHeaderCreator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

public class VoiceRecorderActivity extends Activity {

	private static final String TAG = "VoiceChangerSample";

	private static final int SAMPLE_RATE = 8000;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private MicRecordTask recordTask;
	private AudioPlayTask playTask;
	private AlertDialog saveDialog;

	private WaveDisplayView displayView;
	private ProgressBar progressBar;
	private Button recordButton;
	private Button playButton;
	private Button stopButton;
	private Button saveButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.d(TAG, "Start.");
		LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayView);
		displayView = new WaveDisplayView(getBaseContext());
		displayLayout.addView(displayView);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		recordButton = (Button) findViewById(R.id.Record);
		playButton = (Button) findViewById(R.id.Play);
		stopButton = (Button) findViewById(R.id.Stop);
		saveButton = (Button) findViewById(R.id.Save);

		configureEvnetListener();
		setInitializeState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean ret = super.onCreateOptionsMenu(menu);
		int index = Menu.FIRST;
		menu.add(Menu.NONE, index++, Menu.NONE, "データクリア");
		menu.add(Menu.NONE, index++, Menu.NONE, "ノイズを追加");
		menu.add(Menu.NONE, index++, Menu.NONE, "サインを追加");
		menu.add(Menu.NONE, index++, Menu.NONE, "矩形を追加");
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int size = 8000;
		final int freq = 440;
		switch (item.getItemId()) {
		case Menu.FIRST:
			displayView.clearWaveData();
			break;
		case Menu.FIRST + 1:
			displayView.addWaveData(NormalizeWaveData.createNoiseData(size));
			break;
		case Menu.FIRST + 2:
			displayView.addWaveData(NormalizeWaveData.createSineData(size, freq));
			break;
		case Menu.FIRST + 3:
			displayView.addWaveData(NormalizeWaveData.createSquareData(size, freq));
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		if (stopButton.isEnabled()) {
			stopAll();
		}
		super.onPause();
	}

	private void configureEvnetListener() {
		recordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecording();
			}
		});

		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startPlaying();
			}
		});

		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopAll();
			}
		});

		saveDialog = createSaveDialog();
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveDialog.show();
			}
		});
	}
	private void setInitializeState() {
		recordButton.setEnabled(true);
		playButton.setEnabled(true);
		stopButton.setEnabled(false);
		saveButton.setEnabled(true);
	}

	private AlertDialog createSaveDialog() {
		final Handler handler = new Handler();
		final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
		return new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_save_title)
			.setView(view)
			.setPositiveButton(R.string.dialog_save_button_save, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText filename = (EditText) view.findViewById(R.id.filenameEditText);
					RadioButton wavRadio = (RadioButton) view.findViewById(R.id.wavRadio);

					boolean isWavFile = wavRadio.isChecked();
					final File file = new File(getSavePath(), filename.getText() + (isWavFile ? ".wav" : ".raw"));
					saveSoundFile(file, isWavFile);

					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(VoiceRecorderActivity.this, "Save completed: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
						}
					});
				}
			})
			.setNegativeButton(R.string.dialog_save_button_cancel, null)
			.create();
	}

	private boolean saveSoundFile(File savefile, boolean isWavFile) {
		byte[] data = displayView.getAllWaveData();
		if (data.length == 0) {
			Log.w(TAG, "save data is not found.");
			return false;
		}

		try {
			savefile.createNewFile();

			FileOutputStream targetStream = new FileOutputStream(savefile);
			try {
				if (isWavFile) {
					WaveFileHeaderCreator.pushWaveHeader(targetStream, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING, data.length);
				}
				targetStream.write(data);
			} finally {
				if (targetStream != null) {
					targetStream.close();
				}
			}
			return true;
		} catch (IOException ex) {
			Log.w(TAG, "Fail to save sound file.", ex);
			return false;
		}
	}


	private void startRecording() {
		Log.i(TAG, "start recording.");
		setButtonEnable(true);
		try {
			recordTask = new MicRecordTask(progressBar, displayView, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
			recordTask.setMax(10 * getDataBytesPerSecond(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING));
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Fail to create MicRecordTask.", ex);
		}
		recordTask.start();
		waitEndTask(recordTask);
	}

	private void stopRecording() {
		stopTask(recordTask);
		Log.i(TAG, "stop recording.");
	}

	private void startPlaying() {
		Log.i(TAG, "start playing.");

		setButtonEnable(true);
		try {
			playTask = new AudioPlayTask(progressBar, displayView, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Fail to create MicRecordTask.", ex);
		}
		playTask.start();
		waitEndTask(playTask);
	}

	private void stopPlaying() {
		stopTask(playTask);
		Log.i(TAG, "stop playing.");
	}

	private void stopTask(StopableTask task) {
		if (task.stopTask()) {
			try {
				task.join(1000);
			} catch (InterruptedException e) {
				Log.w(TAG, "Interrupted recoring thread stopping.");
			}
		}
		setButtonEnable(false);
	}


	private void stopAll() {
		if (recordTask != null && recordTask.isRunning()) {
			stopRecording();
		}
		if (playTask != null && playTask.isRunning()) {
			stopPlaying();
		}
	}

	private void setButtonEnable(boolean b) {
		recordButton.setEnabled(!b);
		playButton.setEnabled(!b);
		stopButton.setEnabled(b);
		saveButton.setEnabled(!b && hasSDCard());
	}


	private void waitEndTask(final Thread t) {
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					t.join();
				} catch (InterruptedException e) {
				}

				handler.post(new Runnable() {
					@Override
					public void run() {
						setButtonEnable(false);
					}
				});
			}
		}).start();
	}


	private File getCacheFile() {
		return new File(getSavePath(), "cache.raw");
	}

	private File getSavePath() {
		if (hasSDCard()) {
			File path = new File(Environment.getExternalStorageDirectory(), "/VoiceChanger/");
			path.mkdirs();
			return path;
		} else {
			Log.i(TAG, "SDCard is unuseable: " + Environment.getExternalStorageState());
			return getFilesDir();
		}
	}

	private boolean hasSDCard() {
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}


	private int getDataBytesPerSecond(int sampleRate, int channelConfig, int audioEncoding) {
		boolean is8bit = audioEncoding == AudioFormat.ENCODING_PCM_8BIT;
		boolean isMonoChannel = channelConfig != AudioFormat.CHANNEL_CONFIGURATION_STEREO;
		return sampleRate * (isMonoChannel ? 1: 2) * (is8bit ? 1: 2);
	}
}
