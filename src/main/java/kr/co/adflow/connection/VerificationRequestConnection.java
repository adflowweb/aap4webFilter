package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.adlfow.util.FilterProperites;

public class VerificationRequestConnection {
	private static FilterProperites filterProperites = FilterProperites
			.getInstance();
	private static final String APP4SERVERIP = filterProperites
			.read("aap4ServerIp");

	private static final String MKDIRNAME = filterProperites.read("mkdirname");

	private URL url;
	private HttpURLConnection connection = null;

	public int verificationPageSend(HttpServletRequest req,
			HttpServletResponse res) {
		System.out
				.println("#############Verification Requset Start  ###############");
		int reponseCode = 0;
		long start = System.currentTimeMillis();
		try {
			// Create connection

			url = new URL(APP4SERVERIP + "/v1/verify/"
					+ req.getSession().getId());

			connection = (HttpURLConnection) url.openConnection();

			// "X-Requested-With"
			if (req.getHeader("X-Requested-With") == null) {
				connection.setRequestMethod("POST");
			} else {
				connection.setRequestMethod("PUT");
			}

			System.out
					.println("#############verificationRequest ReqHeader#############");
			for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				System.out.println(header + ":" + req.getHeader(header));
			}

			System.out.println("req.getURI:" + req.getRequestURI());
			System.out
					.println("#############verificationRequest ReqHeaderEND#############");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// verificationServerRequest
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(req.getHeader("hash").toString());
			wr.flush();
			wr.close();

			System.out.println("verificationServerRequest Hash:"
					+ req.getHeader("hash").toString());

			// Get verificationServerResponse
			reponseCode=connection.getResponseCode();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("TRY CATCH reponseCode:" + reponseCode);
			if (reponseCode == 0) {
				reponseCode = 500;
			}

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		System.out.println("VerifiCationConnection elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
		System.out
				.println("#############Verification Requset END  ###############");
		return reponseCode;
	}

}
