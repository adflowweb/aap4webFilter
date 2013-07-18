package kr.co.adlfow.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.adflow.connection.VerificationRequestConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(VerificationFilter.class);
	private HashMap map;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.info("**************************************************************");
		logger.info("Verification DO FILTER Start ");

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		int verificationResponseCode = 0;
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

		for (int i = 0; i < 10; i++) {

			if (i == 7) {
				req.setAttribute("verificationUri", 3);
			}
		}

		if (req.getHeader("hash") != null) {
			logger.info("Hash is Not Null Request Verification Server Start");
			VerificationRequestConnection connection = new VerificationRequestConnection();
			verificationResponseCode = connection
					.verificationPageSend(req, res);
			logger.debug("Verification Server ResponseCode:" + verificationResponseCode);

			if (verificationResponseCode == 200) {
				logger.debug("verification Success");

			} else if (verificationResponseCode == 404) {
				logger.debug("virtualPageNotFound 404");
				res.sendError(404);

			} else if (verificationResponseCode == 500) {
				logger.debug("Server Error 500");
				res.sendError(500);
			}

		} else {
			logger.info("Else is ...First Call Create VirtualBrowser FiTER");
			chain.doFilter(req, res);
		}

		logger.info("Verification DO FILTER END");
		logger.info("**************************************************************");

	}

	// verificationURI get
	public void init(FilterConfig arg0) throws ServletException {

		logger.info("**************************************************************");
		logger.info("VerificationFilter  INIT GET Verification URI ");
		logger.info("**************************************************************");
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
