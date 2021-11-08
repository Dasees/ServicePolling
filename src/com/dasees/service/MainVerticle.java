package com.dasees.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Verticle whose purpose is to verify the URL and poll for the service to verify if its up or not
 * @author Dasees Gupta
 *
 */
public class MainVerticle extends AbstractVerticle {
	public static final String NAME = "Name";
	public static final String STATUS = "Status";
	private static final String URL = "url";
	private static final int HTTP_SUCCESS = 200;
	private static final String URL_REGEX =
			"^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))" +
					"(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" +
					"([).!';/?:,][[:blank:]])?$";
	private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
	private HashMap<String, String> services = new HashMap<>();
	private FileUtil fileutil;

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);

		fileutil = new FileUtil(vertx);	    
		fileutil.fileExists(res -> {
			if (res.succeeded() && res.result()) {
				fileutil.readFromFile(services);
			} else {
				fileutil.createFile(result -> {
					if (result.succeeded()) {
						System.out.println("File created");
					} else {
						System.err.println(result.cause().getMessage());
					}
				});
			}
		});
	}

	public void start() {
		HttpServer server=vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		getServiceList(router);
		addService(router);
		deleteService(router);
		vertx.setPeriodic(30000, timerId ->
		pollServices(services, vertx, fileutil));
		server.requestHandler(router).listen(8080,result -> {
			if (result.succeeded()) {
				System.out.println("HTTP server started for testing");

			} else {
				System.out.println(result.cause());
			}
		});
	}
/**
 * Get method to fetch the list of services
 * @param router
 */
	private void getServiceList(Router router) {
		router.get("/").handler(req -> {
			List<JsonObject> jsonServices = services
					.entrySet()
					.stream()
					.map(service ->
					new JsonObject()
					.put(NAME, service.getKey())
					.put(STATUS, service.getValue()))
					.collect(Collectors.toList());
			req.response()
			.putHeader("content-type", "application/json")
			.end(new JsonArray(jsonServices).encode());
		});
	}
/**
 * POST method to add service for poll
 * @param router
 */
	private void addService(Router router) {
		router.post("/add").handler(req -> {
			String url=req.getBodyAsJson().getString(URL);
			if(isValidUrl(url))
			{
				services.put(url," ");
				req.response().end("Added to List");
				pollServices(services, vertx, fileutil);	
			}else
			{
				req.response().end("Invalid Url");
			}

		});
	}
/**
 * DELETE method to remove service from polling
 * @param router
 */
	private void deleteService(Router router) {
		router.delete("/").handler(req -> {
			String url=req.getBodyAsJson().getString(URL);
			if(isValidUrl(url))
			{
				services.remove(req.getBodyAsJson().getString(URL));
				req.response().end("Removed from List");
				fileutil.writeToFile(services);	
			}else
			{
				req.response().end("Invalid Url");
			}

		});
	}
/**
 * To validate if input URL is valid or not
 * @param url
 * @return
 */
	public static boolean isValidUrl(String url)
	{
		if (url == null) {
			return false;
		}

		Matcher matcher = URL_PATTERN.matcher(url);
		return matcher.matches();
	}
	public Future<List<String>> pollServices(Map<String, String> services, Vertx vertx,
			FileUtil fileutil) {
		services.forEach((url, status) -> {
			WebClient.create(vertx).getAbs(url).send(result -> {
				if (result == null || result.result() == null) {
					services.put(url, "Stopped");
					fileutil.writeToFile(services);
					return;
				}

				if (result.result().statusCode() == HTTP_SUCCESS) {
					services.put(url, "Running");
				} else {
					services.put(url, "Stopped");
				}
				fileutil.writeToFile(services);
			});
		});
		return Future.succeededFuture();
	}

}