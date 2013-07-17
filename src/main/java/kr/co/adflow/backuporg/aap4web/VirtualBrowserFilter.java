package kr.co.adflow.backuporg.aap4web;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

public class VirtualBrowserFilter implements Filter {

	private static final String VERIFICATION_SERVER_ADDRESS = "http://192.168.1.94:3000";

	// testServer
	// private static final String VERIFICATION_SERVER_ADDRESS =
	// "http://192.168.1.28:3000/dom";

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession(true);

		System.out.println("requestURI : " + req.getRequestURI());
		System.out.println("requestMethod : " + req.getMethod());
		System.out.println("contentType : " + req.getContentType());

		for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
			String param = (String) e.nextElement();
			System.out.println(param + ":" + req.getParameter(param));
		}

		final CopyPrintWriter writer = new CopyPrintWriter(response.getWriter());
		chain.doFilter(request, new HttpServletResponseWrapper(
				(HttpServletResponse) response) {
			@Override
			public PrintWriter getWriter() {
				return writer;
			}
		});

		System.out.println("response origin : " + writer.getCopy());

		long start = System.currentTimeMillis();

		// send data
		// receive data
		// print data
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			url = new URL(VERIFICATION_SERVER_ADDRESS + "/v1/virtualpages/"+req.getSession().getId());
			connection = (HttpURLConnection) url.openConnection();

			if (req.getHeader("X-Requested-With") == null) {
				connection.setRequestMethod("POST");
			} else {
				connection.setRequestMethod("PUT");
			}

			System.out.println("----------------------------------");
			for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
				String header = (String) e.nextElement();
				connection.setRequestProperty(header, req.getHeader(header));
				System.out.println(header + ":" + req.getHeader(header));
			}
			connection.setRequestProperty("request_uri_origin", req.getRequestURI());
			System.out.println("----------------------------------");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());

			wr.writeBytes(writer.getCopy());

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
			System.out
					.println("response : " + responseData.toString());

			// set session
			// session.setAttribute("uptodateDom", responseData.toString());

		} catch (Exception e) {
			e.printStackTrace();
			// return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		System.out.println("elapsedTime : "
				+ (System.currentTimeMillis() - start) + " ms ");
	}

	public void init(FilterConfig config) throws ServletException {

	}

}
