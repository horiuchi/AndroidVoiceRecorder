package jp.dip.taoe.android.myvoicerecorder.util;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import android.util.Log;

public class NormalizeWaveData {

	private static final String TAG = "NormalizeWaveData";

	public static void writeShortData(short s, byte[] out, int offset) {
		out[offset] = (byte) s;
		out[offset + 1] = (byte) (s >> 8);
	}
	public static void convertFromDouble(double d, byte[]bs, int offset) {
		d *= Short.MAX_VALUE;
		writeShortData((short) d, bs, offset);
	}

	public static short readShortData(byte[] in, int offset) {
		return (short) ((in[offset + 1] << 8) + (in[offset] & 0xff));
	}
	public static double convertToDouble(byte[] bs, int offset) {
		double d = readShortData(bs, offset);
		d /= Short.MAX_VALUE;
		return d;
	}


	public static byte[] createNoiseData(int sizeByShort) {
		byte[] data = new byte[sizeByShort * 2];
		Random rand = new Random();
		rand.nextBytes(data);
		return data;
	}

	public static byte[] createSineData(int sizeByShort, double frequency) {
		byte[] data = new byte[sizeByShort * 2];
		double t = 0.0;
		double dt = 1.0 / sizeByShort;
		for (int index = 0; index < sizeByShort; index++, t += dt) {
			short s = (short) (Short.MAX_VALUE * Math.sin(2.0 * Math.PI * t * frequency));
			writeShortData(s, data, index * 2);
		}
		return data;
	}

	public static byte[] createSquareData(int sizeByShort, double frequency) {
		byte[] data = new byte[sizeByShort * 2];
		double t = 0.0;
		double dt = 1.0 / sizeByShort;
		for (int index = 0; index < sizeByShort; index++, t += dt) {
			double d = Math.sin(2.0 * Math.PI * t * frequency);
			short s = 0;
			if (d > 0.0) {
				s = Short.MAX_VALUE;
			} else if (d < 0.0) {
				s = -Short.MAX_VALUE;
			}
			writeShortData(s, data, index * 2);
		}
		return data;
	}


	/**
	 * rawデータを、doubleの配列に変換する。<br>
	 * 値は、-1〜1 の範囲に収まるように正規化する。
	 * @param waveData rawデータ
	 * @return 正規化されたデータの配列
	 */
	public static double[] convertWaveData(byte[] waveData) {
		double[] result = new double[waveData.length / 2];
		for (int index = 0; index < result.length; index++) {
			result[index] = convertToDouble(waveData, index * 2);
		}
		return result;
	}

	/**
	 * 周波数データを表示用に、指定された個数に切り詰めます。
	 * @param ds 周波数データ
	 * @param count 表示数
	 * @return 切り詰められたデータ
	 */
	public static double[][] convertPlotData(double[] ds, int count) {
		double[][] result = new double[count][];
		int interval = ds.length / count;
		int remainder = ds.length % count;

		int resultIndex = 0;
		for (int index = 0, counter = 0; index < ds.length; index++, counter++) {
			if (counter >= interval) {
				if (remainder > 0 && counter == interval) {
					remainder--;
				} else {
					resultIndex++;
					counter = 0;
				}
			}
			double d = ds[index];
			if (counter == 0) {
				double[] work = new double[2];
				work[d < 0 ? 1 : 0] = d;
				result[resultIndex] = work;
			} else {
				if (d >= 0 && d > result[resultIndex][0]) {
					result[resultIndex][0] = d;
				} else if (d < 0 && d < result[resultIndex][1]) {
					result[resultIndex][1] = d;
				}
			}
		}
		Log.d(TAG, "converted PlotData count=" + resultIndex);
		return result;
	}

	/**
	 * 周波数データをFFTした結果を正規化して返します。
	 * @param ds 周波数データ
	 * @return FFT後のデータ
	 */
	public static double[] convertFFT(double[] ds) {
		Log.d(TAG, "convert FFT start: " + ds.length);
		if (!FastFourierTransformer.isPowerOf2(ds.length)) {
			ds = convertRequiredArray(ds);
		}
		FastFourierTransformer fft = new FastFourierTransformer();
		Log.d(TAG, "calculate FFT start: " + ds.length);
		Complex[] complexs = fft.transform(ds);
		Log.d(TAG, "convert FFT end.");
		return normalizeArray(complexs);
	}
	private static double[] normalizeArray(Complex[] cs) {
		double[] result = new double[cs.length];
		double max = 0.0;
		for (int index = 0; index < result.length; index++) {
			result[index] = cs[index].abs();
			if (max < result[index]) {
				max = result[index];
			}
		}
		for (int index = 0; index < result.length; index++) {
			result[index] /= max;
		}
		return result;
	}
	private static double[] convertRequiredArray(double[] ds) {
		double[] result = new double[getRequiredLength(ds.length)];
		Arrays.fill(result, 0.0);
		System.arraycopy(ds, 0, result, 0, ds.length);
		return result;
	}
	private static int getRequiredLength(int length) {
		int res = length - 1;
		res = (res | (res >> 1));
		res = (res | (res >> 2));
		res = (res | (res >> 4));
		res = (res | (res >> 8));
		res = (res | (res >> 16));
		return res + 1;
	}


	public static byte[] normalizeWaveData(byte[] bs) {
		return bs;
	}
	/*
	public static byte[] normalizeWaveData(byte[] bs) {
		byte[] result = new byte[bs.length];
		for (int index = 0; index < bs.length / 2; index++) {
			double d = convertToDouble(bs, index * 2);
			convertFromDouble(normalize(d), result, index * 2);
		}
		return result;
	}
	private static double normalize(double d) {
		double x = Math.abs(d);
		if (x <= 0) return 0.0;
		return Math.signum(d) * Math.sqrt(x);
	}
	*/
}
