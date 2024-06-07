package com.linkedin.openhouse.tables.api.handler.impl;

import com.linkedin.openhouse.cluster.configs.ClusterProperties;
import com.linkedin.openhouse.common.api.spec.ApiResponse;
import com.linkedin.openhouse.tables.api.handler.CatalogApiHandler;
import com.linkedin.openhouse.tables.api.spec.v1.response.GetCatalogConfigResponseBody;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.CatalogDefaults;
import com.linkedin.openhouse.tables.api.spec.v1.response.components.CatalogOverrides;
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
  public ApiResponse<GetCatalogConfigResponseBody> getCatalogConfig() {
    return ApiResponse.<GetCatalogConfigResponseBody>builder()
        .httpStatus(HttpStatus.OK)
        .responseBody(
            GetCatalogConfigResponseBody.builder()
                .overrides(
                    CatalogOverrides.builder()
                        .warehouse(
                            clusterProperties.getClusterStorageURI()
                                + clusterProperties.getClusterStorageRootPath())
                        .build())
                .defaults(
                    CatalogDefaults.builder()
                        .clients(clusterProperties.getClusterClientPoolSize())
                        .build())
                .build())
        .build();
  }
}
