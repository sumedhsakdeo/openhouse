"""Delete data older than a retention policy window from a Lance table."""

import logging
from datetime import datetime, timedelta, timezone

import click
import lancedb

logger = logging.getLogger(__name__)


@click.command()
@click.option("--table-location", required=True, help="Storage path to the Lance database")
@click.option("--table-name", required=True, help="Name of the Lance table")
@click.option("--days", required=True, type=int, help="Retention period in days")
@click.option("--timestamp-column", default="timestamp", help="Column name for timestamp filtering")
@click.option("--dry-run", is_flag=True, default=False, help="Print what would be deleted without deleting")
def main(table_location: str, table_name: str, days: int, timestamp_column: str, dry_run: bool) -> None:
    """Delete rows older than the specified retention period."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

    cutoff = datetime.now(timezone.utc) - timedelta(days=days)
    cutoff_str = cutoff.isoformat()
    filter_expr = f"{timestamp_column} < timestamp '{cutoff_str}'"

    logger.info("Connecting to Lance database at %s", table_location)
    db = lancedb.connect(table_location)
    table = db.open_table(table_name)

    count_before = table.count_rows()
    rows_to_delete = table.count_rows(filter=filter_expr)

    if dry_run:
        logger.info("[DRY RUN] Would delete %d of %d rows (cutoff: %s)", rows_to_delete, count_before, cutoff_str)
        return

    if rows_to_delete == 0:
        logger.info("No rows older than %s found. Nothing to delete.", cutoff_str)
        return

    logger.info("Deleting %d rows older than %s from table '%s'", rows_to_delete, cutoff_str, table_name)
    table.delete(filter_expr)
    count_after = table.count_rows()
    logger.info("Retention complete: %d -> %d rows (deleted %d)", count_before, count_after, count_before - count_after)


if __name__ == "__main__":
    main()
