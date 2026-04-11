package com.linkedin.openhouse.tables.api.spec.v0.response.lance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** Response body listing Lance table versions. */
@Builder(toBuilder = true)
@Value
public class LanceVersionsResponse {

  @Schema(description = "List of table versions")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private List<VersionInfo> versions;

  /** Information about a single table version. */
  @Builder
  @Value
  public static class VersionInfo {
    @Schema(description = "Version number")
    private int version;

    @Schema(description = "Timestamp of this version", nullable = true)
    private String timestamp;

    @Schema(description = "Version metadata")
    private Map<String, String> metadata;
  }
}
