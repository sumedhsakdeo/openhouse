package com.linkedin.openhouse.tables.controller;

import com.linkedin.openhouse.tables.api.handler.CatalogApiHandler;
import com.linkedin.openhouse.tables.api.spec.v1.response.GetCatalogConfigResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
      value = {"/iceberg-rest/v1/config"},
      produces = {"application/json"})
  public ResponseEntity<GetCatalogConfigResponseBody> getCatalogConfig(
      @RequestParam(name = "warehouse", required = false) String warehouse) {

    com.linkedin.openhouse.common.api.spec.ApiResponse<GetCatalogConfigResponseBody> apiResponse =
        catalogApiHandler.getCatalogConfig();

    return new ResponseEntity<>(
        apiResponse.getResponseBody(), apiResponse.getHttpHeaders(), apiResponse.getHttpStatus());
  }
}
