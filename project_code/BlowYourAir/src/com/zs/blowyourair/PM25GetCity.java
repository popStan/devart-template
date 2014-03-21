package com.zs.blowyourair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zs.blowyourair.BlowYourAir.MyHandler;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class PM25GetCity {

	private static final String TAG = "BlowYourAir";
	private Location lastKnownLocation;
	private Context mContext;
	private boolean isGPSRegist = false;
	private boolean isNWRegist = false;
	private static PM25GetCity instance = null;
	private LocationListener mListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

			Log.d(TAG, "onStatusChanged : provider=" + provider + " status="
					+ status + "extras=" + extras);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderEnabled, provider=" + provider);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProviderDisabled, provider=" + provider);

		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onLocationChanged, location=" + location.toString());
			lastKnownLocation = location;
			new BlowYourAir().onLocationChanged();
		}
	};
	private LocationManager mLocationManager;

	public PM25GetCity(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public static PM25GetCity getInstance(Context context) {
		if (instance == null) {
			instance = new PM25GetCity(context);
		}
		return instance;
	}

	private void getLastKnownLocation() {

		Location network = this.mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location gps = this.mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if ((network != null)) {
			this.lastKnownLocation = network;
			Log.d(TAG, "network last location newer than " + lastKnownLocation);
			if (isNWRegist) {
				this.mLocationManager.removeUpdates(mListener);
			}

		}
		if ((gps != null)) {
			if ((network == null) || (network.getTime() <= gps.getTime())) {
				this.lastKnownLocation = gps;
				Log.d(TAG, "gps last location newer than " + lastKnownLocation);
			}
			if (isGPSRegist) {
				this.mLocationManager.removeUpdates(mListener);
			}
		}
	}

	public void checkLocationService() {
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this.mContext,
					"Better use GPS to get accurate location",
					Toast.LENGTH_SHORT).show();
		}

		if ((!mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				&& (!mLocationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)))
			Toast.makeText(this.mContext, "Please enable your network or GPS",
					Toast.LENGTH_SHORT).show();
	}

	public void unregistListener() {
			mLocationManager.removeUpdates(mListener);
			isGPSRegist = false;
			isNWRegist = false;
	}
	
	public void requestCityName(final CityNameStatus cityNameStatus) {
		Log.d(TAG, "getLastKnownLocation...");
		getLastKnownLocation();
		if (this.lastKnownLocation != null) {

			Log.d(TAG, "LastKnownLocation != null...");
			HttpGetTask task = new HttpGetTask(
					"http://maps.googleapis.com/maps/api/geocode/json?latlng=%s&sensor=true") {
				protected void onPostExecute(String result) {
					Log.d(TAG, "onPostExecute");
					String city = null;
					if (result != null && result.contains("OK")) {
						try {
							JSONArray jsonArray = new JSONObject(result)
									.getJSONArray("results").getJSONObject(0)
									.getJSONArray("address_components");
							for (int i = 0; i < jsonArray.length(); ++i) {
								JSONObject jsonObject = jsonArray
										.getJSONObject(i);
								String types = jsonObject.getJSONArray("types")
										.toString();
								if ((types.contains("locality"))
										&& (types.contains("political"))
										&& (!types.contains("sublocality"))) {
									Log.d(TAG, jsonObject.toString());
									city = jsonObject.getString("short_name");
									Log.d(TAG, city);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							mLocationManager.removeUpdates(mListener);
						}
					}
					cityNameStatus.update(city);
				}

				protected void onPreExecute() {
					Log.d(TAG, "onPreExecute");
				};
			};

			Object[] objects = new Object[2];
			objects[0] = Double.valueOf(lastKnownLocation.getLatitude());
			objects[1] = Double.valueOf(lastKnownLocation.getLongitude());
			Log.i(TAG, lastKnownLocation.getLatitude() + " : "
					+ lastKnownLocation.getLongitude());
			task.execute(String.format("%s,%s", objects));
		} else {
			Log.d(TAG, "LastKnownLocation == null...");

			if (!isNWRegist) {
				if (this.mLocationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					Log.d(TAG, "NETWORK Enabled!");
					this.mLocationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER, 5000, 0,
							this.mListener);
					Log.d(TAG, "requestLocationUpdates by NETWORK.");
					isNWRegist = true;
				}
			} else {
				Log.d(TAG, "already registed NETWORK.");
			}
			if (!isGPSRegist) {
				if (this.mLocationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					Log.d(TAG, "GPS Enabled!");
					this.mLocationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, 5000, 0,
							this.mListener);
					Log.d(TAG, "requestLocationUpdates by GPS.");
					isGPSRegist = true;
				}
			} else {
				Log.d(TAG, "already registed GPS.");
			}

			Log.d(TAG, mLocationManager.getProviders(true).toString());
			cityNameStatus.update(null);
		}

	}

	public static abstract interface CityNameStatus {
		public abstract void update(String cityString);
	}
}
