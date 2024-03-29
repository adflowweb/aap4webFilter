package kr.co.adflow.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class EncodingFilter implements Filter {

	protected String encoding = null;
	protected FilterConfig filterConfig = null;
	protected boolean ignore = true;

	public void destroy() {
		this.encoding = null;
		this.filterConfig = null;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// Conditionally select and set the character encoding to be used
		try {
			if (ignore || (request.getCharacterEncoding() == null)) {
				String encoding = selectEncoding(request);
				if (encoding != null) {

					// utf - 8
					request.setCharacterEncoding(encoding);
					response.setCharacterEncoding(encoding);

				}
			}

			// Pass control on to the next filter
			chain.doFilter(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			this.filterConfig = filterConfig;
			this.encoding = filterConfig.getInitParameter("encoding");
			String value = filterConfig.getInitParameter("ignore");
			if (value == null)
				this.ignore = true;
			else if (value.equalsIgnoreCase("true"))
				this.ignore = true;
			else if (value.equalsIgnoreCase("yes"))
				this.ignore = true;
			else
				this.ignore = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String selectEncoding(ServletRequest request) {
		return (this.encoding);
	}

}
