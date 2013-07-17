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

import kr.co.adflow.connection.VirtualBrowserConnection;
import kr.co.adlfow.util.CopyPrintWriter;

public class VirtualBrowserFilter implements Filter {

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		try {
			System.out
					.println("#############VirtualBrowserFilter Start###############");

			HttpServletRequest req = (HttpServletRequest) request;

			this.reqLog(req);

			final CopyPrintWriter writer = new CopyPrintWriter(
					response.getWriter());
			chain.doFilter(request, new HttpServletResponseWrapper(
					(HttpServletResponse) response) {
				@Override
				public PrintWriter getWriter() {
					return writer;
				}
			});

			VirtualBrowserConnection connection = new VirtualBrowserConnection();

			connection.virtualPageDataSend(req, writer.getCopy());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out
				.println("#############VirtualBrowserFilter END###############");
	}

	public void reqLog(HttpServletRequest req) {
		System.out.println("requestURI : " + req.getRequestURI());
		System.out.println("requestMethod : " + req.getMethod());
		System.out.println("contentType : " + req.getContentType());

		System.out
				.println("_____________VirtualBrowserFilter param_____________________");
		for (Enumeration<?> e = req.getParameterNames(); e.hasMoreElements();) {
			String param = (String) e.nextElement();

			System.out.println(param + ":" + req.getParameter(param));

		}
		System.out
				.println("______________VirtualBrowserFilter param END____________________");

	}

	public void init(FilterConfig config) throws ServletException {

	}

}
