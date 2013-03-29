package app.arcanum.tasks;


import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import app.arcanum.AppSettings;
import app.arcanum.tasks.contracts.ITaskPostListener;

public class HttpSendMessageTask extends AsyncTask<byte[], Void, Boolean> {
	private static final String TAG = HttpSendMessageTask.class.getName();
	protected ITaskPostListener _callback;
	private byte[] _input;
	
	public HttpSendMessageTask setCallback(ITaskPostListener callback) {
		_callback = callback;
		return this;
	}
	
	@Override
    protected Boolean doInBackground(byte[]... params) {
		if(params.length <= 0)
			return false;
		
		_input = params[0];
		
        AndroidHttpClient client = AndroidHttpClient.newInstance(AppSettings.HTTP.USER_AGENT);
        try {
        	HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.SEND_MESSAGE);
	        post.addHeader("Content-Type", 				AppSettings.MESSAGE_CONTENT_TYPE);
	        post.addHeader("Content-Transfer-Encoding",	AppSettings.MESSAGE_CONTENT_ENCODING);
	        
	        //if(AppSettings.MESSAGE_CONTENT_ENCODING == "base64") {
	        	String content = Base64.encodeToString(_input, Base64.DEFAULT);
	        	StringEntity entity = new StringEntity(content);
	        	post.setEntity(entity);
	        //} else {
	        //	ByteArrayInputStream content = new ByteArrayInputStream(params[0]);
	        //	
	        //	BasicHttpEntity entity = new BasicHttpEntity();
	        //	entity.setContent(content);
	        //
	        //	post.setEntity(entity);
	        //}
	        
            HttpResponse response = client.execute(post);
			
	        StatusLine statusLine = response.getStatusLine();
	        return statusLine.getStatusCode() == HttpURLConnection.HTTP_OK;	         
        } catch (ClientProtocolException ex) {
        	final String message = "ClientProtocolException";
        	Log.e(TAG, message, ex);
		} catch (IOException ex) {
			final String message = "IOException";
			Log.e(TAG, message, ex);
		} finally {
			client.close();
		}
        return false;
    }

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(_callback != null)
			_callback.onPostExecute(TAG, _input, result);
	}
}
