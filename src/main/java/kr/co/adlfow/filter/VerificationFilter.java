package kr.co.adlfow.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.co.adflow.connection.VerificationConnection;
import kr.co.adlfow.util.UrlCheckUtil;

public class VerificationFilter implements Filter {

	private HashMap map;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		System.out.println("___________________________________________");
		System.out.println("Verification DO FILTER TEST");
		System.out.println("___________________________________________");

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
		for (int i = 0; i < 10; i++) {
			
			if(i==7){
				request.setAttribute("verificationUri", 3);
			}
		}
		
		
		chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {

		System.out.println("___________________________________________");
		System.out.println("VerificationFilter  INIT GET verification URI ");
		System.out.println("___________________________________________");
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
