package com.lamark.business.vivobr.core.exception;
 
import java.io.Serializable;

import com.lamark.business.vivobr.core.dto.ExceptionDto;

public class VivoBrException extends Exception implements Serializable {
	private static final long serialVersionUID = 1L;
	private ExceptionDto exceptionDto;
	
	public VivoBrException(ExceptionDto dto) {
		super(dto.getMessage());
		this.exceptionDto=dto;
	}

	
	public ExceptionDto getExceptionDto() {
		return exceptionDto;
	}
}
