package com.zs.blowyourair;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.zs.blowyourair.BolwYourAir.MyHandler;

public class RecordThread {

	private static final String TAG = "BlowYourAir";
	public static final int UPDATE_UI = 1;
	private AudioRecord ar;
	private int bs = 100;
	private static int SAMPLE_RATE_IN_HZ = 8000;
	// private Message msg;
	private int number = 1;
	private int tal = 1;
	private MyHandler handler;
	private long currenttime;
	private long endtime;
	private long time = 1;
	private byte[] buffer;

	// 到达该值之后 触发事件
	private static int BLOW_ACTIVI = 2700;

	public RecordThread(MyHandler myHandler) {
		super();
		bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		Log.d(TAG, "get minbuffer size....bs: " + bs);

		ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bs);
		Log.d(TAG, "new audiorecord....");
		handler = myHandler;
	}

	public void run() {
		Log.d(TAG, "[run]start recording....");
		number++;
		currenttime = System.currentTimeMillis();
		int r = ar.read(buffer, 0, bs) + 1;
		int v = 0;
		for (int i = 0; i < buffer.length; i++) {
			v += (buffer[i] * buffer[i]);
		}
		int value = Integer.valueOf(v / (int) r);
		tal = tal + value;
		endtime = System.currentTimeMillis();
		time = time + (endtime - currenttime);

		if (time >= 300 || number > 5) {

			int total = tal / number;
//			Log.d(TAG, "[run]tal:" + tal + " number:" + number + " total:"
//					+ total + " r:" + r + " v:" + v);
			if (total > BLOW_ACTIVI) {

				// 利用传入的handler 给界面发送通知
				BolwYourAir.blow_count += 1;
				handler.sendEmptyMessage(UPDATE_UI);
				number = 1;
				tal = 1;
				time = 1;
			}
		}
	}

	public void prepare() {
		buffer = new byte[bs];
		ar.startRecording();
	}

	public void stop() {
		Log.d(TAG, "[run] ar stop & release~~");
		ar.stop();
	}
}