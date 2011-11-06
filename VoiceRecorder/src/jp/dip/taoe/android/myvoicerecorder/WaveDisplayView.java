/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder;

import java.io.ByteArrayOutputStream;

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
	private final ByteArrayOutputStream waveData = new ByteArrayOutputStream(8000 * 2 * 10);

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
		int height = this.getHeight() - margin * 2;

		double[] ds = NormalizeWaveData.convertWaveData(bs);
		{
			double[][] plots = NormalizeWaveData.convertPlotData(ds, width);
			float lastY = height / 2.0f;
			boolean isLastPlus = true;
			for (int x = 0; x < width; x++) {
				boolean wValue = plots[x][0] > 0.0 && plots[x][1] < 0.0;
				if (wValue) {
					double[] values = isLastPlus ?
							new double[] { plots[x][1], plots[x][0] } :
							new double[] { plots[x][0], plots[x][1] };
					for (double d : values) {
						lastY = drawWaveLine(canvas, d, x, lastY, height, margin);
					}
				} else {
					double value = 0.0;
					if (plots[x][1] < 0.0) {
						value = plots[x][1];
						isLastPlus = false;
					} else {
						value = plots[x][0];
						isLastPlus = true;
					}
					lastY = drawWaveLine(canvas, value, x, lastY, height, margin);
				}
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

	private float drawWaveLine(Canvas canvas, double value, float x, float y, int height, int margin) {
		float nextY = height * -1 * (float)(value - 1.0) / 2.0f;
		canvas.drawLine(x + margin, y + margin, x+1 + margin, nextY + margin, waveBaseLine);
		return nextY;
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

	/* (non-Javadoc)
	 * @see jp.dip.taoe.android.myvoicerecorder.WaveDataStore#addWaveData(byte[], int, int)
	 */
	@Override
	public void addWaveData(byte[] data, int offset, int length) {
		waveData.write(data, offset, length);
		fireInvalidate();
	}

	/* (non-Javadoc)
	 * @see jp.dip.taoe.android.myvoicerecorder.WaveDataStore#closeWaveData()
	 */
	@Override
	public void closeWaveData() {
		byte[] bs = waveData.toByteArray();
		byte[] data = NormalizeWaveData.normalizeWaveData(bs);
		waveData.reset();
		addWaveData(data);
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

}
