package com.vastpro.sphinx.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
 
public class SphinxRest extends ResourceConfig {
    public SphinxRest() {
        packages("com.vastpro.sphinx.rest.resource");
        register(JacksonFeature.class);
        System.out.println("Rest Class");
    }
}
