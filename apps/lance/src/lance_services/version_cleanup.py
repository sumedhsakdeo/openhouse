"""Clean old Lance versions beyond a configured retention window."""

import logging

import click
import lancedb

logger = logging.getLogger(__name__)


@click.command()
@click.option("--table-location", required=True, help="Storage path to the Lance database")
@click.option("--table-name", required=True, help="Name of the Lance table")
@click.option("--keep-versions", default=10, type=int, help="Number of recent versions to keep")
@click.option("--dry-run", is_flag=True, default=False, help="Print what would be cleaned without cleaning")
def main(table_location: str, table_name: str, keep_versions: int, dry_run: bool) -> None:
    """Remove old versions of a Lance table, keeping only the most recent N versions."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

    logger.info("Connecting to Lance database at %s", table_location)
    db = lancedb.connect(table_location)
    table = db.open_table(table_name)

    versions = table.list_versions()
    versions_sorted = sorted(versions, key=lambda v: v["version"], reverse=True)

    if len(versions_sorted) <= keep_versions:
        logger.info("Table has %d versions, keep_versions=%d. Nothing to clean.", len(versions_sorted), keep_versions)
        return

    versions_to_remove = versions_sorted[keep_versions:]
    logger.info(
        "Table has %d versions. Removing %d old versions (keeping %d most recent).",
        len(versions_sorted),
        len(versions_to_remove),
        keep_versions,
    )

    if dry_run:
        for v in versions_to_remove:
            logger.info("[DRY RUN] Would remove version %d", v["version"])
        return

    # Lance's cleanup_old_versions removes versions older than a threshold
    table.cleanup_old_versions(older_than=None, delete_unverified=False)
    logger.info("Version cleanup complete.")


if __name__ == "__main__":
    main()
