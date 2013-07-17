package kr.co.adflow.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import kr.co.adlfow.util.FilterProperites;
import kr.co.adlfow.util.HashSh;

public class VirtualBrowserConnection {

	private static FilterProperites filterProperites = FilterProperites
			.getInstance();
	private static String app4ServerIp = filterProperites.read("aap4ServerIp");
	private static final String VERIFICATION_SERVER_ADDRESS = app4ServerIp;
	private URL url;
	private HttpURLConnection connection = null;

	public void virtualPageDataSend(HttpServletRequest req,
			String responseorigin) {

		long start = System.currentTimeMillis();
		try {
			// Create connection

			url = new URL(VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"
					+ req.getSession().getId());

			connection = (HttpURLConnection) url.openConnection();

			//"X-Requested-With" 
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
			///home/master/nodejs/aap4web/routes/site
			if (req.getRequestURI().equals("/index.do")) {
				System.out.println("in...req.index.do");
				temp = "/board/index.jsp";
				this.makeDir("/home/master/nodejs//aap4web/routes/site/board/index.jsp");
				System.out.println("temp:" + temp);
				connection.setRequestProperty("virtual_page_uri", temp);
			} else if (req.getRequestURI().equals("/notice_list.do")) {
				System.out.println("in.../notice_list.do");
				temp = "/board/test_list.jsp";
				System.out.println("temp:" + temp);
				connection.setRequestProperty("virtual_page_uri", temp);

			} else if (req.getRequestURI().equals("/notice_content.do")) {
				System.out.println("in.../notice_content.do");
				temp = "/board/test_read.jsp";
				System.out.println("temp:" + temp);
				connection.setRequestProperty("virtual_page_uri", temp);

			} else {

				connection.setRequestProperty("virtual_page_uri",
						req.getRequestURI());
			}

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// verificationServerRequest
			this.verificationServerRequest(connection, responseorigin);

			// Get Response
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
	
	//mkdir
	
	public void makeDir(String fileName){
		File dir = new File(fileName); // 
		if(!dir.exists()){ 
			if(!dir.mkdirs()){
				System.out.println("mkdir fail");
			}
		}
	}

}
