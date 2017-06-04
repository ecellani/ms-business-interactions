package br.com.sysmap.application.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Created by ecellani on 04/06/17.
 */
@Data
@Slf4j
@ToString
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class CustomResponse implements Serializable {

    private Boolean success;
    private ServiceRequestType payload;
    private ResponseError error;

    public CustomResponse(Boolean success, ServiceRequestType payload, ResponseError error) {
        this.error = error;
        this.success = success;
        this.payload = payload;
    }

    public CustomResponse(Boolean success, ServiceRequestType payload) {
        this(success, payload, null);
    }

    public CustomResponse(Boolean success, ResponseError error) {
        this(success, null, error);
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
