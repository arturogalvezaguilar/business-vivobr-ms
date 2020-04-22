package com.lamark.business.vivobr.core.handler;

import com.lamark.business.vivobr.api.BaseController;
import com.lamark.business.vivobr.core.exception.VivoBrException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class VivoBrExceptionHandler extends BaseController implements ExceptionMapper<VivoBrException> {

	public Response toResponse(VivoBrException e) {
		// TODO Auto-generated method stub
		return buildBadRequestWrapperResponse(e.getExceptionDto());
	}
 
}
