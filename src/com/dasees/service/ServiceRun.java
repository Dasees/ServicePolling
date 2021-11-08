package com.dasees.service;

import io.vertx.core.Vertx;

/**
 * Used to deploy verticle
 * @author Dasees Gupta
 *
 */
public class ServiceRun {

	public static void main(String[] args) {
		Vertx.vertx().deployVerticle(new MainVerticle());
	}

}
