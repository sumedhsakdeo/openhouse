package com.linkedin.openhouse.tables.api.handler.impl;

import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.handler.IcebergRestTablesApiHandler;
import com.linkedin.openhouse.tables.api.spec.v0.response.GetTableResponseBody;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetTableResponseBody;
import com.linkedin.openhouse.tables.api.validator.TablesApiValidator;
import com.linkedin.openhouse.tables.dto.mapper.TablesMapper;
import com.linkedin.openhouse.tables.services.TablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Default OpenHouse Tables API Handler Implementation that is the layer between Iceberg REST and
 * Backend.
 */
public class IcebergRestTablesApiHandlerImpl implements IcebergRestTablesApiHandler {

  @Autowired private TablesApiValidator tablesApiValidator;

  @Autowired private TablesMapper tablesMapper;

  @Autowired private TablesService tableService;

  @Override
  public ApiResponse<IcebergRestGetTableResponseBody> getTable(
      String databaseId, String tableId, String actingPrincipal) {
    tablesApiValidator.validateGetTable(databaseId, tableId);
    GetTableResponseBody getTableResponseBody =
        tablesMapper.toGetTableResponseBody(
            tableService.getTable(databaseId, tableId, "openhouse"));
    return ApiResponse.<IcebergRestGetTableResponseBody>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(
            IcebergRestGetTableResponseBody.builder()
                .metadataLocation(getTableResponseBody.getTableLocation())
                .build())
        .build();
  }
}
