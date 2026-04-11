"""Compact small Lance fragments into larger ones."""

import logging

import click
import lancedb

logger = logging.getLogger(__name__)


@click.command()
@click.option("--table-location", required=True, help="Storage path to the Lance database")
@click.option("--table-name", required=True, help="Name of the Lance table")
@click.option("--target-rows-per-fragment", default=1048576, type=int, help="Target rows per fragment")
@click.option("--max-rows-per-group", default=1024, type=int, help="Max rows per group within a fragment")
def main(table_location: str, table_name: str, target_rows_per_fragment: int, max_rows_per_group: int) -> None:
    """Compact small data fragments in a Lance table into larger ones."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

    logger.info("Connecting to Lance database at %s", table_location)
    db = lancedb.connect(table_location)
    table = db.open_table(table_name)

    logger.info(
        "Starting compaction for table '%s' (target_rows_per_fragment=%d, max_rows_per_group=%d)",
        table_name,
        target_rows_per_fragment,
        max_rows_per_group,
    )
    stats = table.compact_files(
        target_rows_per_fragment=target_rows_per_fragment,
        max_rows_per_group=max_rows_per_group,
    )
    logger.info(
        "Compaction complete: fragments_removed=%d, fragments_added=%d",
        stats.fragments_removed,
        stats.fragments_added,
    )


if __name__ == "__main__":
    main()
