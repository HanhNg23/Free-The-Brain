package com.freethebrain.payload;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import lombok.Data;

/*
This class i use generic to create
ApiResponse for flexible object response
*/
@Data
public class ApiResponse<T> {

	private boolean success;
	
	private HttpStatus status;

	private String message;

	@SuppressWarnings("rawtypes")
	private T object;

	public ApiResponse(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
		this.status = null;
		this.object = null;
	}
	
	public ApiResponse(boolean success, String message, T object) {
		super();
		this.success = success;
		this.message = message;
		this.status = null;
		this.object = object;
	}

	public ApiResponse(@Nullable HttpStatus status, T object) {
		super();
		this.status = status;
		this.object = object;
	}
	
}
