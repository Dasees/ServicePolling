# ServicePolling
It is used to monitor whether services are up or not e.g https://google.com is up or not


**Instructions** 

Run the ServiceRun class to start HTTP server.

Following classes have created:

(1)	MainVerticle is the verticle class which hosts the operations to monitor services.
public class MainVerticle extends AbstractVerticle {

(2)	FileUtil class has all the operations related to file operations.
public class FileUtil {

(3)	ServiceRun class is used to deploy vertical.
public class ServiceRun {

**Tools Used**

•	Java8

•	Vertx

•	Postman

•	Eclipse



**Add Service**
 (POST: http://localhost:8080/add)
 
**Get Service List**
 (GET: http://localhost:8080)
 
**Delete from List** 
(DELETE: http://localhost:8080/)
 
 




