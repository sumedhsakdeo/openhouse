package com.linkedin.openhouse.tables.api.spec.v0.request.lance;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for performing vector similarity search on a Lance table. */
@Builder(toBuilder = true)
@EqualsAndHashCode
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LanceSearchRequest {

  @Schema(description = "Query vector for similarity search", required = true)
  @NotNull(message = "queryVector cannot be null")
  private List<Float> queryVector;

  @Schema(description = "Name of the vector column to search against", example = "embedding")
  @NotEmpty(message = "vectorColumn cannot be empty")
  private String vectorColumn;

  @Schema(description = "Maximum number of results to return", defaultValue = "10")
  @Builder.Default
  @Min(1)
  private int limit = 10;

  @Schema(
      nullable = true,
      description = "Optional SQL filter expression (e.g. \"category = 'news'\")")
  private String filter;

  @Schema(description = "Distance metric: L2, cosine, dot", defaultValue = "L2")
  @Builder.Default
  private String metricType = "L2";

  @Schema(description = "Number of probes for IVF search", defaultValue = "20")
  @Builder.Default
  @Min(1)
  private int nprobes = 20;
}
