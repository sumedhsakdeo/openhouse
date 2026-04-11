import logging
from collections.abc import Iterator

import pyarrow as pa

from openhouse.dataloader.lance_catalog import OpenHouseLanceCatalog
from openhouse.dataloader.table_identifier import TableIdentifier

logger = logging.getLogger(__name__)


class OpenHouseLanceDataLoader:
    """An API for loading data from OpenHouse Lance tables.

    Loads Lance table data as PyArrow Tables and RecordBatches, with support
    for vector similarity search during loading.

    Args:
        catalog: An OpenHouseLanceCatalog instance for table discovery
        table_identifier: Identifier for the table to load (database and table name)
    """

    def __init__(
        self,
        catalog: OpenHouseLanceCatalog,
        table_identifier: TableIdentifier,
    ):
        self._catalog = catalog
        self._table_id = table_identifier
        self._lance_table = None

    def _get_lance_table(self):
        """Lazily load and cache the underlying LanceDB table handle."""
        if self._lance_table is None:
            logger.info("Loading Lance table %s", self._table_id)
            self._lance_table = self._catalog.load_table(self._table_id.database, self._table_id.table)
        return self._lance_table

    def to_arrow_table(self) -> pa.Table:
        """Load the entire Lance table as a PyArrow Table.

        Returns:
            A PyArrow Table containing all rows from the Lance table.
        """
        logger.info("Reading Lance table %s to PyArrow Table", self._table_id)
        lance_table = self._get_lance_table()
        return lance_table.to_arrow()

    def to_batches(self, batch_size: int = 1024) -> Iterator[pa.RecordBatch]:
        """Load the Lance table as an iterator of PyArrow RecordBatches.

        Args:
            batch_size: Maximum number of rows per RecordBatch. Smaller values
                reduce peak memory but increase per-batch overhead.

        Yields:
            PyArrow RecordBatch for each chunk of the table.
        """
        logger.info("Reading Lance table %s as batches (batch_size=%d)", self._table_id, batch_size)
        arrow_table = self.to_arrow_table()
        yield from arrow_table.to_batches(max_chunksize=batch_size)

    def search(
        self,
        query_vector: list[float],
        column: str,
        limit: int = 10,
        filter: str | None = None,
    ) -> pa.Table:
        """Perform a vector similarity search on the Lance table.

        Args:
            query_vector: The query vector to search for nearest neighbors.
            column: The name of the vector column to search against.
            limit: Maximum number of results to return.
            filter: Optional SQL filter expression to apply before the search
                (e.g. ``"category = 'news'"``).

        Returns:
            A PyArrow Table containing the closest matching rows, ordered by
            distance from the query vector.
        """
        logger.info(
            "Searching Lance table %s on column '%s' (limit=%d, filter=%s)",
            self._table_id,
            column,
            limit,
            filter,
        )
        lance_table = self._get_lance_table()
        query = lance_table.search(query_vector, vector_column_name=column).limit(limit)
        if filter is not None:
            query = query.where(filter)
        return query.to_arrow()
