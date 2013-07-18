package kr.co.adlfow.filter;

import java.io.IOException;
import java.io.PrintWriter;
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

import kr.co.adflow.connection.VerificationRequestConnection;
import kr.co.adflow.connection.VirtualBrowserCreateConnection;
import kr.co.adlfow.util.CopyPrintWriter;

public class VirtualBrowserFilter implements Filter {

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		int verificationResponseCode = 0;

		try {
			System.out
					.println("#############VirtualBrowserFilter Start ###############");

			HttpServletRequest req = (HttpServletRequest) request;

			// request LOG
			this.reqLog(req);

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

			// verification URI Check

			if (req.getAttribute("verificationUri") != null) {
				int temp = (Integer) req.getAttribute("verificationUri");
				System.out.println("verificationUri:" + temp);

				// verification request
				if (req.getAttribute("hash") != null) {
					System.out
							.println("verification request hash is not Null..");
					VerificationRequestConnection connection = new VerificationRequestConnection();
					verificationResponseCode = connection.verificationPageSend(
							req, writer.getCopy());
					System.out.println("verificationResponseCode:"+verificationResponseCode);
					
					if(verificationResponseCode==200){
						System.out.println("verification Success");
					}else if(verificationResponseCode==404){
						System.out.println("verification 404");
					}else if(verificationResponseCode==505){
						System.out.println("verification 505");
					}else if(verificationResponseCode==1000){
						System.out.println("Exception reponseCode 1000");
					}

					// first request
				} else {

					VirtualBrowserCreateConnection connection = new VirtualBrowserCreateConnection();
					connection.virtualPageDataSend(req, writer.getCopy());

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out
				.println("#############VirtualBrowserFilter END ###############");
	}

	// reqlog method
	public void reqLog(HttpServletRequest req) {
		System.out.println("requestURI : " + req.getRequestURI());
		System.out.println("requestMethod : " + req.getMethod());
		System.out.println("contentType : " + req.getContentType());

		System.out
				.println("#############VirtualBrowserFilter LOG param#############");
		for (Enumeration<?> e = req.getParameterNames(); e.hasMoreElements();) {
			String param = (String) e.nextElement();

			System.out.println(param + ":" + req.getParameter(param));

		}
		System.out
				.println("#############VirtualBrowserFilter LOG param END#############");

	}

	public void init(FilterConfig config) throws ServletException {

	}

}
