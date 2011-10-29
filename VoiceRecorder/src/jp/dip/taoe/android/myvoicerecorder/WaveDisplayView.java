/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import jp.dip.taoe.android.myvoicerecorder.util.NormalizeWaveData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

/**
 * @author horiuchihiroki
 *
 */
public class WaveDisplayView extends View implements WaveDataStore {

	private final Handler handler;
	private final ByteArrayOutputStream waveData = new ByteArrayOutputStream(8000 * 10);

	private final Paint waveBaseLine = new Paint();
	private final Paint fftDataLine = new Paint();

	/**
	 * @param context
	 */
	public WaveDisplayView(Context context) {
		super(context);
		handler = new Handler();

		waveBaseLine.setARGB(255, 128, 255, 128);
		waveBaseLine.setStyle(Paint.Style.STROKE);
		waveBaseLine.setStrokeWidth(1.0f);
		waveBaseLine.setStrokeCap(Paint.Cap.ROUND);

		fftDataLine.setARGB(128, 128, 128, 255);
		fftDataLine.setStyle(Paint.Style.STROKE);
		fftDataLine.setStrokeWidth(1.0f);
		fftDataLine.setStrokeCap(Paint.Cap.ROUND);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		byte[] bs = waveData.toByteArray();
		if (bs.length == 0) {
			return;
		}

		final int margin = 2;
		int width = this.getWidth() - margin * 2;
		int height = this.getHeight();

		double[] ds = NormalizeWaveData.convertWaveData(bs);
		{
			double[] plots = NormalizeWaveData.convertPlotData(ds, width);
			float lastY = height / 2.0f;
			for (int x = 0; x < width; x++) {
				float y = height * -1 * (float)(plots[x] - 1.0) / 2.0f;
				canvas.drawLine(x + margin, lastY, x+1 + margin, y, waveBaseLine);
				lastY = y;
			}
		}
		/*{
			double[] ffts = NormalizeWaveData.convertFFT(ds);
			float lastY = 0.0f;
			for (int x = 0; x < width; x++) {
				float y = height * (float)(1.0 - ffts[x]);
				canvas.drawLine(x + margin, lastY, x+1 + margin, y, fftDataLine);
				lastY = y;
			}
		}*/
	}

	/* (non-Javadoc)
	 * @see jp.dip.taoe.android.myvoicerecorder.WaveDataStore#getAllWaveData()
	 */
	@Override
	public byte[] getAllWaveData() {
		return waveData.toByteArray();
	}

	/* (non-Javadoc)
	 * @see jp.dip.taoe.android.myvoicerecorder.WaveDataStore#addWaveData(byte[])
	 */
	@Override
	public void addWaveData(byte[] data) {
		addWaveData(data, 0, data.length);
	}

	@Override
	public void addWaveData(byte[] data, int offset, int length) {
		waveData.write(data, offset, length);
		fireInvalidate();
	}

	/* (non-Javadoc)
	 * @see jp.dip.taoe.android.myvoicerecorder.WaveDataStore#clearWaveData()
	 */
	@Override
	public void clearWaveData() {
		waveData.reset();
		fireInvalidate();
	}

	private void fireInvalidate() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				WaveDisplayView.this.invalidate();
			}
		});
	}


	private static final int DATA_SIZE = 8000;

	public void addNoizeData() {
		byte[] data = new byte[DATA_SIZE * 2];
		Random rand = new Random();
		rand.nextBytes(data);
		addWaveData(data);
	}

	public void addSineData() {
		byte[] data = new byte[DATA_SIZE * 2];

		final double freq = 440;
		double t = 0.0;
		double dt = 1.0 / DATA_SIZE;
		for (int index = 0; index < DATA_SIZE; index++, t += dt) {
			short s = (short) (Short.MAX_VALUE * Math.sin(2.0 * Math.PI * t * freq));
			NormalizeWaveData.writeShortData(s, data, index * 2);
		}

		addWaveData(data);
	}

	public void addSquareData() {
		byte[] data = new byte[DATA_SIZE * 2];

		final double freq = 440;
		double t = 0.0;
		double dt = 1.0 / DATA_SIZE;
		for (int index = 0; index < DATA_SIZE; index++, t += dt) {
			double d = Math.sin(2.0 * Math.PI * t * freq);
			short s = 0;
			if (d > 0.0) {
				s = Short.MAX_VALUE;
			} else if (d < 0.0) {
				s = -Short.MAX_VALUE;
			}
			NormalizeWaveData.writeShortData(s, data, index * 2);
		}

		addWaveData(data);
	}
}
