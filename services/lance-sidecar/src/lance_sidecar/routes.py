"""API route handlers for the Lance sidecar service."""

import logging
from urllib.parse import unquote

from fastapi import APIRouter, HTTPException

from lance_sidecar import lance_ops, models

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/v1")


def _decode_table_id(table_id: str) -> tuple[str, str]:
    """Decode a table_id path parameter into (location, table_name).

    The table_id is expected to be URL-encoded as: {location}:{table_name}
    """
    decoded = unquote(table_id)
    if ":" not in decoded:
        raise HTTPException(status_code=400, detail=f"Invalid table_id format: '{decoded}'. Expected 'location:table_name'")
    # Split on last colon to handle URIs like s3://bucket/path:table_name
    parts = decoded.rsplit(":", 1)
    return parts[0], parts[1]


@router.post("/tables", response_model=models.TableMetadataResponse, status_code=201)
def create_table(request: models.CreateTableRequest):
    """Create a new Lance table at the specified storage location."""
    try:
        result = lance_ops.create_table(
            location=request.location,
            table_name=request.table_name,
            schema_json=request.schema_json,
            properties=request.properties,
        )
        return models.TableMetadataResponse(
            location=request.location,
            table_name=request.table_name,
            schema_json=result["schema_json"],
            version=result["version"],
            num_rows=result["num_rows"],
            properties=request.properties,
        )
    except Exception as e:
        logger.exception("Failed to create table at %s", request.location)
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/tables/{table_id}/metadata", response_model=models.TableMetadataResponse)
def get_metadata(table_id: str):
    """Get metadata for an existing Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        result = lance_ops.get_metadata(location, table_name)
        return models.TableMetadataResponse(**result)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Table not found at {location}/{table_name}")
    except Exception as e:
        logger.exception("Failed to get metadata for %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/tables/{table_id}", status_code=204)
def delete_table(table_id: str):
    """Delete a Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        lance_ops.delete_table(location, table_name)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Table not found at {location}/{table_name}")
    except Exception as e:
        logger.exception("Failed to delete table %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/tables/{table_id}/versions", response_model=models.ListVersionsResponse)
def list_versions(table_id: str):
    """List all versions of a Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        versions = lance_ops.list_versions(location, table_name)
        return models.ListVersionsResponse(versions=[models.VersionInfo(**v) for v in versions])
    except Exception as e:
        logger.exception("Failed to list versions for %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/tables/{table_id}/indexes", status_code=201)
def create_index(table_id: str, request: models.CreateIndexRequest):
    """Create a vector index on a Lance table column."""
    location, table_name = _decode_table_id(table_id)
    try:
        lance_ops.create_index(
            location=location,
            table_name=table_name,
            column=request.column,
            index_type=request.index_type,
            metric_type=request.metric_type,
            num_partitions=request.num_partitions,
            num_sub_vectors=request.num_sub_vectors,
        )
        return {"status": "created"}
    except Exception as e:
        logger.exception("Failed to create index on %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/tables/{table_id}/indexes", response_model=models.ListIndexesResponse)
def list_indexes(table_id: str):
    """List all indexes on a Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        indexes = lance_ops.list_indexes(location, table_name)
        return models.ListIndexesResponse(indexes=[models.IndexInfo(**idx) for idx in indexes])
    except Exception as e:
        logger.exception("Failed to list indexes for %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/tables/{table_id}/search", response_model=models.SearchResponse)
def vector_search(table_id: str, request: models.SearchRequest):
    """Perform vector similarity search on a Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        results = lance_ops.vector_search(
            location=location,
            table_name=table_name,
            query_vector=request.query_vector,
            vector_column=request.vector_column,
            limit=request.limit,
            filter_expr=request.filter,
            metric_type=request.metric_type,
            nprobes=request.nprobes,
        )
        return models.SearchResponse(results=[models.SearchResult(**r) for r in results])
    except Exception as e:
        logger.exception("Failed to search %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/tables/{table_id}/compact", response_model=models.CompactResponse)
def compact_table(table_id: str, request: models.CompactRequest):
    """Trigger compaction on a Lance table."""
    location, table_name = _decode_table_id(table_id)
    try:
        result = lance_ops.compact_table(
            location=location,
            table_name=table_name,
            target_rows_per_fragment=request.target_rows_per_fragment,
            max_rows_per_group=request.max_rows_per_group,
        )
        return models.CompactResponse(**result)
    except Exception as e:
        logger.exception("Failed to compact %s/%s", location, table_name)
        raise HTTPException(status_code=500, detail=str(e))
