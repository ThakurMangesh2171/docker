package com.vassarlabs.gp.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.vassarlabs.gp.pojo.ApiResponse;

/**
 * This service is used to create the appropriate success and error responses
 * in the specified format to be returned to the client application
 */
@Service
public class ApiResponseService {

    public ApiResponse createSuccessResponse(Boolean result, Integer statusCode, String description, Object response) {
        ApiResponse successResponse = new ApiResponse();

        successResponse.setResult(result);
        successResponse.setStatusCode(statusCode);
        successResponse.setStatusCodeDescription(description);
        successResponse.setResponse(response);

        return successResponse;
    }

    public ApiResponse createErrorResponse(Boolean result, Integer statusCode, String description, String message) {
        ApiResponse errorResponse = new ApiResponse();

        errorResponse.setResult(result);
        errorResponse.setStatusCode(statusCode);
        errorResponse.setStatusCodeDescription(description);
        errorResponse.setMessage(message);

        return errorResponse;
    }

    //Method which builds the api error response
    public ApiResponse buildApiResponse(HttpStatus httpStatus, String message) {
    	Boolean result = httpStatus.value() == 201 || httpStatus.value() == 200;
    	
        //Construct the api response
        return this.createErrorResponse(
        		result,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message);
    }

    //Method which builds the api success response
    public ApiResponse buildApiResponse(HttpStatus httpStatus, Object response) {
        //Construct the api response
        return this.createSuccessResponse(
                true,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                response);
    }

    public ApiResponse createSuccessResponse(Boolean result, Integer statusCode, String description,String message, Object response) {
        ApiResponse successResponse = new ApiResponse();

        successResponse.setResult(result);
        successResponse.setStatusCode(statusCode);
        successResponse.setStatusCodeDescription(description);
        successResponse.setMessage(message);
        successResponse.setResponse(response);

        return successResponse;
    }
    public ApiResponse buildApiResponse(HttpStatus httpStatus, String message,Object response) {
        Boolean result = httpStatus.value() == 201 || httpStatus.value() == 200;

        //Construct the api response
        return this.createSuccessResponse(
                result,
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                response);
    }
}
