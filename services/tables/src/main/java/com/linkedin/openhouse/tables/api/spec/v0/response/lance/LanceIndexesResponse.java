package com.linkedin.openhouse.tables.api.spec.v0.response.lance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/** Response body listing indexes on a Lance table. */
@Builder(toBuilder = true)
@Value
public class LanceIndexesResponse {

  @Schema(description = "List of indexes on the table")
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private List<IndexInfo> indexes;

  /** Information about a single vector index. */
  @Builder
  @Value
  public static class IndexInfo {
    @Schema(description = "Indexed columns")
    private List<String> columns;

    @Schema(description = "Index type (e.g. IVF_PQ, IVF_HNSW_SQ)")
    private String indexType;

    @Schema(description = "Index name", nullable = true)
    private String name;
  }
}
