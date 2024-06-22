package com.linkedin.openhouse.tables.api.handler.impl;

import com.linkedin.openhouse.cluster.configs.ClusterProperties;
import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.handler.CatalogApiHandler;
import com.linkedin.openhouse.tables.api.spec.v1.response.IcebergRestGetCatalogConfigResponseBody;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.IcebergRestCatalogDefaults;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.IcebergRestCatalogOverrides;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 * Default OpenHouse Tables API Handler Implementation that is the layer between REST and Backend.
 */
public class OpenHouseCatalogApiHandler implements CatalogApiHandler {

  @Autowired private ClusterProperties clusterProperties;

  /**
   * Function to Get Catalog configuration.
   *
   * @return the catalog response body that would be returned to the client.
   */
  @Override
  public ApiResponse<IcebergRestGetCatalogConfigResponseBody> getCatalogConfig() {
    return ApiResponse.<IcebergRestGetCatalogConfigResponseBody>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(
            IcebergRestGetCatalogConfigResponseBody.builder()
                .overrides(
                    IcebergRestCatalogOverrides.builder()
                        .warehouse(
                            clusterProperties.getClusterStorageURI()
                                + clusterProperties.getClusterStorageRootPath())
                        .build())
                .defaults(
                    IcebergRestCatalogDefaults.builder()
                        .clients(clusterProperties.getClusterClientPoolSize())
                        .build())
                .build())
        .build();
  }
}
