package app.arcanum.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import app.arcanum.AppSettings;
import app.arcanum.tasks.contracts.MessageRequest;
import app.arcanum.tasks.contracts.MessageResponse;

import com.google.gson.Gson;

public class HttpGetMessagesTask extends AsyncTask<MessageRequest, Void, MessageResponse[]> {
	private static final String TAG = "HttpGetMessagesTask";
	
	@Override
	protected MessageResponse[] doInBackground(MessageRequest... requests) {
		MessageResponse[] result = null;
		
		if(requests.length <= 0)
			return result;
		
		try {
	        HttpClient client = new DefaultHttpClient();
	        
	        HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.GET_MESSAGE);
			post.addHeader("Connection", 	"Keep-Alive");
			post.addHeader("Content-Type", 	"application/json; charset=utf-8");
			post.addHeader("Accept",		"application/json; charset=utf-8");
			post.addHeader("Accept-Charset","utf-8");
	        
			Gson gson = new Gson();
			String request_string = gson.toJson(requests[0]);
			StringEntity entity = new StringEntity(request_string);
        	post.setEntity(entity);
			
            HttpResponse response = client.execute(post);
            
        	StatusLine statusLine = response.getStatusLine();
	        if(statusLine.getStatusCode() == java.net.HttpURLConnection.HTTP_OK){
	            byte[] result_bytes = EntityUtils.toByteArray(response.getEntity());
	            String result_string = new String(result_bytes, "UTF-8");
	            result = gson.fromJson(result_string, MessageResponse[].class);        
	        }
		} catch(UnsupportedEncodingException ex) {
			Log.e(TAG, "UnsupportedEncodingException", ex);
		} catch (ClientProtocolException ex) {
			Log.e(TAG, "ClientProtocolException", ex);
		} catch (IOException ex) {
			Log.e(TAG, "IOException", ex);
		}
		return result;
	}
}
