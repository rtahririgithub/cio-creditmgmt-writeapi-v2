package com.telus.credit.api;

import static com.telus.credit.common.CreditMgmtCommonConstants.HEADER_CORR_ID;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.telus.credit.exceptions.CreditException;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.firestore.model.CreditAssessment;
import com.telus.credit.pubsub.model.CreditAssessmentEvent;
import com.telus.credit.service.impl.CreditAssessmentMessageSender;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = {"Customer Credit Profile Assessment Change Listener"}, produces = "application/json")
@RequestMapping(produces = "application/json")
@Validated
public class AssessmentChangeListenerAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentChangeListenerAPI.class);

    @Autowired
    private CreditAssessmentMessageSender messageSender;

    @PatchMapping(path = "/v1/customer/listener/assessmentChangeEvent")
    @ApiOperation(value = "Receive and publish assessment message to topic")
    public ResponseEntity<Object> newAssessmentMessage(HttpServletRequest request,
                                               @ApiParam(value = "Event", required = true) @RequestBody CreditAssessmentEvent event) {

        boolean status = !(Objects.isNull(event) || Objects.isNull(event.getEvent()) || event.getEvent().isEmpty());
        if (status) {
            for (CreditAssessment e : event.getEvent()) {
                if (Objects.isNull(e) || Objects.isNull(e.getCustomerId())) {
                    status = false;
                    break;
                }
            }
        }

        if (!status) { 
            String eventStr = (event!=null)?("["+event.toString()+"]"):"";
            throw new CreditException(HttpStatus.BAD_REQUEST, ExceptionConstants.ERR_CODE_1000, "Customer info is missing", ExceptionConstants.ERR_CODE_1000_MSG,eventStr);
        }

        HttpStatus retCode = HttpStatus.OK;
        try {
            messageSender.publish(event);
            LOGGER.info("Message published {}", event);
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            LOGGER.error("{}: {} Error publishing event[ {} ]. {}", ExceptionConstants.STACKDRIVER_METRIC, ExceptionConstants.PUBSUB200, event, ExceptionHelper.getStackTrace(e));
            
            retCode = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(retCode).body(Collections.singletonMap(HEADER_CORR_ID, request.getAttribute(HEADER_CORR_ID)));
    }

    @PostMapping(path = "/listener/v1/customer/assessmentChangeEvent")
    @ApiOperation(value = "Receive and publish assessment message to topic")
    public ResponseEntity<Object> newAssessmentMessagePost(HttpServletRequest request,
                                               @ApiParam(value = "Event", required = true) @RequestBody CreditAssessmentEvent event) {

       return newAssessmentMessage(request, event);
    }

}
