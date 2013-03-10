package app.arcanum.tasks;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import app.arcanum.AppSettings;

public class HttpSendMessageTask extends AsyncTask<byte[], String, String> {
	protected ITaskCallbackable _callback;
	
	public HttpSendMessageTask setCallback(ITaskCallbackable callback) {
		_callback = callback;
		return this;
	}
	
	@Override
    protected String doInBackground(byte[]... params) {
        byte[] result = null;
        String str = "";
        
        try {
	        HttpClient client = new DefaultHttpClient();
	        
	        HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.SEND_MESSAGE);
	        post.addHeader("Connection", 				"Keep-Alive");
	        post.addHeader("Content-Type", 				AppSettings.MESSAGE_CONTENT_TYPE);
	        post.addHeader("Content-Transfer-Encoding",	AppSettings.MESSAGE_CONTENT_ENCODING);
	        
	        //if(AppSettings.MESSAGE_CONTENT_ENCODING == "base64") {
	        	String content = Base64.encodeToString(params[0], Base64.DEFAULT);
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
	        if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
	            result = EntityUtils.toByteArray(response.getEntity());
	            str = new String(result, "UTF-8");
	        }
        } catch (ClientProtocolException ex) {
        	final String message = "ClientProtocolException";
        	Log.e("HttpPostTask", message, ex);
        	TrySendErrorCallback(message, ex);
		} catch (IOException ex) {
			final String message = "IOException";
			Log.e("HttpPostTask", message, ex);
			TrySendErrorCallback(message, ex);
		}
        return str;
    }

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(_callback == null)
			return;
		_callback.PostExecuteCalled();
	}
	
	private void TrySendErrorCallback(String message, Throwable ex) {
		if(_callback == null)
			return;
		_callback.ErrorOccurred(message, ex);
	}
}
