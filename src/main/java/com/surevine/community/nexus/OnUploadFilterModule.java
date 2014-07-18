package com.surevine.community.nexus;

import java.io.IOException;

import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

@Named
public class OnUploadFilterModule extends AbstractModule {
	
	@Override
	protected void configure() {
	    install(new ServletModule() {
	      @Override
	      protected void configureServlets() {
	    	  filter("/*").through(OnManualUploadWebFilter.class);
	    	  filter("/nexus/content/repositories/*").through(OnMavenUploadWebFilter.class);
	    	  
	    	  filter("/*").through(new Filter() {
				@Override
				public void destroy() {}

				@Override
				public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
					final HttpServletRequest req = (HttpServletRequest) arg0;
					System.out.println(req.getMethod() +" " +req.getRequestURI());
					arg2.doFilter(arg0, arg1);
				}

				@Override
				public void init(FilterConfig arg0) throws ServletException {}
	    	  });
	      }
	    });
	}
}
