"""Command line interface for the IFPUG analysis tool."""

from __future__ import annotations

import argparse
from pathlib import Path
from typing import Sequence

from .document_loader import load_documents
from .ifpug import size_items
from .parser import parse_documents
from .report import build_report


def build_argument_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Analyze requirement documents and produce an IFPUG function point report.",
    )
    parser.add_argument(
        "input",
        type=Path,
        help="Path to a requirement document or a directory containing multiple documents.",
    )
    parser.add_argument(
        "output",
        type=Path,
        help="Path where the Excel report (.xlsx) should be written.",
    )
    parser.add_argument(
        "--show-ignored",
        action="store_true",
        help="Print unparsed lines discovered during the analysis.",
    )
    return parser


def run_cli(argv: Sequence[str] | None = None) -> int:
    parser = build_argument_parser()
    args = parser.parse_args(argv)

    documents = load_documents(args.input)
    if not documents:
        parser.error("No supported documents were found at the provided location.")

    parse_result = parse_documents(documents)
    sizing_result = size_items(parse_result.logical_files, parse_result.transactions)

    build_report(sizing_result, args.output)

    print("生成报告: ", args.output)
    print("总功能点: ", sizing_result.total_function_points)
    if args.show_ignored and parse_result.ignored_blocks:
        print("未识别的内容:")
        for line in parse_result.ignored_blocks:
            print("  -", line)

    return 0


if __name__ == "__main__":
    raise SystemExit(run_cli())
