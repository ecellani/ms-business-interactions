package br.com.sysmap.application.service;

import br.com.sysmap.application.domain.CustomResponse;
import br.com.sysmap.application.domain.ResponseError;
import br.com.sysmap.application.domain.ServiceRequestType;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Created by ecellani on 04/06/17.
 */
@Slf4j
@Service("genBusinessInteraction")
public class GenBusinessInteractionImpl implements IGenBusinessInteraction {

    @Produce
    private ProducerTemplate template;

    @Autowired
    private RedisTemplate<String, ServiceRequestType> redisTemplate;

    @Override
    public CustomResponse generate(ServiceRequestType serviceRequestType) {

        if (serviceRequestType == null
                || serviceRequestType.getId() == null
                || serviceRequestType.getServiceId() == null
                || serviceRequestType.getChannel() == null) {
            ResponseError responseError = new ResponseError(BAD_REQUEST.name(), BAD_REQUEST.getReasonPhrase());
            return new CustomResponse(false, responseError);
        }

        ServiceRequestType cache = getCache(serviceRequestType);
        if (cache != null) {
            ResponseError responseError = new ResponseError("BUSINESS_ERROR", "Already exists");
            return new CustomResponse(false, responseError);
        }

        CustomResponse customResponse;
        try {
            Object result = generateBusinessInteractionWS(serviceRequestType);

            if (result instanceof ServiceRequestType) {
                ServiceRequestType serviceRequestTypeResult = (ServiceRequestType) result;
                customResponse = new CustomResponse(true, serviceRequestTypeResult);
                saveCache(serviceRequestTypeResult);

            } else if (result instanceof ResponseError) {
                customResponse = new CustomResponse(false, (ResponseError) result);
            } else {
                throw new Exception("Unknown result object");
            }
            log.info(customResponse.toString());
        } catch (Exception e) {
            ResponseError responseError = new ResponseError("FATAL_ERROR", e.getMessage());
            customResponse = new CustomResponse(false, responseError);
            log.error(e.getMessage(), e);
        }
        return customResponse;
    }

    public CustomResponse get(String businessId) {
        ServiceRequestType cache = getCache(businessId);
        if (cache != null) {
            return new CustomResponse(true, cache);
        } else {
            ResponseError responseError = new ResponseError(NOT_FOUND.name(), "Key not found");
            return new CustomResponse(false, responseError);
        }
    }

    public CustomResponse delete(String businessId) {
        ServiceRequestType cache = getCache(businessId);
        if (cache == null) {
            ResponseError responseError = new ResponseError(NOT_FOUND.name(), "Key not found");
            return new CustomResponse(false, responseError);
        }
        deleteCache(cache);
        return new CustomResponse(true);
    }

    private void saveCache(ServiceRequestType serviceRequestType) {
        try {
            redisTemplate.opsForValue().set(serviceRequestType.hashCode()+"", serviceRequestType);
            redisTemplate.opsForValue().set(serviceRequestType.getBusinessId(), serviceRequestType);
            log.info("Key {} has been added to cache", serviceRequestType.hashCode());
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to Redis. The service is not available", e);
        }
    }

    private ServiceRequestType getCache(ServiceRequestType serviceRequestType) {
        return getCache(serviceRequestType.hashCode()+"");
    }

    private ServiceRequestType getCache(String key) {
        try {
            if (redisTemplate.hasKey(key)) {
                log.info("Key {} has been found in cache", key);
                return redisTemplate.opsForValue().get(key);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to Redis. The service is not available", e);
        }
        return null;
    }

    private void deleteCache(ServiceRequestType serviceRequestType) {
        redisTemplate.delete(serviceRequestType.hashCode()+"");
        redisTemplate.delete(serviceRequestType.getBusinessId());
    }

    private Object generateBusinessInteractionWS(ServiceRequestType serviceRequestType) throws ExecutionException, InterruptedException {
        return template.asyncSendBody("direct:integration-business-interaction-generate", serviceRequestType).get();
    }
}
