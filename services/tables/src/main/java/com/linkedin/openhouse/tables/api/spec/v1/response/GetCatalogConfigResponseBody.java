package com.linkedin.openhouse.tables.api.spec.v1.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.CatalogDefaults;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.CatalogOverrides;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Schema for CatalogConfig, properties are defined here
 * https://iceberg.apache.org/docs/latest/configuration/#catalog-properties
 */
@Builder(toBuilder = true)
@Value
public class GetCatalogConfigResponseBody {

  @Schema(
      description = "Catalog configuration overrides",
      example = "{\"warehouse\": \"s3://my-bucket/\"}")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private CatalogOverrides overrides;

  @Schema(description = "Catalog configuration defaults", example = "{\"clients\": \"4\"}")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private CatalogDefaults defaults;

  public String toJson() {
    return new Gson().toJson(this);
  }
}
