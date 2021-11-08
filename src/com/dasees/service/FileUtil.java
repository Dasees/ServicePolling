package com.dasees.service;

import static com.dasees.service.MainVerticle.NAME;
import static com.dasees.service.MainVerticle.STATUS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Vertx file management 
 * @author Dasees Gupta
 *
 */
public class FileUtil {
	
	public static final String FILE_NAME = "ServiceMonitor.txt";
	private FileSystem filesystem;
	
	FileUtil(Vertx vertx)
	{
		this.filesystem=vertx.fileSystem();
	}
	
	public void fileExists(Handler<AsyncResult<Boolean>> handler) {
        filesystem.exists(FILE_NAME, handler);
    }

    public void readFromFile(HashMap<String, String> services) {
        filesystem.readFile(FILE_NAME, getHandler(services));
    }

    public void createFile(Handler<AsyncResult<Void>> handler) {
        filesystem.createFile(FILE_NAME, handler);
    }
    public void writeToFile(Map<String, String> services) {
        filesystem.open(FILE_NAME, new OpenOptions(), result -> {
            if (result.succeeded()) {
                AsyncFile file = result.result();
                List<JsonObject> jsonServices = services
                        .entrySet()
                        .stream()
                        .map(service ->
                                new JsonObject()
                                        .put(NAME, service.getKey())
                                        .put(STATUS, service.getValue()))
                        .collect(Collectors.toList());

                Buffer buffer;
                if (jsonServices.isEmpty()) {
                	buffer = Buffer.buffer("");
                } else {
                	buffer = Buffer.buffer(jsonServices.toString());
                }
                file.write(buffer);
            } else {
                System.err.println(result.cause().getMessage());

            }
        });
    }
    private Handler<AsyncResult<Buffer>> getHandler(HashMap<String, String> services) {
        return result -> {
            if (result.succeeded() && result.result() != null  ) {
            	if(result.result().length() != 0)
            	{
                    JsonArray jsonarray = result.result().toJsonArray();
                    for (Object json : jsonarray) {
                        JsonObject jsonservice = (JsonObject) json;
                        services.put(jsonservice.getValue(NAME).toString(),
                        		jsonservice.getValue(STATUS).toString());
                    }
                }
            }
             else {
                System.err.println(result.cause().getMessage());
            }
        };
    }

 
}
