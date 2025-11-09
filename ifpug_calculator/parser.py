"""Parse textual requirement snippets into IFPUG data structures."""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Optional, Sequence

from .document_loader import Document


@dataclass
class LogicalFile:
    type: str  # ILF or EIF
    name: str
    dets: List[str] = field(default_factory=list)
    rets: List[str] = field(default_factory=list)
    description: Optional[str] = None
    source: Optional[Path] = None


@dataclass
class Transaction:
    type: str  # EI, EO, EQ
    name: str
    dets: List[str] = field(default_factory=list)
    ftrs: List[str] = field(default_factory=list)
    description: Optional[str] = None
    source: Optional[Path] = None


@dataclass
class ParseResult:
    logical_files: List[LogicalFile]
    transactions: List[Transaction]
    ignored_blocks: List[str]


_HEADER_PATTERN = re.compile(r"^(ILF|EIF|EI|EO|EQ)\s*[:：\-]?\s*(.+)?$", re.IGNORECASE)
_DETAIL_PATTERN = re.compile(r"^(DET|RET|FTR|描述|说明)\s*[:：\-]\s*(.+)$", re.IGNORECASE)
def parse_documents(documents: Sequence[Document]) -> ParseResult:
    logical_files: List[LogicalFile] = []
    transactions: List[Transaction] = []
    ignored: List[str] = []

    for document in documents:
        current = None
        for raw_line in document.iter_lines():
            line = _normalize_line(raw_line)
            if not line:
                continue
            header = _HEADER_PATTERN.match(line)
            if header:
                type_name = header.group(1).upper()
                name = (header.group(2) or type_name).strip()
                if type_name in {"ILF", "EIF"}:
                    current = LogicalFile(type_name, name, source=document.source)
                    logical_files.append(current)
                else:
                    current = Transaction(type_name, name, source=document.source)
                    transactions.append(current)
                continue
            if not current:
                if line:
                    ignored.append(line)
                continue
            detail = _DETAIL_PATTERN.match(line)
            if detail:
                kind = detail.group(1).upper()
                values = _split_items(detail.group(2))
                if isinstance(current, LogicalFile):
                    if kind == "DET":
                        current.dets.extend(values)
                    elif kind == "RET":
                        current.rets.extend(values)
                    elif kind in {"描述", "说明"}:
                        current.description = detail.group(2).strip()
                else:
                    if kind == "DET":
                        current.dets.extend(values)
                    elif kind == "FTR":
                        current.ftrs.extend(values)
                    elif kind in {"描述", "说明"}:
                        current.description = detail.group(2).strip()
                continue
            if isinstance(current, LogicalFile):
                if any(token in line.upper() for token in ("DET", "RET")):
                    for piece in re.split(r"\bDET\b|\bRET\b", line, flags=re.IGNORECASE):
                        cleaned = piece.strip(" :：-")
                        if not cleaned:
                            continue
                        prefix_match = re.match(r"^(DET|RET)\s*[:：\-]\s*(.+)$", cleaned, re.IGNORECASE)
                        if prefix_match:
                            kind = prefix_match.group(1).upper()
                            values = _split_items(prefix_match.group(2))
                            if kind == "DET":
                                current.dets.extend(values)
                            else:
                                current.rets.extend(values)
                        else:
                            values = _split_items(cleaned)
                            current.dets.extend(values)
                    continue
                current.dets.extend(_split_items(line))
            else:
                if "DET" in line.upper() or "FTR" in line.upper():
                    for piece in re.split(r"\bDET\b|\bFTR\b", line, flags=re.IGNORECASE):
                        cleaned = piece.strip(" :：-")
                        if not cleaned:
                            continue
                        prefix_match = re.match(r"^(DET|FTR)\s*[:：\-]\s*(.+)$", cleaned, re.IGNORECASE)
                        if prefix_match:
                            kind = prefix_match.group(1).upper()
                            values = _split_items(prefix_match.group(2))
                            if kind == "DET":
                                current.dets.extend(values)
                            else:
                                current.ftrs.extend(values)
                        else:
                            current.dets.extend(_split_items(cleaned))
                    continue
                current.dets.extend(_split_items(line))

    return ParseResult(logical_files, transactions, ignored)


def _normalize_line(line: str) -> str:
    cleaned = line.strip().strip("-•*·●")
    cleaned = cleaned.replace("：", ":").replace("；", ";")
    cleaned = re.sub(r"\s+", " ", cleaned)
    return cleaned


def _split_items(text: str) -> List[str]:
    text = text.replace("、", ",")
    parts = re.split(r"[,;\|/]+", text)
    values = []
    for part in parts:
        cleaned = part.strip()
        if cleaned:
            values.append(cleaned)
    return values
