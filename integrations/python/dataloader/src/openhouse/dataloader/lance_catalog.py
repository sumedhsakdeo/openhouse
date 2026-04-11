import logging
from typing import Any, Self

import lancedb
import requests

logger = logging.getLogger(__name__)

_TABLE_LOCATION = "tableLocation"
_TABLE_FORMAT = "tableFormat"
_LANCE_FORMAT = "LANCE"


class OpenHouseLanceCatalogError(Exception):
    """Error raised when the OpenHouse Lance catalog fails to load a table."""


class OpenHouseLanceCatalog:
    """Client-side catalog implementation for Lance tables in OpenHouse.

    Leverages the OpenHouse Tables Service REST API to discover Lance table
    metadata, then returns a LanceDB table handle backed by the table's storage
    location.

    Args:
        name: Catalog name
        uri: OpenHouse Tables Service base URL
        auth_token: JWT Bearer token for authentication
        ssl_ca_cert: Path to CA cert bundle for SSL verification
        timeout_seconds: HTTP request timeout in seconds
    """

    def __init__(
        self,
        name: str,
        uri: str,
        auth_token: str | None = None,
        ssl_ca_cert: str | None = None,
        timeout_seconds: float = 30,
    ):
        self._name = name
        self._uri = uri.rstrip("/")
        self._timeout = timeout_seconds
        logger.info("Initializing OpenHouseLanceCatalog for service at %s", self._uri)
        self._session = requests.Session()
        self._session.headers["Content-Type"] = "application/json"

        if auth_token is not None:
            self._session.headers["Authorization"] = f"Bearer {auth_token}"

        if ssl_ca_cert is not None:
            self._session.verify = ssl_ca_cert

    def __enter__(self) -> Self:
        return self

    def __exit__(self, *_: Any) -> None:
        self.close()

    def close(self) -> None:
        self._session.close()

    def load_table(self, database: str, table: str) -> lancedb.table.LanceTable:
        """Load a Lance table from OpenHouse by database and table name.

        Calls the OpenHouse REST API to retrieve table metadata, verifies the
        table format is LANCE, then opens and returns a LanceDB table handle
        pointing at the table's storage location.

        Args:
            database: Database name
            table: Table name

        Returns:
            A LanceDB table object for the requested table.

        Raises:
            OpenHouseLanceCatalogError: If the table is not found, is not in
                Lance format, or the response is missing required fields.
        """
        url = f"{self._uri}/v1/databases/{database}/tables/{table}"

        response = self._session.get(url, timeout=self._timeout)
        if not response.ok:
            if response.status_code == 404:
                raise OpenHouseLanceCatalogError(f"Table {database}.{table} does not exist")
            raise OpenHouseLanceCatalogError(
                f"Failed to load table {database}.{table}: HTTP {response.status_code}. Response: {response.text}"
            )

        table_response = response.json()

        table_format = table_response.get(_TABLE_FORMAT)
        if table_format != _LANCE_FORMAT:
            raise OpenHouseLanceCatalogError(
                f"Table {database}.{table} has format '{table_format}', expected '{_LANCE_FORMAT}'"
            )

        table_location = table_response.get(_TABLE_LOCATION)
        if not table_location:
            raise OpenHouseLanceCatalogError(
                f"Response for table {database}.{table} is missing '{_TABLE_LOCATION}'. Response: {table_response}"
            )

        logger.info("Opening Lance table %s.%s at %s", database, table, table_location)
        db = lancedb.connect(table_location)
        return db.open_table(table)

    def list_tables(self, database: str) -> list[str]:
        """List all Lance table names in a database.

        Calls the OpenHouse REST API to list tables in the given database and
        filters to only those with Lance format.

        Args:
            database: Database name

        Returns:
            A list of table names that use the Lance format.

        Raises:
            OpenHouseLanceCatalogError: If the API request fails.
        """
        url = f"{self._uri}/v1/databases/{database}/tables"

        response = self._session.get(url, timeout=self._timeout)
        if not response.ok:
            raise OpenHouseLanceCatalogError(
                f"Failed to list tables in database {database}: HTTP {response.status_code}. Response: {response.text}"
            )

        tables_response = response.json()
        results = tables_response.get("results", [])
        return [
            t["tableId"]
            for t in results
            if t.get(_TABLE_FORMAT) == _LANCE_FORMAT
        ]
