/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder.util;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

/**
 * @author horiuchihiroki
 *
 */
public class NormalizeWaveDataTest extends TestCase {

	public void testConvertPlotData1() {
		double[] ds = new double[20];
		Arrays.fill(ds, 1.0);
		{
			System.out.println(20/7);
			System.out.println(19/3);
			double[] data = NormalizeWaveData.convertPlotData(ds, 7);
			assertEquals(7, data.length);
			assertEquals(1.0, data[0]);
			assertEquals(1.0, data[6]);
		}
		{
			double[] data = NormalizeWaveData.convertPlotData(ds, 6);
			assertEquals(6, data.length);
		}
	}

	public void testConvertPlotData2() {
		double[] ds = new double[800];
		Random random = new Random(0);
		for (int index = 0; index < ds.length; index++) {
			ds[index] = random.nextDouble();
		}

		{
			double[] data = NormalizeWaveData.convertPlotData(ds, 150);
			assertEquals(150, data.length);
		}
	}

}
