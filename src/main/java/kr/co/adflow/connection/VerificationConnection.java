package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import kr.co.adlfow.util.FilterProperites;

public class VerificationConnection {

	public String verificationCheck() {

		long start = System.currentTimeMillis();
		HttpURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;

		URL serverAddress = null;
		
		//FilterProperites filterProperites=FilterProperites.getInstance();
		
		String app4ServerIp="http://192.168.1.94:3000";
	
		try {
			serverAddress = new URL(app4ServerIp+"/v1/verificationuri");

			connection = null;

			connection = (HttpURLConnection) serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			connection.connect();

			rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			sb = new StringBuilder();

			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}

			System.out.println(sb.toString());

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			connection.disconnect();
			rd = null;
			wr = null;
			connection = null;
		}
		System.out.println("VerificationConnection elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
		return sb.toString();

	}
}
