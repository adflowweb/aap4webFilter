package kr.co.adflow.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJsFilter implements Filter {

	private static Logger logger = LoggerFactory
			.getLogger(VerificationFilter.class);

	private static boolean onOff = true;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		logger.debug("TestJS Filter");

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if (req.getRequestURI().toString().contains("aap.js")) {

			logger.debug("Test JS Filter if..");

			if (onOff) {
				res.setStatus(200);
				onOff=false;
			} else {
				res.setStatus(304);
				onOff=true;
			}
		}

		chain.doFilter(request, response);

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
