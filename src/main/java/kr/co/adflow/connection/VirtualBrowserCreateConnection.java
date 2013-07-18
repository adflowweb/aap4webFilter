package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import kr.co.adlfow.util.FilterProperites;

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

	public void virtualPageDataSend(HttpServletRequest req,
			String responseOrigin) {

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

			System.out
					.println("-------------VirtualBrowserConnection ReqHeader---------------------");
			for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				System.out.println(header + ":" + req.getHeader(header));
			}
			System.out
					.println("-------------VirtualBrowserConnection ReqHeaderEND--------------------");
			System.out.println("req.getURI:" + req.getRequestURI());
			String temp = "";
			// /home/master/nodejs/aap4web/routes/site
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

			} else {

				connection.setRequestProperty(VITUALPAGEURI,
						req.getRequestURI());
			}

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// verificationServerRequest
			this.verificationServerRequest(connection, responseOrigin);

			// Get verificationServerResponse
			this.verificationServerResponse(connection);

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		System.out.println("VirtualBrowserConnection elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
	}
	
	
 	
	
	
	
	
	

	// request
	public void verificationServerRequest(HttpURLConnection connection,
			String reqData) throws IOException {
		// Send request
		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

		wr.writeBytes(reqData);

		wr.flush();
		wr.close();

	}

	// response
	public void verificationServerResponse(HttpURLConnection connection)
			throws IOException {
		InputStream is = connection.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer responseData = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			responseData.append(line);
			responseData.append('\r');
		}
		rd.close();
		// return responseData.toString();
		System.out.println("Node.Js Server response DATA : "
				+ responseData.toString());

	}

	// mkdir

	public void makeDir(String fileName) {
		File dir = new File(fileName); //
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				System.out.println("mkdir fail");
			}
		}
	}

}
