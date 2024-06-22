package com.linkedin.openhouse.tables.controller;

import com.linkedin.openhouse.tables.api.handler.CatalogApiHandler;
import com.linkedin.openhouse.tables.api.handler.IcebergRestTablesApiHandler;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetCatalogConfigResponseBody;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetTableResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class implements the Iceberg REST API spec for the tables service. The REST API spec is
 * defined in the OpenAPI 3.0 format in
 * https://github.com/apache/iceberg/blob/main/open-api/rest-catalog-open-api.yaml
 */
@RestController
public class IcebergRestController {

  @Autowired private CatalogApiHandler catalogApiHandler;

  @Autowired private IcebergRestTablesApiHandler icebergRestTablesApiHandler;

  @Operation(
      summary = "List all catalog configuration settings",
      description =
          "All REST clients should first call this route to get catalog configuration properties from the server to configure the catalog and its HTTP client. Configuration from the server consists of two sets of key/value pairs.\n"
              + "\n"
              + "defaults - properties that should be used as default configuration; applied before client configuration\n"
              + "overrides - properties that should be used to override client configuration; applied after defaults and client configuration\n"
              + "Catalog configuration is constructed by setting the defaults, then client- provided configuration, and finally overrides. The final property set is then used to configure the catalog.\n"
              + "\n"
              + "For example, a default configuration property might set the size of the client pool, which can be replaced with a client-specific setting. An override might be used to set the warehouse location, which is stored on the server rather than in client configuration.\n"
              + "\n"
              + "Common catalog configuration settings are documented at https://iceberg.apache.org/docs/latest/configuration/#catalog-properties\n"
              + "\n.",
      tags = {"Configuration"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Server specified configuration values."),
        @ApiResponse(
            responseCode = "401",
            description =
                "Unauthorized. Authentication is required and has failed or has not yet been provided."),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. Authenticated user does not have the necessary permissions."),
        @ApiResponse(
            responseCode = "419",
            description =
                "Credentials have timed out. If possible, the client should refresh credentials and retry.")
      })
  @GetMapping(
      value = {"/v1/config"},
      produces = {"application/json"})
  public ResponseEntity<IcebergRestGetCatalogConfigResponseBody> getCatalogConfig(
      @RequestParam(name = "warehouse", required = false) String warehouse) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<IcebergRestGetCatalogConfigResponseBody>
        apiResponse = catalogApiHandler.getCatalogConfig();

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }

  @Operation(
      summary = "Load a table from the catalog",
      description =
          "Load a table from the catalog.\n"
              + "\n"
              + "The response contains both configuration and table metadata. The configuration, if non-empty is used as additional configuration for the table that overrides catalog configuration. For example, this configuration may change the FileIO implementation to be used for the table.\n"
              + "\n"
              + "The response also contains the table's full metadata, matching the table metadata JSON file.\n"
              + "\n"
              + "The catalog configuration may contain credentials that should be used for subsequent requests for the table. The configuration key \"token\" is used to pass an access token to be used as a bearer token for table requests. Otherwise, a token may be passed using a RFC 8693 token type as a configuration key. For example, \"urn:ietf:params:oauth:token-type:jwt=<JWT-token>\".\n"
              + "\n",
      tags = {"IcebergRest/Tables"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Server specified configuration values."),
        @ApiResponse(
            responseCode = "401",
            description =
                "Unauthorized. Authentication is required and has failed or has not yet been provided."),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. Authenticated user does not have the necessary permissions."),
        @ApiResponse(
            responseCode = "419",
            description =
                "Credentials have timed out. If possible, the client should refresh credentials and retry.")
      })
  @GetMapping(
      value = {"/v1/namespaces/{namespace}/tables/{table}"},
      produces = {"application/json"})
  public ResponseEntity<IcebergRestGetTableResponseBody> getTable(
      @Parameter(description = "Namespace", required = true) @PathVariable String namespace,
      @Parameter(description = "Table", required = true) @PathVariable String table,
      @Parameter(description = "Prefix", required = false) @PathVariable String prefix,
      @RequestParam(name = "snapshots", required = false, defaultValue = "all") String warehouse) {
    com.linkedin.openhouse.common.api.spec.ApiResponse<IcebergRestGetTableResponseBody>
        apiResponse = icebergRestTablesApiHandler.getTable(namespace, table, "openhouse");

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }
}
