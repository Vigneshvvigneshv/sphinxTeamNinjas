package com.vastpro.sphinx.rest;

import org.glassfish.jersey.server.ResourceConfig;

import com.vastpro.sphinx.util.SphinxConstants;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

 
public class SphinxRest extends ResourceConfig {
    public SphinxRest() {
        packages(SphinxConstants.RESOURCE_PACKAGE);//resource package
        register(JacksonFeature.class);
        register(MultiPartFeature.class);
        System.out.println("Rest Class");
    }
    
	
}
