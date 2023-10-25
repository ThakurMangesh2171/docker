package com.vassarlabs.gp.controller;


import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.SuccessMessages;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.pojo.ApiResponse;
import com.vassarlabs.gp.pojo.UserPersonalisedDetails;
import com.vassarlabs.gp.service.api.UserPersonalisedService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(description = "List of APIs related to User Personalisation")
public class UserPersonalisationDataController {

    private static final Logger LOGGER = LogManager.getLogger(UserPersonalisationDataController.class);

    @Autowired
    private UserPersonalisedService userPersonalisedService;

    @Autowired
    private ApiResponseService apiResponseService;


    //API for saving/updating User Personalised Data
    @ApiOperation(value = "API for save/update User Personalised data",notes = "It is used for saving/updating User Personalised data")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok")})
    @PostMapping(value = APIConstants.SAVE_USER_PERSONALISED_DATA)
    public ApiResponse saveUserPersonalisedData(@PathVariable String  userId, @RequestBody UserPersonalisedDetails userPersonalisedDetails) throws InvalidValueProvidedException{
        LOGGER.info("In UserPersonalisedController :: saveUserPersonalisedData");
        if(userId==null || userId.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.USER_ID_NULL_ERROR);
        }
        userPersonalisedService.saveUserPersonalisedData(userId,userPersonalisedDetails);
        return apiResponseService.buildApiResponse(HttpStatus.OK, SuccessMessages.USER_PERSONALISED_DATA_SAVED_SUCCESS);
    }


    //API to fetch Particular User Personalised Data
    @ApiOperation(value = "API for Fetching Particular User Personalised Data",notes = "It is used For Fetching ParticularUser Personalised Data")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = UserPersonalisedDetails.class) })
    @GetMapping(value = APIConstants.FETCH_USER_PERSONALISED_DATA)
    public ApiResponse getUserPersonalisedData(@RequestParam String userId) throws InvalidValueProvidedException {
        LOGGER.info("In ApplicationDataController :: getObjectData");
        if(userId==null || userId.trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.USER_ID_NULL_ERROR);
        }
        UserPersonalisedDetails userPersonalisedDetails = userPersonalisedService.getUserPersonalisedData(userId);
        return apiResponseService.buildApiResponse(HttpStatus.OK,userPersonalisedDetails);
    }
}
