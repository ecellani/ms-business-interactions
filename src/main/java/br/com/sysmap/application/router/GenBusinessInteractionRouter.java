package br.com.sysmap.application.router;

import br.com.sysmap.application.domain.CustomResponse;
import br.com.sysmap.application.domain.ServiceRequestType;
import br.com.sysmap.infrastructure.config.ApplicationConfig;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.camel.model.rest.RestBindingMode.json;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Created by ecellani on 04/06/17.
 */
@Component
public class GenBusinessInteractionRouter extends RouteBuilder {

    @Autowired
    private ApplicationConfig config;

    @Override
    public void configure() throws Exception {

        restConfiguration()
            .component("servlet")
            .bindingMode(json)
            .dataFormatProperty("prettyPrint", "true")
            .apiContextPath(config.getPath().getApiDoc())
                .apiProperty("api.title", config.getPath().getApiTitle())
                .apiProperty("api.version", config.getPath().getApiVersion())
                .apiProperty("cors", "true");

        rest(config.getPath().getBusinessInteractions())
            .description(config.getPath().getBusinessInteractionsDesc())
            .consumes(APPLICATION_JSON_UTF8_VALUE)
            .produces(APPLICATION_JSON_UTF8_VALUE)

        .post().description("Generate the business id").type(ServiceRequestType.class).outType(CustomResponse.class)
            .responseMessage()
                .code(OK.value())
                .message("Custom Response with the business interaction ID")
            .endResponseMessage()
            .to("direct:business-interaction-generate")
        ;
   }
}
