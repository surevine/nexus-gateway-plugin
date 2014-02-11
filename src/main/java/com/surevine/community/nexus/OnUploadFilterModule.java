package com.surevine.community.nexus;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

@Named
public class OnUploadFilterModule extends AbstractModule {
	
	@Override
	protected void configure() {
	    install(new ServletModule() {
	      @Override
	      protected void configureServlets() {
	    	  filter("/*").through(OnUploadWebFilter.class);
	      }
	    });
	}
}
