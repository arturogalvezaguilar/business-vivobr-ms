package com.lamark.business.vivobr.api;

import javax.ws.rs.core.Response;
import java.util.List;

public class BaseController {
	
	public <T> Response buildSuccessWrapperResponse(List<T> list) {
		return Response.status(Response.Status.ACCEPTED).entity(list).build();
	}
	
	public <T> Response buildSuccessWrapperResponse(T data) {
		return Response.status(Response.Status.ACCEPTED).entity(data).build();
	}
	
	public <T> Response buildSuccessWrapperResponse() {
		return Response.status(Response.Status.ACCEPTED).build();
	}
	
	public <T> Response buildBadRequestWrapperResponse(T data) {
		return Response.status(Response.Status.BAD_REQUEST).entity(data).build();
	}
}
