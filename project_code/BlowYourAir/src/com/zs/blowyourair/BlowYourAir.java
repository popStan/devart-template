package com.zs.blowyourair;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zs.blowyourair.PM25GetAqi.PM25;

public class BlowYourAir extends Activity {

	private static final String TAG = "BlowYourAir";

	private Button bt1;
	// private Button bt2;
	// private Button bt3;
	// private Button bt4;
	private TextView text1;
	private TextView text2;
	// private TextView text3;
	private String mCity = null;
	private PM25GetAqi.PM25 mPM25 = null;
	static int blow_count = 0;
	RecordThread recordThread = null;
	public static final int UPDATE_UI = 0;
	public static final int START_RECORD = 1;
	public static final int STOP_RECORD = 2;
	public static final int GET_CITY_OK = 3;

	private void checkAccessLocation() {
		Log.d(TAG, "Check Accessibility");
		new PM25GetCity(this).checkLocationService();
	}

	private void clearVar(String state) {
		mPM25 = null;
		mCity = null;
		blow_count = 0;
		text2.setText("PM2.5: ");
		if (state.equals("start")) 	{
			text1.setText("Your Breathing:");
		} else if (state.equals("stop")) {
			text1.setText("No input.");
		}
	}

	private void getCity() {
		if (NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE) {
			Toast.makeText(this, "network_none", Toast.LENGTH_SHORT).show();
			return;
		}
		Log.d(TAG, "about to getcity....");
		new PM25GetCity(this).requestCityName(new PM25GetCity.CityNameStatus() {
			public void update(String city) {
				if (city == null) {// 未定位到城市
					Log.d(TAG, "Get city name error");
					return;
				}
				mCity = city;
				myHandler.sendMessage(myHandler.obtainMessage(GET_CITY_OK));
				// text1.setText("City:" + city);
				Log.d(TAG, "geo coder get city name:" + city);
			}
		});

	}

	private void getPM25() {
		if (NetworkUtils.getNetworkState(this) == NetworkUtils.NETWORN_NONE) {
			Toast.makeText(this, "network_none", Toast.LENGTH_SHORT).show();
			return;
		}

		if (mCity == null) {
			Log.e(TAG, "City is null");
			return;
		}

		Log.d(TAG, "about to getpm25....");
		new PM25GetAqi().request(new PM25GetAqi.PM25Info() {

			@Override
			public void onInfo(PM25 pm25) {
				// TODO Auto-generated method stub
				if (pm25.aqi == null) {
					Log.d(TAG, "Get PM25 error...");
					// text2.setText("PM25:null");
					return;
				}
				mPM25 = pm25;
				// text2.setText("PM25:" + mPM25.aqi);
			}
		}, mCity);

	}

	class MyHandler extends Handler {

	}

	byte[] buffer = new byte[100];
	MyHandler myHandler = new MyHandler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case START_RECORD:
				recordThread.run();
				myHandler.sendMessageDelayed(
						myHandler.obtainMessage(START_RECORD), 8);
				break;
			case STOP_RECORD:
				myHandler.removeMessages(START_RECORD);
				if (recordThread != null) {
					recordThread.stop();
				}
				bt1.setText("Click to Blow");
				if (mPM25 != null && mPM25.aqi != null) {
					text2.setText("PM25:" + mPM25.aqi);
				} else {
					text2.setText("PM25: Fetch data error");
				}
				break;

			case GET_CITY_OK:
				getPM25();
				break;
			case UPDATE_UI:
				text1.setText("Your Breathing:" + blow_count);
				break;
			default:
				break;
			}
			super.handleMessage(msg); // 接收到message后更新UI，并通过isblow停止线程

			// Parameter.isblow=false;

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blow_your_air);

		checkAccessLocation();

		bt1 = (Button) findViewById(R.id.bt1);
		// bt2 = (Button) findViewById(R.id.bt2);
		// bt3 = (Button) findViewById(R.id.bt3);
		// bt4 = (Button) findViewById(R.id.bt4);
		BtListener btlistener = new BtListener();
		text1 = (TextView) findViewById(R.id.text1);
		text2 = (TextView) findViewById(R.id.text2);
		// text3 = (TextView) findViewById(R.id.text3);
		bt1.setOnClickListener(btlistener);
		// bt2.setOnClickListener(btlistener);
		// bt3.setOnClickListener(btlistener);
		// bt4.setOnClickListener(btlistener);

	}

	class BtListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// start thread
			switch (v.getId()) {
			case R.id.bt1:
				Log.d(TAG, "on click bt1");
				getCity();
//				getPM25();
				if (recordThread == null) {
					recordThread = new RecordThread(myHandler);
				}
				clearVar("start");
				bt1.setText("Recording...");
				recordThread.prepare();
				myHandler.removeMessages(START_RECORD);
				myHandler.sendMessage(myHandler.obtainMessage(START_RECORD));
				break;
			// case R.id.bt2:
			// Log.d(TAG, "on click bt2");
			// getPM25();
			// break;
			// case R.id.bt3:
			// Log.d(TAG, "on click bt3");
			// if (recordThread == null) {
			// recordThread = new RecordThread(myHandler);
			// }
			// bt3.setText("Recording...");
			// recordThread.prepare();
			// myHandler.removeMessages(START_RECORD);
			// myHandler.sendMessage(myHandler.obtainMessage(START_RECORD));
			// break;
			//
			// case R.id.bt4:
			// Log.d(TAG, "on click b4");
			// Log.d(TAG, "Sto record....");
			// myHandler.removeMessages(START_RECORD);
			// if (recordThread != null) {
			// recordThread.stop();
			// }
			// break;
			default:
				break;
			}

		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		myHandler.removeMessages(START_RECORD);
		if (recordThread != null) {
			recordThread.stop();
		}
		clearVar("stop");
	}

	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstTime < 2000) {
			finish();
		} else {
			firstTime = System.currentTimeMillis();
			Toast.makeText(this, "Press again to EXIT.", Toast.LENGTH_SHORT)
					.show();
		}
	}

}
