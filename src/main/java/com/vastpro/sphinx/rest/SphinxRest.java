package com.vastpro.sphinx.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

public class SphinxRest extends ResourceConfig {
	static {
		System.out.println("Sphinx rest started");
	}
}
