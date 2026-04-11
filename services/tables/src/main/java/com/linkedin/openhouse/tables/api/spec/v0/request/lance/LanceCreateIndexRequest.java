package com.linkedin.openhouse.tables.api.spec.v0.request.lance;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for creating a vector index on a Lance table column. */
@Builder(toBuilder = true)
@EqualsAndHashCode
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LanceCreateIndexRequest {

  @Schema(description = "Name of the vector column to index", example = "embedding")
  @NotEmpty(message = "column cannot be empty")
  private String column;

  @Schema(
      description = "Index type: IVF_PQ, IVF_HNSW_SQ, etc.",
      defaultValue = "IVF_PQ",
      example = "IVF_PQ")
  @Builder.Default
  private String indexType = "IVF_PQ";

  @Schema(description = "Distance metric: L2, cosine, dot", defaultValue = "L2")
  @Builder.Default
  private String metricType = "L2";

  @Schema(nullable = true, description = "Number of IVF partitions")
  private Integer numPartitions;

  @Schema(nullable = true, description = "Number of PQ sub-vectors")
  private Integer numSubVectors;
}
