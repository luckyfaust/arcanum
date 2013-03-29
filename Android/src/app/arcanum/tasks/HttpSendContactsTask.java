package app.arcanum.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import app.arcanum.AppSettings;
import app.arcanum.tasks.contracts.ServerContactRequest;
import app.arcanum.tasks.contracts.ServerContactResponse;

public class HttpSendContactsTask extends AsyncTask<ServerContactRequest, Void, ServerContactResponse[]> {
	@Override
	protected ServerContactResponse[] doInBackground(ServerContactRequest... contacts) {
		ServerContactResponse[] result = null;
		
		AndroidHttpClient client = AndroidHttpClient.newInstance(AppSettings.HTTP.USER_AGENT);
		try {
			HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.SYNC_CONTACTS);
			post.addHeader("Content-Type", 	"application/json; charset=utf-8");
			post.addHeader("Accept",		"application/json; charset=utf-8");
			post.addHeader("Accept-Charset","utf-8");
	        
			Gson gson = new Gson();
			String contacts_string = gson.toJson(contacts);
			StringEntity entity = new StringEntity(contacts_string);
        	post.setEntity(entity);
        	
        	HttpResponse response = client.execute(post);
        	StatusLine statusLine = response.getStatusLine();
	        if(statusLine.getStatusCode() == java.net.HttpURLConnection.HTTP_OK){
	            byte[] result_bytes = EntityUtils.toByteArray(response.getEntity());
	            String result_string = new String(result_bytes, "UTF-8");
	            result = gson.fromJson(result_string, ServerContactResponse[].class);        
	        }
		} catch(Exception ex) {
			Log.e("HttpSendContactsTask", "Error while sync contacts.", ex);
		} finally {
			client.close();
		}
		
		return result;
	}
}
