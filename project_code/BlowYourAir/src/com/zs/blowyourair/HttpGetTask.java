package com.zs.blowyourair;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

public class HttpGetTask extends AsyncTask<String, Integer, String>{
	
	private static final String TAG = "BlowYourAir";
	private String mURI;
	
	public HttpGetTask(String uri) {
		// TODO Auto-generated constructor stub
		mURI = uri;
	}

	@Override
	protected String  doInBackground(String...strings) {
		// TODO Auto-generated method stub
		return HttpRequest(String.format(mURI, strings[0]));
//		sprintf("http://maps.googleapis.com/maps/api/geocode/json?latlng=%s&sensor=true ", strs[0]);
	}

	public String HttpRequest(String uri){
		
		Log.d(TAG, "HttpRequest get uri: " + uri);
		HttpClient mHttpClent = new DefaultHttpClient();		
		HttpGet httpGet = new HttpGet(uri);
		try {
			HttpResponse httpResponse = mHttpClent.execute(httpGet);
			Log.d(TAG, "get StatusCode: " + httpResponse.getStatusLine().getStatusCode());			
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
