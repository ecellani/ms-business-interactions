package br.com.sysmap.infrastructure.mapper;

import br.com.sysmap.application.domain.ServiceRequestType;
import br.com.sysmap.webservice.customer.selfempowered.GenerateBusinessInteractionOut;

import java.util.function.Function;

/**
 * Created by ecellani on 04/06/17.
 */
public interface AntiCorruptionFunctions {

    Function<GenerateBusinessInteractionOut, ServiceRequestType> externalTypeToServiceRequestType = requestTypeOut -> {
        if (requestTypeOut == null)
            return null;

        ServiceRequestType serviceRequestType = new ServiceRequestType();
        if (requestTypeOut.getBusinessInteraction() != null)
            serviceRequestType.setBusinessId(requestTypeOut.getBusinessInteraction().getID());

        return serviceRequestType;
    };
}
