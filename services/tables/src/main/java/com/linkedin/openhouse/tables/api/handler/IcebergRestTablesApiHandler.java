package com.linkedin.openhouse.tables.api.handler;

import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetTableResponseBody;

public interface IcebergRestTablesApiHandler {

  ApiResponse<IcebergRestGetTableResponseBody> getTable(
      String databaseId, String tableId, String actingPrincipal);
}
