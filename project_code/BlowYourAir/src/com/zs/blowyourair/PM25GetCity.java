package com.zs.blowyourair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

		}
	};
	private LocationManager mLocationManager;

	public PM25GetCity(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
	}

	private void getLastKnownLocation() {

		Location passive = this.mLocationManager
				.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		Location network = this.mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location gps = this.mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (passive != null) {
			this.lastKnownLocation = passive;
			Log.d(TAG, "passive last location newer than " + lastKnownLocation);
		}
		if ((network != null)) {
			if (passive == null || (passive.getTime() <= network.getTime())) {
				this.lastKnownLocation = network;
				Log.d(TAG, "network last location newer than "
						+ lastKnownLocation);
			}
		}
		if ((gps != null)) {
			if ((network == null) || (network.getTime() <= gps.getTime())) {
				this.lastKnownLocation = gps;
				Log.d(TAG, "gps last location newer than " + lastKnownLocation);
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

			if (this.mLocationManager
					.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
				Log.d(TAG, "PASSIVE Enabled!");
				this.mLocationManager.requestLocationUpdates(
						LocationManager.PASSIVE_PROVIDER, 1000L, 10.0F,
						this.mListener);
			}
			if (this.mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				Log.d(TAG, "NETWORK Enabled!");
				this.mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000L, 10.0F,
						this.mListener);
			}
			if (this.mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Log.d(TAG, "GPS Enabled!");
				this.mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000L, 10.0F,
						this.mListener);
			} else {
				Log.d(TAG, "No one Enabled!");
			}

			Log.d(TAG, mLocationManager.getProviders(true).toString());
			Log.d(TAG, "requestLocationUpdates...");
		}

	}

	public static abstract interface CityNameStatus {
		public abstract void update(String cityString);
	}
}
