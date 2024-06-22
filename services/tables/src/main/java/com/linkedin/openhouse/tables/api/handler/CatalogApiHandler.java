package com.linkedin.openhouse.tables.api.handler;

import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetCatalogConfigResponseBody;

/**
 * Interface layer between REST and Catalog backend. The implementation is injected into the Service
 * layer.
 */
public interface CatalogApiHandler {

  /**
   * Function to Get Catalog configuration.
   *
   * @return the catalog response body that would be returned to the client.
   */
  ApiResponse<IcebergRestGetCatalogConfigResponseBody> getCatalogConfig();
}
