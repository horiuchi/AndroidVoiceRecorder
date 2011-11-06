/**
 *
 */
package jp.dip.taoe.android.myvoicerecorder.util;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author horiuchihiroki
 *
 */
public class NormalizeWaveDataTest extends TestCase {

	public void testConvertShort2ByteArray1() {
		byte[] bs = new byte[20];
		short s = 0;

		NormalizeWaveData.writeShortData(s, bs, 0);
		assertEquals(0, bs[0]);
		assertEquals(0, bs[1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, 0));
	}

	public void testConvertShort2ByteArray2() {
		byte[] bs = new byte[20];
		short s = 1;
		int offset = 2;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals(1, bs[offset]);
		assertEquals(0, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray3() {
		byte[] bs = new byte[20];
		short s = -1;
		int offset = 4;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals((byte)0xff, bs[offset]);
		assertEquals((byte)0xff, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray4() {
		byte[] bs = new byte[20];
		short s = 100;
		int offset = 6;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals(100, bs[offset]);
		assertEquals(0, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray5() {
		byte[] bs = new byte[20];
		short s = 10000;
		int offset = 8;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals(0x10, bs[offset]);
		assertEquals(0x27, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray10() {
		byte[] bs = new byte[20];
		short s = Short.MAX_VALUE;
		int offset = 18;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals((byte)0xff, bs[offset]);
		assertEquals((byte)0x7f, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray11() {
		byte[] bs = new byte[20];
		short s = -Short.MAX_VALUE;
		int offset = 16;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals((byte)0x01, bs[offset]);
		assertEquals((byte)0x80, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}

	public void testConvertShort2ByteArray12() {
		byte[] bs = new byte[20];
		short s = Short.MIN_VALUE;
		int offset = 14;

		NormalizeWaveData.writeShortData(s, bs, offset);
		assertEquals((byte)0x00, bs[offset]);
		assertEquals((byte)0x80, bs[offset + 1]);
		assertEquals(s, NormalizeWaveData.readShortData(bs, offset));
	}


	public void testConvertDouble2ByteArray1() {
		byte[] bs = new byte[10];
		double d = 1.0;
		int offset = 0;

		NormalizeWaveData.convertFromDouble(d, bs, offset);
		assertEquals(d, NormalizeWaveData.convertToDouble(bs, offset));
	}

	public void testConvertDouble2ByteArray2() {
		byte[] bs = new byte[10];
		double d = -1.0;
		int offset = 0;

		NormalizeWaveData.convertFromDouble(d, bs, offset);
		assertEquals(d, NormalizeWaveData.convertToDouble(bs, offset));
	}


	public void testConvertPlotData1() {
		double[] ds = new double[20];
		Arrays.fill(ds, 1.0);
		{
			System.out.println(20/7);
			System.out.println(19/3);
			double[][] data = NormalizeWaveData.convertPlotData(ds, 7);
			assertEquals(7, data.length);
			assertEquals(1.0, data[0][0]);
			assertEquals(0.0, data[0][1]);
			assertEquals(1.0, data[6][0]);
			assertEquals(0.0, data[6][1]);
		}
		{
			double[][] data = NormalizeWaveData.convertPlotData(ds, 6);
			assertEquals(6, data.length);
			assertEquals(1.0, data[0][0]);
			assertEquals(0.0, data[0][1]);
			assertEquals(1.0, data[5][0]);
			assertEquals(0.0, data[5][1]);
		}
	}

	public void testConvertPlotData2() {
		double[] ds = new double[800];
		Arrays.fill(ds, -1.0);
		{
			double[][] data = NormalizeWaveData.convertPlotData(ds, 153);
			assertEquals(153, data.length);
			assertEquals(0.0, data[0][0]);
			assertEquals(-1.0, data[0][1]);
			assertEquals(-1.0, data[100][1]);
			assertEquals(-1.0, data[152][1]);
		}
		{
			double[][] data = NormalizeWaveData.convertPlotData(ds, 77);
			assertEquals(77, data.length);
			assertEquals(0.0, data[0][0]);
			assertEquals(-1.0, data[0][1]);
			assertEquals(-1.0, data[10][1]);
			assertEquals(-1.0, data[76][1]);
		}
	}

}
