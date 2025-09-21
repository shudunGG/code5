"""IFPUG counting tables and sizing utilities."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable, List

from .parser import LogicalFile, Transaction


LOGICAL_FILE_WEIGHTS = {
    "ILF": {"low": 7, "average": 10, "high": 15},
    "EIF": {"low": 5, "average": 7, "high": 10},
}

TRANSACTION_WEIGHTS = {
    "EI": {"low": 3, "average": 4, "high": 6},
    "EO": {"low": 4, "average": 5, "high": 7},
    "EQ": {"low": 3, "average": 4, "high": 6},
}


@dataclass
class SizedLogicalFile:
    item: LogicalFile
    det_count: int
    ret_count: int
    complexity: str
    function_points: int


@dataclass
class SizedTransaction:
    item: Transaction
    det_count: int
    ftr_count: int
    complexity: str
    function_points: int


@dataclass
class SizingResult:
    logical_files: List[SizedLogicalFile]
    transactions: List[SizedTransaction]

    @property
    def total_function_points(self) -> int:
        return sum(entry.function_points for entry in self.logical_files + self.transactions)


def size_items(logical_files: Iterable[LogicalFile], transactions: Iterable[Transaction]) -> SizingResult:
    sized_lf = [size_logical_file(item) for item in logical_files]
    sized_tx = [size_transaction(item) for item in transactions]
    return SizingResult(sized_lf, sized_tx)


def size_logical_file(item: LogicalFile) -> SizedLogicalFile:
    det_count = len({det.strip() for det in item.dets if det and det.strip()})
    ret_count_raw = len({ret.strip() for ret in item.rets if ret and ret.strip()})
    ret_count = ret_count_raw or 1
    complexity = _logical_file_complexity(det_count, ret_count)
    weight = LOGICAL_FILE_WEIGHTS[item.type.upper()][complexity]
    return SizedLogicalFile(item, det_count, ret_count, complexity, weight)


def size_transaction(item: Transaction) -> SizedTransaction:
    det_count = len({det.strip() for det in item.dets if det and det.strip()})
    ftr_count = len({ftr.strip() for ftr in item.ftrs if ftr and ftr.strip()})
    complexity = _transaction_complexity(item.type.upper(), det_count, ftr_count)
    weight = TRANSACTION_WEIGHTS[item.type.upper()][complexity]
    return SizedTransaction(item, det_count, ftr_count, complexity, weight)


def _logical_file_complexity(det_count: int, ret_count: int) -> str:
    if ret_count <= 1:
        if det_count <= 19:
            return "low"
        if det_count <= 50:
            return "low"
        return "average"
    if 2 <= ret_count <= 5:
        if det_count <= 19:
            return "low"
        if det_count <= 50:
            return "average"
        return "high"
    # ret_count >= 6
    if det_count <= 19:
        return "average"
    return "high"


def _transaction_complexity(kind: str, det_count: int, ftr_count: int) -> str:
    kind = kind.upper()
    if kind == "EI":
        if ftr_count <= 1:
            if det_count <= 4:
                return "low"
            if det_count <= 15:
                return "low"
            return "average"
        if ftr_count == 2:
            if det_count <= 4:
                return "low"
            if det_count <= 15:
                return "average"
            return "high"
        # ftr_count >= 3
        if det_count <= 4:
            return "average"
        return "high"
    # EO and EQ share the same matrix
    if ftr_count <= 1:
        if det_count <= 5:
            return "low"
        if det_count <= 19:
            return "low"
        return "average"
    if 2 <= ftr_count <= 3:
        if det_count <= 5:
            return "low"
        if det_count <= 19:
            return "average"
        return "high"
    # ftr_count >= 4
    if det_count <= 5:
        return "average"
    return "high"
