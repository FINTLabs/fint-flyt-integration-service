package no.fintlabs.integration.model.dtos;

import lombok.Data;

@Data
public class ConfigurationDto {

    private Long id;

    private Long integrationId;

    private Long integrationMetadataId;

    private boolean completed;

    private String comment;

    private Integer version;

}
