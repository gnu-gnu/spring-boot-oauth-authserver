package com.gnu.AuthServer;

import java.io.IOException;
import java.util.Base64;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AuthInnerFilter implements Filter {
	Logger logger = LoggerFactory.getLogger(AuthInnerFilter.class);
	final Marker REQUEST_MARKER = MarkerFactory.getMarker("HTTP_REQUEST");
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
			HttpServletRequest req = (HttpServletRequest)arg0;
			logger.info("--------- userprincipal : {}", req.getUserPrincipal());
			logger.info("--------- call for {}", req.getRequestURI());
			Enumeration<String> names = req.getHeaderNames();
			logger.info("----- headers");
			String key = "";
			while(names.hasMoreElements()){
				key = names.nextElement();
				if (key.startsWith("authorization")) {
					logger.info(REQUEST_MARKER, "{} : {}", key, new String(Base64.getDecoder().decode(req.getHeader(key).split(" ")[1])));
				} else {
					logger.info(REQUEST_MARKER, "{} : {}", key, req.getHeader(key));
				}
			}
			String paramKey = "";
			logger.info("----- params");
			Enumeration<String> params = req.getParameterNames();
			while(params.hasMoreElements()){
				paramKey = params.nextElement();
				logger.info(REQUEST_MARKER, "{} : {}", paramKey, req.getParameter(paramKey));
			}
			arg2.doFilter(arg0, arg1);
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
