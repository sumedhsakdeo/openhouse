package com.linkedin.openhouse.tables.controller;

import static com.linkedin.openhouse.common.security.AuthenticationUtils.*;

import com.linkedin.openhouse.tables.api.handler.LanceApiHandler;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceCreateIndexRequest;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceSearchRequest;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceIndexesResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceSearchResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceVersionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Lance-specific API endpoints. These endpoints handle operations unique to the
 * Lance table format: version listing, vector index management, and vector search.
 *
 * <p>This controller is only active when {@code cluster.lance.enabled=true}.
 */
@RestController
@ConditionalOnProperty(name = "cluster.lance.enabled", havingValue = "true")
public class LanceController {

  @Autowired private LanceApiHandler lanceApiHandler;

  @Operation(
      summary = "List versions of a Lance table",
      description = "Returns all versions of a Lance table",
      tags = {"Lance"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lance versions listed successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
      })
  @GetMapping(
      value = "/v1/databases/{databaseId}/tables/{tableId}/lance/v1/versions",
      produces = "application/json")
  public ResponseEntity<LanceVersionsResponse> listVersions(
      @Parameter(description = "Database ID", required = true) @PathVariable String databaseId,
      @Parameter(description = "Table ID", required = true) @PathVariable String tableId) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<LanceVersionsResponse> apiResponse =
        lanceApiHandler.listVersions(databaseId, tableId, extractAuthenticatedUserPrincipal());

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }

  @Operation(
      summary = "Create a vector index on a Lance table",
      description = "Creates a vector index on a specified column of a Lance table",
      tags = {"Lance"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Index created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
      })
  @PostMapping(
      value = "/v1/databases/{databaseId}/tables/{tableId}/lance/v1/indexes",
      produces = "application/json",
      consumes = "application/json")
  public ResponseEntity<Void> createIndex(
      @Parameter(description = "Database ID", required = true) @PathVariable String databaseId,
      @Parameter(description = "Table ID", required = true) @PathVariable String tableId,
      @RequestBody LanceCreateIndexRequest request) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<Void> apiResponse =
        lanceApiHandler.createIndex(
            databaseId, tableId, request, extractAuthenticatedUserPrincipal());

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }

  @Operation(
      summary = "List indexes on a Lance table",
      description = "Returns all vector indexes on a Lance table",
      tags = {"Lance"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Indexes listed successfully"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
      })
  @GetMapping(
      value = "/v1/databases/{databaseId}/tables/{tableId}/lance/v1/indexes",
      produces = "application/json")
  public ResponseEntity<LanceIndexesResponse> listIndexes(
      @Parameter(description = "Database ID", required = true) @PathVariable String databaseId,
      @Parameter(description = "Table ID", required = true) @PathVariable String tableId) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<LanceIndexesResponse> apiResponse =
        lanceApiHandler.listIndexes(databaseId, tableId, extractAuthenticatedUserPrincipal());

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }

  @Operation(
      summary = "Vector search on a Lance table",
      description = "Performs vector similarity search on a Lance table",
      tags = {"Lance"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "404", description = "Table not found"),
      })
  @PostMapping(
      value = "/v1/databases/{databaseId}/tables/{tableId}/lance/v1/search",
      produces = "application/json",
      consumes = "application/json")
  public ResponseEntity<LanceSearchResponse> search(
      @Parameter(description = "Database ID", required = true) @PathVariable String databaseId,
      @Parameter(description = "Table ID", required = true) @PathVariable String tableId,
      @RequestBody LanceSearchRequest request) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<LanceSearchResponse> apiResponse =
        lanceApiHandler.search(databaseId, tableId, request, extractAuthenticatedUserPrincipal());

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }
}
