package com.linkedin.openhouse.tables.api.spec.v0.response.lance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** Response body containing vector search results from a Lance table. */
@Builder(toBuilder = true)
@Value
public class LanceSearchResponse {

  @Schema(description = "List of search results ordered by distance")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private List<SearchResult> results;

  /** A single search result row with its distance score. */
  @Builder
  @Value
  public static class SearchResult {
    @Schema(description = "Row data as key-value pairs")
    private Map<String, Object> data;

    @Schema(description = "Distance from the query vector")
    private double distance;
  }
}
