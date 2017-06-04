package br.com.sysmap.application.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Created by ecellani on 04/06/17.
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class ResponseErrorMessage implements Serializable {

    private String system;
    private String statusCode;
    private String description;
    private String statusDetail;
}
