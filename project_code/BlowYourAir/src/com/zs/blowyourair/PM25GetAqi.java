package com.zs.blowyourair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PM25GetAqi {
	private static final String TAG = "BlowYourAir";

	public void request(final PM25Info pm25info, String city) {
		HttpGetTask task = new HttpGetTask(
				//新key:ktCqhMgEtdzzo83ZsV3n OK~~~
				"http://www.pm25.in/api/querys/aqi_details.json?token=ktCqhMgEtdzzo83ZsV3n&city=%s&stations=no") {
			protected void onPostExecute(String result) {
				PM25 pm25 = new PM25();
				if (result != null && !result.contains("error")) {
					Log.d(TAG, result);
					try {
						JSONArray localJSONArray = new JSONArray(result);
						JSONObject localJSONObject = localJSONArray
								.getJSONObject(0);
						pm25.aqi = localJSONObject.optString("aqi");
						pm25.area = localJSONObject.optString("area");
						pm25.pm2_5 = localJSONObject.optString("pm2_5");
						pm25.pm10 = localJSONObject.optString("pm10");
						pm25.position_name = localJSONObject
								.optString("position_name");
						pm25.primary_pollutant = localJSONObject
								.optString("primary_pollutant");
						pm25.quality = localJSONObject.optString("quality");
						pm25.time_point = localJSONObject
								.optString("time_point");

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				pm25info.onInfo(pm25);
			}
		};
		task.execute(new String[] { city.toLowerCase() });
	}

	public static class PM25 {
		public String aqi;
		public String area;
		public String pm10;
		public String pm2_5;
		public String position_name;
		public String primary_pollutant;
		public String quality;
		public String time_point;

	}

	public static abstract interface PM25Info {
		public abstract void onInfo(PM25 pm25);
	}
}
