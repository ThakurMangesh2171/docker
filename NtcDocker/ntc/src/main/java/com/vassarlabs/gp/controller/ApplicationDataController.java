package com.vassarlabs.gp.controller;

import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.SuccessMessages;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.pojo.ApiResponse;
import com.vassarlabs.gp.pojo.PlanDetails;
import com.vassarlabs.gp.pojo.ResponseRfpExcelResponse;
import com.vassarlabs.gp.pojo.ApplicationDataDetails;
import com.vassarlabs.gp.service.api.ApplicationDataService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Api(description = "List of APIs related to Application Data")
public class ApplicationDataController {

    private static final Logger LOGGER = LogManager.getLogger(ApplicationDataController.class);

    @Autowired
    private ApplicationDataService applicationDataService;

    @Autowired
    private ApiResponseService apiResponseService;


    //API to fetch List Of application Data with Optional status, type and subtype
    @ApiOperation(value = "API to fetch List Of Application Data with Filters on status, type and subType",notes = "It is used for fetching List Of Application Data with Filters on status, type and subType")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ApplicationDataDetails.class, responseContainer = "List")})
    @GetMapping(value = APIConstants.GET_ALL_APPLICATION_DATA)
    public ApiResponse getAllApplicationData(@RequestParam Optional<String> type, @RequestParam Optional<String> subType , @RequestParam Optional<String> status) {
        LOGGER.info("In ApplicationDataController :: getAllApplicationData");

        //TODO Need to validate for valid type and subTye if any
        List<ApplicationDataDetails> applicationDataDetailsList = applicationDataService.getAllApplicationData(type,subType,status);
        return apiResponseService.buildApiResponse(HttpStatus.OK, applicationDataDetailsList);
    }


    @ApiOperation(value = "API for Fetching Particular Object Data",notes = "It is used For Fetching Particular Object Data")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = ApplicationDataDetails.class) })
    @GetMapping(value = APIConstants.FETCH_APPLICATION_DATA_BY_TYPE_AND_ID)
    public ApiResponse getObjectData(@RequestParam String type, @RequestParam String objectId) throws InvalidValueProvidedException, ResourceNotFoundException {
        LOGGER.info("In ApplicationDataController :: getObjectData");
        if(type==null || type.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.TYPE_NULL_ERROR);
        }
        if(objectId==null || objectId.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.OBJECT_ID_NULL_ERROR);
        }
        ApplicationDataDetails applicationDataDetails = applicationDataService.getObjectDataByTypeAndId(type,objectId);
        return apiResponseService.buildApiResponse(HttpStatus.OK,applicationDataDetails);
    }



    //API to Deleting Particular Simulation Data
    @ApiOperation(value = "API for deleting Particular Object Data",notes = "It is used For deleting Particular Object Data")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })
    @PostMapping(value = APIConstants.DELETE_APPLICATION_DATA)
    public ApiResponse deleteApplicationData(@PathVariable String type, @PathVariable String objectId) throws InvalidValueProvidedException, ResourceNotFoundException {
        LOGGER.info("In ApplicationDataController :: deleteApplicationData");
        if(type==null || type.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.TYPE_NULL_ERROR);
        }
        if(objectId==null || objectId.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.OBJECT_ID_NULL_ERROR);
        }
        applicationDataService.deleteApplicationDataByTypeAndId(type,objectId);
        return apiResponseService.buildApiResponse(HttpStatus.OK, SuccessMessages.APPLICATION_DATA_DELETED_SUCCESS);
    }



    // api for save/update simulation data
    @ApiOperation(value = "api for save/update Particular Object data",notes = "It is used for saving/updating Particular Object data")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok")})
    @PostMapping(value = APIConstants.SAVE_APPLICATION_DATA)
    public ApiResponse saveApplicationData(@RequestBody @Valid ApplicationDataDetails applicationDataDetails){
        LOGGER.info("In ApplicationDataController :: saveApplicationData");
        String applicationId = applicationDataService.saveApplicationData(applicationDataDetails);
        return apiResponseService.buildApiResponse(HttpStatus.OK,SuccessMessages.APPLICATION_DATA_SAVED_SUCCESS,applicationId);
    }


    //API to fetch Plan Id and Option Id for Particular ObjectId and type and Sub type
    @ApiOperation(value = "API to fetch Plan Id and Option Id for Particular ObjectId of given type and subType",notes = "It is used for fetching Plan Id and Option Id for Particular ObjectId of given type and subType")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = PlanDetails.class)})
    @GetMapping(value = APIConstants.GET_PLAN_AND_OPTION_ID)
    public ApiResponse getPlanAndOptionId(@RequestParam String objectId, @RequestParam String type, @RequestParam String subType) throws ResourceNotFoundException {
        LOGGER.info("In ApplicationDataController :: getPlanAndOptionId");
        //TODO Need to validate for valid type and subTye if any
        PlanDetails planDetails = applicationDataService.getPlanAndOptionId(objectId,type,subType);
        return apiResponseService.buildApiResponse(HttpStatus.OK, planDetails);
    }


}
