package com.linkedin.openhouse.tables.api.handler;

import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceCreateIndexRequest;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceSearchRequest;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceIndexesResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceSearchResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceVersionsResponse;

/**
 * Interface for Lance-specific API operations. Handles requests that are unique to the Lance table
 * format (vector search, index management, version listing).
 */
public interface LanceApiHandler {

  /**
   * List all versions of a Lance table.
   *
   * @param databaseId Database identifier
   * @param tableId Table identifier
   * @param actingPrincipal Authenticated user
   * @return Response containing the list of versions
   */
  ApiResponse<LanceVersionsResponse> listVersions(
      String databaseId, String tableId, String actingPrincipal);

  /**
   * Create a vector index on a Lance table column.
   *
   * @param databaseId Database identifier
   * @param tableId Table identifier
   * @param request Index creation parameters
   * @param actingPrincipal Authenticated user
   * @return Response confirming index creation
   */
  ApiResponse<Void> createIndex(
      String databaseId,
      String tableId,
      LanceCreateIndexRequest request,
      String actingPrincipal);

  /**
   * List all indexes on a Lance table.
   *
   * @param databaseId Database identifier
   * @param tableId Table identifier
   * @param actingPrincipal Authenticated user
   * @return Response containing the list of indexes
   */
  ApiResponse<LanceIndexesResponse> listIndexes(
      String databaseId, String tableId, String actingPrincipal);

  /**
   * Perform vector similarity search on a Lance table.
   *
   * @param databaseId Database identifier
   * @param tableId Table identifier
   * @param request Search parameters (query vector, column, filters, etc.)
   * @param actingPrincipal Authenticated user
   * @return Response containing search results
   */
  ApiResponse<LanceSearchResponse> search(
      String databaseId,
      String tableId,
      LanceSearchRequest request,
      String actingPrincipal);
}
