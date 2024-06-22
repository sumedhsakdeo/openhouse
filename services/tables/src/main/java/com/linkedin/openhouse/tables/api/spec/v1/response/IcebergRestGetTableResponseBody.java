package com.linkedin.openhouse.tables.api.spec.v1.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class IcebergRestGetTableResponseBody {

  @Schema(
      description = "Metadata location for the table",
      example = "s3://my-bucket/my-table/<uuid>-1.json")
  @JsonProperty(
      access = JsonProperty.Access.READ_ONLY,
      required = false,
      value = "metadata-location")
  private String metadataLocation;
}
