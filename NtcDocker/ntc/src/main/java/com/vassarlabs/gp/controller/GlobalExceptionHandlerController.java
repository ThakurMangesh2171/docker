package com.vassarlabs.gp.controller;

import javax.servlet.http.HttpServletRequest;

import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.pojo.ApiResponse;
import com.vassarlabs.gp.service.impl.ApiResponseService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

	@Autowired
	ApiResponseService apiResponseService;

	private static final Logger LOGGER = LogManager.getLogger(GlobalExceptionHandlerController.class);

	// Handlers for custom (or) user defined exceptions
	// Resource Not Found Exception - When database is queried for non-existent
	// resource
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = ResourceNotFoundException.class)
	public ApiResponse handleResourceNotFoundException(HttpServletRequest request, Exception ex) {
		LOGGER.debug("Inside Resource Not Found Exception");
		LOGGER.error("Request endpoint " + request.getRequestURL() + " Exception " + ex.getMessage());

		// Log the class name and method name where exception occurred
		this.logExceptionLocation(ex);

		// Get the message from exceptions If not present get the default error message.
		String message = ex.getMessage() != null ? ex.getMessage() : ErrorMessages.RESOURCE_NOT_FOUND;

		// Build the api response
		return apiResponseService.buildApiResponse(HttpStatus.NOT_FOUND, message);
	}
	
    //Input Parameters Related Exceptions - When parameters don't satisfy the required conditions
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
			InvalidValueProvidedException.class,
			FileNotFoundException.class})
    public ApiResponse handleInputParameterRelatedExceptions (HttpServletRequest request, Exception ex) {
        LOGGER.debug("Inside Input Parameters Related Exceptions");
        LOGGER.error("Request endpoint " + request.getRequestURL() + " Exception " + ex.getMessage());

        //Log the class name and method name where exception occurred
        this.logExceptionLocation(ex);

        //Build the api response
        return apiResponseService.buildApiResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    //Method Not Allowed / Supported Exception
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResponse handleMethodNotSupportedException (HttpServletRequest request, Exception ex) {
        LOGGER.debug("Inside Method Not Supported Exception Handler");
        LOGGER.error("Request endpoint " + request.getRequestURL() + " Exception " + ex.getMessage());

        //Log the class name and method name where exception occurred
        this.logExceptionLocation(ex);

        //Build the api response
        return apiResponseService.buildApiResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorMessages.METHOD_NOT_ALLOWED);
    }

	// Method which logs the class and method name where the exception occurred
	// N.B : Only the top level exception is considered
	private void logExceptionLocation(Exception ex) {
		// Check if stack trace exists for the method before logging it
		if (ex.getStackTrace().length > 0) {
			StackTraceElement stackTraceElement = ex.getStackTrace()[0];

			LOGGER.debug("Class Name " + stackTraceElement.getClassName());
			LOGGER.debug("Method Name " + stackTraceElement.getMethodName());
		} else {
			LOGGER.debug("No stack trace information found ");
		}
	}

	//Handler for Specific Runtime Exceptions
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ NullPointerException.class, IOException.class, SQLException.class, DecoderException.class})
	public ApiResponse handleSpecificExceptions (HttpServletRequest request, Exception ex) {
		LOGGER.debug("Inside handler of specific runtime exceptions");
		LOGGER.error("Request endpoint " + request.getRequestURL() + " Exception " + ex.getMessage());

		ex.printStackTrace();

		//Log the class name and method name where exception occurred
		this.logExceptionLocation(ex);

		//Build the api response
		return apiResponseService.buildApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR);
	}


	//Handler for Other Runtime Exceptions
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({
			DataIntegrityViolationException.class
	})
	public ApiResponse handleDataIntegrityViolationExceptions (HttpServletRequest request, Exception ex) {
		LOGGER.debug("Inside handler of Data integrity violation exceptions");
		LOGGER.error("Request endpoint " + request.getRequestURL() + " Exception " + ex.getMessage());
		ex.printStackTrace();

		//Log the class name and method name where exception occurred
		this.logExceptionLocation(ex);

		//Build the api response
		return apiResponseService.buildApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}
}
