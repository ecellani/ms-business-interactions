package br.com.sysmap.application.router;

import br.com.sysmap.application.domain.CustomResponse;
import br.com.sysmap.infrastructure.config.ApplicationConfig;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static javax.ws.rs.HttpMethod.*;
import static org.apache.camel.Exchange.*;
import static org.apache.camel.component.rabbitmq.RabbitMQConstants.DELIVERY_MODE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.boot.autoconfigure.jms.JmsProperties.DeliveryMode.PERSISTENT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Created by ecellani on 04/06/17.
 */
@Component
public class InOutRouter extends RouteBuilder {

    @Autowired
    private ApplicationConfig config;

    private static final String CUSTOM_HTTP_METHOD = "HTTP_METHOD";

    @Override
    public void configure() throws Exception {

        from("direct:business-interaction-generate")
            .setHeader(DELIVERY_MODE, constant(PERSISTENT.getValue()))
            .setHeader(CUSTOM_HTTP_METHOD, header(HTTP_METHOD))
            .inOut(config.getQueues().getBusinessInteractionsGenerate()) // Send to rabbit using InOut exchange pattern
            .choice()
                .when(header(CUSTOM_HTTP_METHOD).isEqualTo(GET))
                    .to("bean:genBusinessInteraction?method=get(${header.businessid})")
                .when(header(CUSTOM_HTTP_METHOD).isEqualTo(DELETE))
                    .to("bean:genBusinessInteraction?method=delete(${header.businessid})")
                .when(header(CUSTOM_HTTP_METHOD).isEqualTo(POST))
                    .process(exchange -> {
                        if (isBlank(exchange.getIn().getBody(String.class)))
                            exchange.getIn().setBody(null);
                    })
                    .to("bean:genBusinessInteraction?method=generate")
                .otherwise()
                    .setHeader(HTTP_RESPONSE_CODE, constant(BAD_REQUEST.value()))
            .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON_UTF8_VALUE))
            .process(exchange -> {
                int responseStatus = OK.value();
                CustomResponse customResponse = exchange.getIn().getBody(CustomResponse.class);

                if (customResponse.getError() != null) {
                    if ("BUSINESS_ERROR".equals(customResponse.getError().getResponseStatus()))
                        responseStatus = BAD_REQUEST.value();
                    else if (NOT_FOUND.name().equals(customResponse.getError().getResponseStatus()))
                        responseStatus = NOT_FOUND.value();
                    else
                        responseStatus = INTERNAL_SERVER_ERROR.value();
                }

                if (customResponse.getSuccess()
                        && customResponse.getPayload() == null)
                    responseStatus = NOT_FOUND.value();

                if (responseStatus != OK.value())
                    exchange.getIn().setBody(customResponse.toJson());

                Map<String, Object> headers = exchange.getIn().getHeaders();
                headers.put(HTTP_RESPONSE_CODE, responseStatus);
                headers.put(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE);
            })
        .end();

        // Rabbit reply
        from(config.getQueues().getBusinessInteractionsGenerate())
            .removeHeaders("CamelHttp*")
        .end();
    }
}
