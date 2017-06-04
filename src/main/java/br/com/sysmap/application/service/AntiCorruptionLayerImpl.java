package br.com.sysmap.application.service;

import br.com.sysmap.application.domain.ResponseError;
import br.com.sysmap.application.domain.ServiceRequestType;
import br.com.sysmap.webservice.customer.selfempowered.GenerateBusinessInteractionOut;
import org.springframework.stereotype.Service;

import static br.com.sysmap.infrastructure.mapper.AntiCorruptionFunctions.externalTypeToServiceRequestType;

/**
 * Created by ecellani on 04/06/17.
 */
@Service("antiCorruptionLayer")
public class AntiCorruptionLayerImpl implements AntiCorruptionLayer {

    @Override
    public Object transform(Object in, ServiceRequestType initialBody) throws Exception {
        if (in instanceof GenerateBusinessInteractionOut) {
            ServiceRequestType transformed = externalTypeToServiceRequestType.apply((GenerateBusinessInteractionOut) in);
            String businessId = transformed != null ? transformed.getBusinessId() : null;
            return new ServiceRequestType(initialBody.getServiceId(), initialBody.getChannel(), initialBody.getId(), businessId);
        } else if (in instanceof ResponseError) {
            return in;
        }
        throw new Exception("Unknown result object");
    }
}
