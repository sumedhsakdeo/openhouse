"""Delete orphaned .lance files not referenced by any active version."""

import logging

import click
import lancedb

logger = logging.getLogger(__name__)


@click.command()
@click.option("--table-location", required=True, help="Storage path to the Lance database")
@click.option("--table-name", required=True, help="Name of the Lance table")
@click.option("--dry-run", is_flag=True, default=False, help="Print orphaned files without deleting")
def main(table_location: str, table_name: str, dry_run: bool) -> None:
    """Identify and delete orphaned data files not referenced by any version manifest."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

    logger.info("Connecting to Lance database at %s", table_location)
    db = lancedb.connect(table_location)
    table = db.open_table(table_name)

    if dry_run:
        logger.info("[DRY RUN] Would clean up old versions and unreferenced files for table '%s'", table_name)
        versions = table.list_versions()
        logger.info("Table currently has %d versions", len(versions))
        return

    logger.info("Running cleanup_old_versions to remove unreferenced files for table '%s'", table_name)
    table.cleanup_old_versions(older_than=None, delete_unverified=True)
    logger.info("Orphan cleanup complete for table '%s'", table_name)


if __name__ == "__main__":
    main()
