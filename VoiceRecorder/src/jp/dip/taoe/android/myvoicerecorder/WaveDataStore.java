package jp.dip.taoe.android.myvoicerecorder;

public interface WaveDataStore {

	public abstract byte[] getAllWaveData();

	public abstract void addWaveData(byte[] data);
	public abstract void addWaveData(byte[] data, int offset, int length);
	public abstract void closeWaveData();

	public abstract void clearWaveData();

}
