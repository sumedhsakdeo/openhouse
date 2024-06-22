package com.linkedin.openhouse.tables.api.spec.v1.response.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class IcebergRestCatalogOverrides {

  @Schema(description = "Root path of the data warehouse", example = "s3://my-bucket/")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String warehouse;
}
