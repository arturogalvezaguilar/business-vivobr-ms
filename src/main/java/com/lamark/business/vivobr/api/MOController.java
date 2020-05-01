package com.lamark.business.vivobr.api;


import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.Endpoint;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
 
import com.lamark.business.vivobr.core.request.MORequestJboss;
import com.lamark.business.vivobr.core.service.InitService;
import com.lamark.business.vivobr.core.service.MOService;
import com.lamark.business.vivobr.core.service.impl.MOServiceImpl;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;

//@Path("/vivo")
//@Produces(MediaType.APPLICATION_JSON)
//public class MOController extends BaseController {
//	
//	@Inject
//	 private MOService moService;
//	
//
//	
//	@POST
//	@Path("/moRequest")
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Produces(MediaType.APPLICATION_JSON)
//
//
//	   public Response processMo( MORequestJboss requestJBoss){
//		moService.process(requestJBoss);
//		return buildSuccessWrapperResponse(null);
//	 }
//	
//	
//	
//
//
//	  
 


public class MOController  {

//    private static final Logger logger = LogManager.getLogger(VertxMain.class);
	
//	private static  final Logger logger = LoggerFactory.getLogger(MOController.class);

    public static void main(String... args){
        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx(options);

        Consumer<Class> runner = clazz -> {
            vertx.deployVerticle(clazz.getName(), stringAsyncResult -> {
                if (stringAsyncResult.succeeded()){
//                    logger.log(Level.INFO, "Succesfully deployed " + clazz.getSimpleName());
                	System.out.println("Succesfully deployed " + clazz.getSimpleName());
                } else {
//                    logger.log(Level.ERROR, "Failed to deploy" + stringAsyncResult.cause());
                	System.out.println("Failed to deploy" + stringAsyncResult.cause());
                }
            });
        };

        Class[] clazzes = {MOServiceImpl.class};

        Stream.of(clazzes).forEach(runner);

    }

}

