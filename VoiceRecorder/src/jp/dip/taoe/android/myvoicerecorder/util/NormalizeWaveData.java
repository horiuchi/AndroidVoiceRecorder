package jp.dip.taoe.android.myvoicerecorder.util;

import java.util.Arrays;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

public class NormalizeWaveData {

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
	private static double convertToDouble(byte[] bs, int offset) {
		double d = (bs[offset + 1] << 8) + bs[offset];
		d /= Short.MAX_VALUE;
		return d;
	}

	/**
	 * 周波数データを表示用に、指定された個数に切り詰めます。
	 * @param ds 周波数データ
	 * @param count 表示数
	 * @return 切り詰められたデータ
	 */
	public static double[] convertPlotData(double[] ds, int count) {
		double[] result = new double[count];
		int interval = ds.length / count + (ds.length % count == 0 ? 0 : 1);
		int lastResultIndex = -1;
		for (int index = 0; index < ds.length; index++) {
			double d = ds[index];
			int resultIndex = index / interval;
			if (lastResultIndex < resultIndex) {
				result[resultIndex] = d;
				lastResultIndex = resultIndex;
			} else {
				if (Math.abs(d) > Math.abs(result[resultIndex])) {
					result[resultIndex] = d;
				}
			}
		}
		System.out.println("PlotData count=" + lastResultIndex);
		return result;
	}

	/**
	 * 周波数データをFFTした結果を正規化して返します。
	 * @param ds 周波数データ
	 * @return FFT後のデータ
	 */
	public static double[] convertFFT(double[] ds) {
		if (!FastFourierTransformer.isPowerOf2(ds.length)) {
			ds = convertRequiredArray(ds);
		}
		FastFourierTransformer fft = new FastFourierTransformer();
		Complex[] complexs = fft.transform(ds);
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
		double[] result = new double[getRequiredArray(ds.length)];
		Arrays.fill(result, 0.0);
		System.arraycopy(ds, 0, result, 0, ds.length);
		return result;
	}
	private static int getRequiredArray(int length) {
		int res = length - 1;
		res = (res | (res >> 1));
		res = (res | (res >> 2));
		res = (res | (res >> 4));
		res = (res | (res >> 8));
		res = (res | (res >> 16));
		return res + 1;
	}

}
