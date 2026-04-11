"""LanceDB operations wrapper. All direct LanceDB SDK calls go through this module."""

import json
import logging

import lancedb
import pyarrow as pa

logger = logging.getLogger(__name__)


def create_table(location: str, table_name: str, schema_json: str, properties: dict[str, str]) -> dict:
    """Create a new Lance table at the given storage location.

    Args:
        location: Storage URI (local path, s3://, gs://, az://).
        table_name: Name for the new table.
        schema_json: Arrow schema serialized as JSON.
        properties: Key-value metadata to store with the table.

    Returns:
        Dict with table metadata (version, num_rows, schema_json).
    """
    schema = pa.ipc.read_schema(pa.BufferReader(schema_json.encode()))
    db = lancedb.connect(location)
    empty_table = pa.table({field.name: pa.array([], type=field.type) for field in schema}, schema=schema)
    table = db.create_table(table_name, data=empty_table)
    logger.info("Created Lance table '%s' at %s", table_name, location)
    return {
        "version": table.version,
        "num_rows": table.count_rows(),
        "schema_json": schema.to_string(),
    }


def get_metadata(location: str, table_name: str) -> dict:
    """Get metadata for an existing Lance table."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    schema = table.schema
    return {
        "location": location,
        "table_name": table_name,
        "schema_json": schema.to_string(),
        "version": table.version,
        "num_rows": table.count_rows(),
    }


def delete_table(location: str, table_name: str) -> None:
    """Delete a Lance table."""
    db = lancedb.connect(location)
    db.drop_table(table_name)
    logger.info("Deleted Lance table '%s' at %s", table_name, location)


def list_versions(location: str, table_name: str) -> list[dict]:
    """List all versions of a Lance table."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    versions = table.list_versions()
    return [
        {
            "version": v["version"],
            "timestamp": str(v.get("timestamp", "")),
            "metadata": v.get("metadata", {}),
        }
        for v in versions
    ]


def create_index(
    location: str,
    table_name: str,
    column: str,
    index_type: str = "IVF_PQ",
    metric_type: str = "L2",
    num_partitions: int | None = None,
    num_sub_vectors: int | None = None,
) -> None:
    """Create a vector index on a column."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    kwargs: dict = {"metric": metric_type}
    if num_partitions is not None:
        kwargs["num_partitions"] = num_partitions
    if num_sub_vectors is not None:
        kwargs["num_sub_vectors"] = num_sub_vectors
    table.create_index(column, index_type=index_type, **kwargs)
    logger.info("Created %s index on column '%s' for table '%s'", index_type, column, table_name)


def list_indexes(location: str, table_name: str) -> list[dict]:
    """List all indexes on a Lance table."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    indexes = table.list_indices()
    return [
        {
            "columns": idx.get("columns", []),
            "index_type": idx.get("index_type", "unknown"),
            "name": idx.get("name"),
        }
        for idx in indexes
    ]


def vector_search(
    location: str,
    table_name: str,
    query_vector: list[float],
    vector_column: str,
    limit: int = 10,
    filter_expr: str | None = None,
    metric_type: str = "L2",
    nprobes: int = 20,
) -> list[dict]:
    """Perform vector similarity search."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    query = table.search(query_vector, vector_column_name=vector_column).metric(metric_type).nprobes(nprobes).limit(limit)
    if filter_expr is not None:
        query = query.where(filter_expr)
    results = query.to_arrow()
    rows = []
    for i in range(results.num_rows):
        row_dict = {col: results.column(col)[i].as_py() for col in results.column_names if col != "_distance"}
        distance = results.column("_distance")[i].as_py() if "_distance" in results.column_names else 0.0
        rows.append({"data": row_dict, "distance": distance})
    return rows


def compact_table(
    location: str,
    table_name: str,
    target_rows_per_fragment: int = 1048576,
    max_rows_per_group: int = 1024,
) -> dict:
    """Trigger compaction on a Lance table."""
    db = lancedb.connect(location)
    table = db.open_table(table_name)
    stats = table.compact_files(
        target_rows_per_fragment=target_rows_per_fragment,
        max_rows_per_group=max_rows_per_group,
    )
    logger.info("Compacted table '%s': removed=%d, added=%d", table_name, stats.fragments_removed, stats.fragments_added)
    return {
        "fragments_removed": stats.fragments_removed,
        "fragments_added": stats.fragments_added,
    }
