package app.arcanum.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import android.os.AsyncTask;
import android.util.Log;

import app.arcanum.AppSettings;
import app.arcanum.contracts.RegisterRequest;
import app.arcanum.contracts.RegisterResponse;

public class HttpSendRegisterTask extends AsyncTask<RegisterRequest, Void, RegisterResponse> {

	@Override
	protected RegisterResponse doInBackground(RegisterRequest... reqs) {
		RegisterResponse resp = new RegisterResponse();
		
		try {
			HttpClient client = new DefaultHttpClient();			
			HttpPost post = new HttpPost(AppSettings.SERVER_URL + AppSettings.Methods.REGISTER);
			post.addHeader("Connection", 	"Keep-Alive");
			post.addHeader("Content-Type", 	"application/json; charset=utf-8");
			post.addHeader("Accept",		"application/json; charset=utf-8");
			post.addHeader("Accept-Charset","utf-8");
	        
			Gson gson = new Gson();
			String 			register_string = gson.toJson(reqs[0]);
			StringEntity 	register_entity = new StringEntity(register_string);
	    	post.setEntity(register_entity);
	    	
	    	HttpResponse response = client.execute(post);
	    	StatusLine statusLine = response.getStatusLine();
	    	resp.HttpStatusCode = statusLine.getStatusCode();
	    	
	        if(resp.HttpStatusCode == java.net.HttpURLConnection.HTTP_OK){
	            byte[] result_bytes = EntityUtils.toByteArray(response.getEntity());
	            String result_string = new String(result_bytes, "UTF-8");
	            resp.HttpBody = result_string;
	        }       	
		} catch(Exception ex) {
			Log.e("HttpSendContactsTask", "Error while sync contacts.", ex);
		}
		
		return resp;
	}	
}
