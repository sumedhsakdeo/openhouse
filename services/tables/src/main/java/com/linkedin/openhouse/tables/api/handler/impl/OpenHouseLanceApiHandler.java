package com.linkedin.openhouse.tables.api.handler.impl;

import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.common.exception.EntityNotFoundException;
import com.linkedin.openhouse.tables.api.handler.LanceApiHandler;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceCreateIndexRequest;
import com.linkedin.openhouse.tables.api.spec.v0.request.lance.LanceSearchRequest;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceIndexesResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceSearchResponse;
import com.linkedin.openhouse.tables.api.spec.v0.response.lance.LanceVersionsResponse;
import com.linkedin.openhouse.tables.common.TableFormat;
import com.linkedin.openhouse.tables.model.TableDto;
import com.linkedin.openhouse.tables.model.TableDtoPrimaryKey;
import com.linkedin.openhouse.tables.repository.OpenHouseInternalRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of {@link LanceApiHandler} that validates table format is LANCE and delegates
 * Lance-specific operations to the Lance sidecar service via HTTP.
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "cluster.lance.enabled", havingValue = "true")
public class OpenHouseLanceApiHandler implements LanceApiHandler {

  @Autowired private OpenHouseInternalRepository openHouseInternalRepository;

  @Value("${cluster.lance.sidecar.url:http://lance-sidecar:8090}")
  private String sidecarUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public ApiResponse<LanceVersionsResponse> listVersions(
      String databaseId, String tableId, String actingPrincipal) {
    TableDto tableDto = loadAndValidateLanceTable(databaseId, tableId);
    String tableLocation = tableDto.getTableLocation();

    String url = sidecarUrl + "/v1/tables/" + encodeTableId(tableLocation, tableId) + "/versions";
    LanceVersionsResponse response = restTemplate.getForObject(url, LanceVersionsResponse.class);

    return ApiResponse.<LanceVersionsResponse>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(response)
        .build();
  }

  @Override
  public ApiResponse<Void> createIndex(
      String databaseId,
      String tableId,
      LanceCreateIndexRequest request,
      String actingPrincipal) {
    TableDto tableDto = loadAndValidateLanceTable(databaseId, tableId);
    String tableLocation = tableDto.getTableLocation();

    String url = sidecarUrl + "/v1/tables/" + encodeTableId(tableLocation, tableId) + "/indexes";
    restTemplate.postForObject(url, request, String.class);

    return ApiResponse.<Void>builder().httpStatus(HttpStatus.CREATED).build();
  }

  @Override
  public ApiResponse<LanceIndexesResponse> listIndexes(
      String databaseId, String tableId, String actingPrincipal) {
    TableDto tableDto = loadAndValidateLanceTable(databaseId, tableId);
    String tableLocation = tableDto.getTableLocation();

    String url = sidecarUrl + "/v1/tables/" + encodeTableId(tableLocation, tableId) + "/indexes";
    LanceIndexesResponse response = restTemplate.getForObject(url, LanceIndexesResponse.class);

    return ApiResponse.<LanceIndexesResponse>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(response)
        .build();
  }

  @Override
  public ApiResponse<LanceSearchResponse> search(
      String databaseId,
      String tableId,
      LanceSearchRequest request,
      String actingPrincipal) {
    TableDto tableDto = loadAndValidateLanceTable(databaseId, tableId);
    String tableLocation = tableDto.getTableLocation();

    String url = sidecarUrl + "/v1/tables/" + encodeTableId(tableLocation, tableId) + "/search";
    LanceSearchResponse response =
        restTemplate.postForObject(url, request, LanceSearchResponse.class);

    return ApiResponse.<LanceSearchResponse>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(response)
        .build();
  }

  /**
   * Load a table and validate it is in Lance format.
   *
   * @throws EntityNotFoundException if the table does not exist
   * @throws IllegalArgumentException if the table is not in Lance format
   */
  private TableDto loadAndValidateLanceTable(String databaseId, String tableId) {
    Optional<TableDto> optionalTableDto =
        openHouseInternalRepository.findById(
            TableDtoPrimaryKey.builder().databaseId(databaseId).tableId(tableId).build());
    if (optionalTableDto.isEmpty()) {
      throw new EntityNotFoundException(
          String.format("Table %s.%s not found", databaseId, tableId), null);
    }
    TableDto tableDto = optionalTableDto.get();
    if (tableDto.getTableFormat() != TableFormat.LANCE) {
      throw new IllegalArgumentException(
          String.format(
              "Table %s.%s has format %s, Lance endpoints require LANCE format",
              databaseId, tableId, tableDto.getTableFormat()));
    }
    return tableDto;
  }

  /** Encode a table location and name into a sidecar table_id path parameter. */
  private static String encodeTableId(String location, String tableName) {
    try {
      return java.net.URLEncoder.encode(location + ":" + tableName, "UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
