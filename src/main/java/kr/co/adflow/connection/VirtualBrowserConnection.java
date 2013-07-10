package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import kr.co.adlfow.util.HashSh;

public class VirtualBrowserConnection {

	private static final String VERIFICATION_SERVER_ADDRESS = "http://127.0.0.1:3000";
	URL url;
	HttpURLConnection connection = null;

	public void virtualPageDataSend(HttpServletRequest req,
			String responseorigin) {

		long start = System.currentTimeMillis();
		try {
			// Create connection

			url = new URL(VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"
					+ req.getSession().getId());

			System.out.println("req.getSession().getId():"
					+ req.getSession().getId());
			connection = (HttpURLConnection) url.openConnection();

			if (req.getHeader("X-Requested-With") == null) {
				connection.setRequestMethod("POST");
			} else {
				connection.setRequestMethod("PUT");
			}

			System.out.println("-------------VirtualBrowserConnection ReqHeader---------------------");
			for (Enumeration<?> e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				System.out.println(header + ":" + req.getHeader(header));
			}
			System.out.println("-------------VirtualBrowserConnection ReqHeaderEND--------------------");
			System.out.println("req.getURI:" + req.getRequestURI());
			String temp = "";

			if (req.getRequestURI().equals("/index.do")) {
				System.out.println("in...req.index.do");
				temp = "/board/index.jsp";
				System.out.println("temp:" + temp);
				connection.setRequestProperty("request_uri_origin", temp);
			} else if (req.getRequestURI().equals("/notice_list.do")) {
				System.out.println("in.../notice_list.do");
				temp = "/board/test_list.jsp";
				System.out.println("temp:" + temp);
				connection.setRequestProperty("request_uri_origin", temp);

			} else if (req.getRequestURI().equals("/notice_content.do")) {
				System.out.println("in.../notice_content.do");
				temp = "/board/test_read.jsp";
				System.out.println("temp:" + temp);
				connection.setRequestProperty("request_uri_origin", temp);

			} else {

				connection.setRequestProperty("request_uri_origin",
						req.getRequestURI());
			}
			

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());

			wr.writeBytes(responseorigin);

			wr.flush();
			wr.close();

			// Get Response
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
			System.out.println("Node.Js Server response DATA : " + responseData.toString());

	
			

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
}
