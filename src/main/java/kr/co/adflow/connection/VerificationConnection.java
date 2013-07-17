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
	private HttpURLConnection connection = null;
	private static FilterProperites filterProperites = FilterProperites.getInstance();
	private static final String APP4SERVERIP = filterProperites.read("aap4ServerIp");
	private OutputStreamWriter wr = null;
	private BufferedReader rd = null;
	private StringBuilder sb = null;
	private String line = null;
	private URL serverAddress = null;

	public String verificationCheck() {

		long start = System.currentTimeMillis();
		
		

		try {
			serverAddress = new URL(APP4SERVERIP + "/v1/verificationuri");

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
