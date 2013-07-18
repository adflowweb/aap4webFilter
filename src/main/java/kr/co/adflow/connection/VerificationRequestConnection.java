package kr.co.adflow.connection;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.co.adlfow.util.FilterProperites;

public class VerificationRequestConnection {
	private Logger logger = LoggerFactory
			.getLogger(VerificationRequestConnection.class);
	private static FilterProperites filterProperites = FilterProperites
			.getInstance();
	private static final String APP4SERVERIP = filterProperites
			.read("aap4ServerIp");

	private URL url;
	private HttpURLConnection connection = null;

	public int verificationPageSend(HttpServletRequest req,
			HttpServletResponse res) {

		logger.info("**************************************************************");
		logger.info("Verification Requset Start");
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
			logger.info("**************************************************************");
			logger.info("verificationRequest ReqHeader");
			for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				logger.debug(header + ":" + req.getHeader(header));
			}

			logger.debug("req.getURI:" + req.getRequestURI());
			logger.info("verificationRequest ReqHeaderEND");
			logger.info("**************************************************************");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// verificationServerRequest
			String hash=req.getHeader("hash").toString();
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(hash);
			wr.flush();
			wr.close();

			logger.debug("Request to Send Verification Server Hash:"+hash);

			// Get verificationServerResponse
			reponseCode = connection.getResponseCode();
			logger.debug("Verification Server ResponseCode:"+reponseCode);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		logger.debug("VerifiCationConnection elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
		logger.info("Verification Requset END");
		logger.info("**************************************************************");
		return reponseCode;
	}

}
