package com.lamark.business.vivobr.api;


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
import io.quarkus.runtime.StartupEvent;

@Path("/vivo")
@Produces(MediaType.APPLICATION_JSON)
public class MOController extends BaseController {
	
	@Inject
	 private MOService moService;
	

	
	@POST
	@Path("/moRequest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)


	   public Response processMo( MORequestJboss requestJBoss){
		moService.process(requestJBoss);
		return buildSuccessWrapperResponse(null);
	 }


	  
}