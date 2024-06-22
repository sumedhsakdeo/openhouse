package com.linkedin.openhouse.tables.api.spec.v1.response.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class IcebergRestCatalogDefaults {

  @Schema(description = "Client pool size", example = "4")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private int clients;
}
