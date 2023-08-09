package no.fintlabs.integration.model.dtos;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class ConfigurationDto {

    private Long id;

    private Long integrationId;

    private Long integrationMetadataId;

    private boolean completed;

    private String comment;

    private Integer version;

}
