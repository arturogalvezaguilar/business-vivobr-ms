package com.lamark.business.vivobr.core.service.impl;

import java.text.DateFormat;
import java.time.ZoneOffset;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamark.business.vivobr.core.request.MORequestJboss;
import com.lamark.business.vivobr.core.service.InitService;
import com.lamark.business.vivobr.core.service.MOService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
@Singleton
public class MOServiceImpl extends AbstractVerticle  {

	@Inject
	InitService initConfiguration;
	
	private static final ObjectMapper mapper = new ObjectMapper();
	  
	  
 
    @Override
    public void start(Future<Void> future) { 

        HttpServer server =  vertx.createHttpServer()
                .requestHandler(createRouter()::accept)
                .listen(8082, httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(httpServerAsyncResult.cause());
                    }
                }); 
        
     
    }
    
    private Router createRouter() {
        Router router = Router.router(vertx);
        router.get("/flight/:id/car/:origin").handler(callPublicFlightAPI());
        router.get("/flight/:id/train/:origin").handler(routingContext -> {
            routingContext.response().end("Here be dragons!");
        });
        return router;
    }
    
    private Handler<RoutingContext> callPublicFlightAPI() {
//    	try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        return routingContext -> {
            String flightId = routingContext.request().getParam("id");
            String origin = routingContext.request().getParam("origin");
            
            //operaciones con bd 
            
            try {
				routingContext.response()
				.putHeader("Content-Type", "application/json")
				.end(mapper.writeValueAsString("Respiesta"));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  
        };
    }
    
	public void process(MORequestJboss requestJBoss) {
		// TODO Auto-generated method stub
		String siteName = this.initConfiguration.getSiteFromNumber(requestJBoss.getShortCode());
		
	}

}
