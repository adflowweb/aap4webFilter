package kr.co.adflow.asynctest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

public class AsyncHttpExample {
	public static void main(String[] args) throws IOException,
			InterruptedException, ExecutionException {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		
		
		Future<Response> f = asyncHttpClient.prepareGet(
				"http://127.0.0.1:3000/v1/verificationuri").execute(
				new AsyncCompletionHandler<Response>() {

					@Override
					public Response onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						// ...
						
						return response;
					}

					@Override
					public void onThrowable(Throwable t) {
						// Something wrong happened.
						
						
					}
				});
		Response res = f.get();
		System.out.println("res:" + res.getResponseBody());

		asyncHttpClient.close();

	}

}
