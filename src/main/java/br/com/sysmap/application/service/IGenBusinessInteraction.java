package br.com.sysmap.application.service;

import br.com.sysmap.application.domain.CustomResponse;
import br.com.sysmap.application.domain.ServiceRequestType;

/**
 * Created by ecellani on 04/06/17.
 */
public interface IGenBusinessInteraction {

    CustomResponse generate(ServiceRequestType serviceRequestType);
}
