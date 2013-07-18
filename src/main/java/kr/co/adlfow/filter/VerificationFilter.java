package kr.co.adlfow.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.webdav.lib.ResponseEntity;

import kr.co.adflow.connection.VerificationRequestConnection;
import kr.co.adlfow.util.CopyPrintWriter;

public class VerificationFilter implements Filter {

	private HashMap map;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		System.out.println("___________________________________________");
		System.out.println("Verification DO FILTER Start ");
		System.out.println("___________________________________________");
		HttpServletRequest req = (HttpServletRequest) request;
		int verificationResponseCode=0;
		/*
		 * try { ArrayList uriArr = null; if (map != null) { for (int i = 0; i <
		 * map.size(); i++) { uriArr = (ArrayList) map.get("uri");
		 * 
		 * }
		 * 
		 * for (int i = 0; i < uriArr.size(); i++) {
		 * System.out.println("uriArrayList:" + uriArr.get(i)); } }
		 * chain.doFilter(request, response); } catch (Exception e) {
		 * 
		 * }
		 */

		// verification URI check
		
		// FilterChain
		final CopyPrintWriter writer = new CopyPrintWriter(
				response.getWriter());
		chain.doFilter(request, new HttpServletResponseWrapper(
				(HttpServletResponse) response) {
			@Override
			public PrintWriter getWriter() {
				return writer;
			}
		});

		for (int i = 0; i < 10; i++) {

			if (i == 7) {
				request.setAttribute("verificationUri", 3);
			}
		}

		if (req.getHeader("hash") != null) {
			System.out.println("verification request hash is not Null..");
			VerificationRequestConnection connection = new VerificationRequestConnection();
			verificationResponseCode = connection.verificationPageSend(req, writer.getCopy());
			System.out.println("verificationResponseCode:"
					+ verificationResponseCode);

			if (verificationResponseCode == 200) {
				System.out.println("verification Success");
			} else if (verificationResponseCode == 404) {
				System.out.println("verification 404");
			} else if (verificationResponseCode == 505) {
				System.out.println("verification 505");
			} else if (verificationResponseCode == 1000) {
				System.out.println("Exception reponseCode 1000");
			}

		}

	
		System.out.println("___________________________________________");
		System.out.println("Verification DO FILTER END");
		System.out.println("___________________________________________");

	}

	// verificationURI get
	public void init(FilterConfig arg0) throws ServletException {

		System.out
				.println("_________________________________________________________");
		System.out.println("VerificationFilter  INIT GET verification URI ");
		System.out
				.println("_________________________________________________________");
		// verification server getURI
		// map
		/*
		 * try { VerificationConnection check = new VerificationConnection();
		 * String result = check.verificationCheck();
		 * 
		 * UrlCheckUtil util = new UrlCheckUtil(); map = new HashMap(); map =
		 * (HashMap) util.urlCheckTable(result); } catch (Exception e) {
		 * e.printStackTrace();
		 * 
		 * }
		 */
	}
}
