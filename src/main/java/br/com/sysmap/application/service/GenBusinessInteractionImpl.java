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

/**
 * Created by ecellani on 04/06/17.
 */
@Slf4j
@Service("genBusinessInteraction")
public class GenBusinessInteractionImpl implements IGenBusinessInteraction {

    @Produce
    private ProducerTemplate template;

    @Autowired
    private RedisTemplate<Integer, ServiceRequestType> redisTemplate;

    @Override
    public CustomResponse generate(ServiceRequestType serviceRequestType) {

        ServiceRequestType cache = getCache(serviceRequestType);
        if (cache != null)
            return new CustomResponse(true, cache);

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

    private ServiceRequestType getCache(ServiceRequestType serviceRequestType) {
        try {
            if (redisTemplate.hasKey(serviceRequestType.hashCode())) {
                log.info("Key {} has been found in cache", serviceRequestType.hashCode());
                return redisTemplate.opsForValue().get(serviceRequestType.hashCode());
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to Redis. The service is not available"/*, e*/);
        }
        return null;
    }

    private void saveCache(ServiceRequestType serviceRequestType) {
        try {
            redisTemplate.opsForValue().set(serviceRequestType.hashCode(), serviceRequestType);
            log.info("Key {} has been added to cache", serviceRequestType.hashCode());
        } catch (RedisConnectionFailureException e) {
            log.warn("Cannot connect to Redis. The service is not available"/*, e*/);
        }
    }

    private Object generateBusinessInteractionWS(ServiceRequestType serviceRequestType) throws ExecutionException, InterruptedException {
        return template.asyncSendBody("direct:integration-business-interaction-generate", serviceRequestType).get();
    }
}
