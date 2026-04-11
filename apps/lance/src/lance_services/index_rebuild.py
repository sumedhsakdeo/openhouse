"""Periodic vector index optimization/rebuild for Lance tables."""

import logging

import click
import lancedb

logger = logging.getLogger(__name__)


@click.command()
@click.option("--table-location", required=True, help="Storage path to the Lance database")
@click.option("--table-name", required=True, help="Name of the Lance table")
@click.option("--column", required=True, help="Vector column name to index")
@click.option("--index-type", default="IVF_PQ", help="Index type: IVF_PQ, IVF_HNSW_SQ, etc.")
@click.option("--metric-type", default="L2", help="Distance metric: L2, cosine, dot")
@click.option("--num-partitions", default=None, type=int, help="Number of IVF partitions")
@click.option("--num-sub-vectors", default=None, type=int, help="Number of PQ sub-vectors")
@click.option("--replace", is_flag=True, default=True, help="Replace existing index if present")
def main(
    table_location: str,
    table_name: str,
    column: str,
    index_type: str,
    metric_type: str,
    num_partitions: int | None,
    num_sub_vectors: int | None,
    replace: bool,
) -> None:
    """Rebuild a vector index on a Lance table column."""
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

    logger.info("Connecting to Lance database at %s", table_location)
    db = lancedb.connect(table_location)
    table = db.open_table(table_name)

    kwargs: dict = {"metric": metric_type, "replace": replace}
    if num_partitions is not None:
        kwargs["num_partitions"] = num_partitions
    if num_sub_vectors is not None:
        kwargs["num_sub_vectors"] = num_sub_vectors

    logger.info(
        "Rebuilding %s index on column '%s' for table '%s' (metric=%s)",
        index_type,
        column,
        table_name,
        metric_type,
    )
    table.create_index(column, index_type=index_type, **kwargs)
    logger.info("Index rebuild complete for column '%s' on table '%s'", column, table_name)


if __name__ == "__main__":
    main()
