package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import kr.co.adflow.util.FilterProperites;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualBrowserCreateConnection {

	private static FilterProperites filterProperites = FilterProperites
			.getInstance();
	private static final String APP4SERVERIP = filterProperites
			.read("aap4ServerIp");
	private static final String VITUALPAGEURI = filterProperites
			.read("vitualPageUri");
	private static final String MKDIRNAME = filterProperites.read("mkdirname");

	private URL url;
	private HttpURLConnection connection = null;

	private Logger logger = LoggerFactory
			.getLogger(VirtualBrowserCreateConnection.class);

	public void virtualPageDataSend(HttpServletRequest req,
			String responseOrigin) {
		logger.info("**************************************************************");
		logger.info("VirtualBrowserCREATE Start");
		long start = System.currentTimeMillis();
		try {
			// Create connection

			url = new URL(APP4SERVERIP + "/v1/virtualpages/"
					+ req.getSession().getId());
			connection = (HttpURLConnection) url.openConnection();

			// "X-Requested-With"
			if (req.getHeader("X-Requested-With") == null) {
				connection.setRequestMethod("POST");
			} else {
				connection.setRequestMethod("PUT");
			}

			logger.info("VirtualBrowserCreate ReqHeader");
			for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				System.out.println(header + ":" + req.getHeader(header));
			}

			logger.debug("req.getURI:" + req.getRequestURI());
			logger.info("VirtualBrowserCreate ReqHeaderEND");
			String temp = "";

			if (req.getRequestURI().equals("/index.do")) {
				temp = "/board/index.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);
			} else if (req.getRequestURI().equals("/notice_list.do")) {
				temp = "/board/test_list.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);
			} else if (req.getRequestURI().equals("/notice_content.do")) {
				temp = "/board/test_read.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);
			} else if (req.getRequestURI().equals("/notice_returnpage.do")) {
				temp = "/board/test_list.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);

			} else if (req.getRequestURI().equals("/notice_search.do")) {
				logger.debug("In notice_search.do");
				temp = "/board/test_search_list.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);

			} else if (req.getRequestURI().equals("/notice_content_search.do")) {
				logger.debug("In notice_content_search.do");
				temp = "/board/test_search_read.jsp";
				this.makeDir(MKDIRNAME + temp);
				connection.setRequestProperty(VITUALPAGEURI, temp);

			} else {

				connection.setRequestProperty(VITUALPAGEURI,
						req.getRequestURI());
			}

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// request virtualPage
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());

			wr.writeBytes(responseOrigin);

			wr.flush();
			wr.close();

			// response
			if (connection.getResponseCode() == 200) {
				InputStream is = connection.getInputStream();
				BufferedReader rd = new BufferedReader(
						new InputStreamReader(is));
				String line;
				StringBuffer responseData = new StringBuffer();
				while ((line = rd.readLine()) != null) {
					responseData.append(line);
					responseData.append('\r');
				}
				rd.close();
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		logger.info("VirtualBrowserConnection elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
		logger.info("VirtualBrowserCREATE END");
	}

	// mkdir
	public void makeDir(String fileName) {
		File dir = new File(fileName); //
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				logger.info("mkdir fail");
			}
		}
	}

}
