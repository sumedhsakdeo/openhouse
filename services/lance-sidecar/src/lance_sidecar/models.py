"""Pydantic request/response models for the Lance sidecar API."""

from pydantic import BaseModel, Field


class CreateTableRequest(BaseModel):
    """Request to create a new Lance table."""

    location: str = Field(description="Storage location for the table (e.g. s3://bucket/path)")
    table_name: str = Field(description="Name of the table within the database")
    schema_json: str = Field(description="Arrow schema as JSON string")
    properties: dict[str, str] = Field(default_factory=dict, description="Table properties")


class TableMetadataResponse(BaseModel):
    """Response containing Lance table metadata."""

    location: str
    table_name: str
    schema_json: str
    version: int
    num_rows: int
    properties: dict[str, str] = Field(default_factory=dict)


class VersionInfo(BaseModel):
    """Information about a single table version."""

    version: int
    timestamp: str | None = None
    metadata: dict[str, str] = Field(default_factory=dict)


class ListVersionsResponse(BaseModel):
    """Response listing all versions of a table."""

    versions: list[VersionInfo]


class CreateIndexRequest(BaseModel):
    """Request to create a vector index."""

    column: str = Field(description="Name of the vector column to index")
    index_type: str = Field(default="IVF_PQ", description="Index type: IVF_PQ, IVF_HNSW_SQ, etc.")
    metric_type: str = Field(default="L2", description="Distance metric: L2, cosine, dot")
    num_partitions: int | None = Field(default=None, description="Number of IVF partitions")
    num_sub_vectors: int | None = Field(default=None, description="Number of PQ sub-vectors")


class IndexInfo(BaseModel):
    """Information about a vector index."""

    columns: list[str]
    index_type: str
    name: str | None = None


class ListIndexesResponse(BaseModel):
    """Response listing all indexes on a table."""

    indexes: list[IndexInfo]


class SearchRequest(BaseModel):
    """Request to perform vector similarity search."""

    query_vector: list[float] = Field(description="Query vector for similarity search")
    vector_column: str = Field(description="Name of the vector column to search")
    limit: int = Field(default=10, description="Maximum number of results to return")
    filter: str | None = Field(default=None, description="Optional SQL filter expression")
    metric_type: str = Field(default="L2", description="Distance metric: L2, cosine, dot")
    nprobes: int = Field(default=20, description="Number of probes for IVF search")


class SearchResult(BaseModel):
    """A single search result row."""

    data: dict[str, object]
    distance: float


class SearchResponse(BaseModel):
    """Response containing vector search results."""

    results: list[SearchResult]


class CompactRequest(BaseModel):
    """Request to trigger compaction."""

    target_rows_per_fragment: int = Field(default=1048576, description="Target rows per fragment after compaction")
    max_rows_per_group: int = Field(default=1024, description="Max rows per group within a fragment")


class CompactResponse(BaseModel):
    """Response from a compaction operation."""

    fragments_removed: int
    fragments_added: int


class ErrorResponse(BaseModel):
    """Standard error response."""

    error: str
    detail: str | None = None
