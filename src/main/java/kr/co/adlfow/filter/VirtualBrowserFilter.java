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

import kr.co.adflow.connection.VirtualBrowserCreateConnection;
import kr.co.adflow.testParser.TestClientModify;
import kr.co.adlfow.util.CopyPrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualBrowserFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(VirtualBrowserFilter.class);

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.info("**************************************************************");
		logger.info("VirtualBrowserFilter Start");
	
		try {

			HttpServletRequest req = (HttpServletRequest) request;

			logger.debug("requestURI : " + req.getRequestURI());
			logger.debug("requestMethod : " + req.getMethod());
			logger.debug("contentType : " + req.getContentType());

			logger.info("**************************************************************");
			logger.info("VirtualBrowserFilter Request Log param Start");
			for (Enumeration<?> e = req.getParameterNames(); e
					.hasMoreElements();) {
				String param = (String) e.nextElement();

				logger.debug(param + ":" + req.getParameter(param));

			}
			logger.info("VirtualBrowserFilter LOG param END");
			logger.info("**************************************************************");

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
			
			
			//Test Client modify
			TestClientModify clientModify= new TestClientModify();
			String test=clientModify.jsoupModify(writer.getCopy());	


			// verification URI Check

			if (req.getAttribute("verificationUri") != null) {
				int temp = (Integer) req.getAttribute("verificationUri");
				logger.debug("verificationUri:" + temp);
				VirtualBrowserCreateConnection connection = new VirtualBrowserCreateConnection();
				connection.virtualPageDataSend(req, test);
			}
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		logger.info("VirtualBrowserFilter END");
		logger.info("**************************************************************");
	}

	public void init(FilterConfig config) throws ServletException {

	}

}
