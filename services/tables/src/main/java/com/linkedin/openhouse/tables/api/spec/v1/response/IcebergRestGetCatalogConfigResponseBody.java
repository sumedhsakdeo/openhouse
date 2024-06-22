package com.linkedin.openhouse.tables.api.spec.v1.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.IcebergRestCatalogDefaults;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.IcebergRestCatalogOverrides;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Schema for CatalogConfig, properties are defined here
 * https://iceberg.apache.org/docs/latest/configuration/#catalog-properties
 */
@Builder(toBuilder = true)
@Value
public class IcebergRestGetCatalogConfigResponseBody {

  @Schema(
      description = "Catalog configuration overrides",
      example = "{\"warehouse\": \"s3://my-bucket/\"}")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private IcebergRestCatalogOverrides overrides;

  @Schema(description = "Catalog configuration defaults", example = "{\"clients\": \"4\"}")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private IcebergRestCatalogDefaults defaults;

  public String toJson() {
    return new Gson().toJson(this);
  }
}
