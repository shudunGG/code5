"""Document loaders for extracting semi-structured requirement text.

The tool only depends on the Python standard library.  For office
formats we implement compact readers that surface textual content that
will later be parsed for IFPUG structures.
"""

from __future__ import annotations

import csv
import re
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterator, List
import xml.etree.ElementTree as ET


@dataclass
class Document:
    """Representation of a requirements document.

    Attributes
    ----------
    source: Path
        Original file path of the document.
    text_blocks: List[str]
        Human readable text fragments extracted from the file.
        Blocks preserve coarse grouping such as paragraphs or rows
        allowing downstream heuristics to make contextual decisions.
    """

    source: Path
    text_blocks: List[str]

    def iter_lines(self) -> Iterator[str]:
        for block in self.text_blocks:
            for line in block.splitlines():
                cleaned = line.strip()
                if cleaned:
                    yield cleaned


SUPPORTED_SUFFIXES: Dict[str, str] = {
    ".txt": "text",
    ".md": "text",
    ".csv": "csv",
    ".tsv": "csv",
    ".docx": "docx",
    ".xlsx": "xlsx",
    ".xlsm": "xlsx",
    ".pdf": "pdf",
}


def load_documents(path: Path) -> List[Document]:
    """Load all supported documents from the provided path."""

    path = path.expanduser().resolve()
    if path.is_dir():
        documents: List[Document] = []
        for entry in sorted(path.iterdir()):
            if entry.is_file():
                doc = load_document(entry)
                if doc:
                    documents.append(doc)
        return documents
    if path.is_file():
        doc = load_document(path)
        return [doc] if doc else []
    raise FileNotFoundError(path)


def load_document(path: Path) -> Document | None:
    loader_key = SUPPORTED_SUFFIXES.get(path.suffix.lower())
    if not loader_key:
        return None
    if loader_key == "text":
        text = path.read_text(encoding="utf-8", errors="ignore")
        return Document(path, [text])
    if loader_key == "csv":
        blocks = list(_extract_csv(path))
        return Document(path, blocks)
    if loader_key == "docx":
        blocks = list(_extract_docx(path))
        return Document(path, blocks)
    if loader_key == "xlsx":
        blocks = list(_extract_xlsx(path))
        return Document(path, blocks)
    if loader_key == "pdf":
        blocks = list(_extract_pdf(path))
        return Document(path, blocks)
    raise RuntimeError(f"Unhandled loader {loader_key}")


def _extract_csv(path: Path) -> Iterator[str]:
    with path.open("r", encoding="utf-8", errors="ignore", newline="") as handle:
        sample = handle.read(4096)
        handle.seek(0)
        dialect = csv.Sniffer().sniff(sample) if sample else csv.excel
        reader = csv.reader(handle, dialect)
        for row in reader:
            if any(cell.strip() for cell in row):
                yield ", ".join(cell.strip() for cell in row if cell.strip())


def _extract_docx(path: Path) -> Iterator[str]:
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if not name.startswith("word/") or not name.endswith(".xml"):
                continue
            if not any(part in name for part in ("document", "header", "footer")):
                continue
            data = archive.read(name)
            try:
                root = ET.fromstring(data)
            except ET.ParseError:
                continue
            namespace_map = _build_namespace_map(root)
            text_nodes = root.findall('.//w:t', namespace_map)
            if not text_nodes:
                continue
            lines: List[str] = []
            for node in text_nodes:
                lines.append(node.text or "")
            combined = _normalize_whitespace(" ".join(lines))
            if combined:
                yield combined


def _extract_xlsx(path: Path) -> Iterator[str]:
    with zipfile.ZipFile(path) as archive:
        shared_strings: List[str] = []
        if "xl/sharedStrings.xml" in archive.namelist():
            shared_strings = _parse_shared_strings(archive.read("xl/sharedStrings.xml"))
        sheet_names = [name for name in archive.namelist() if name.startswith("xl/worksheets/sheet") and name.endswith(".xml")]
        for sheet_name in sorted(sheet_names):
            xml_data = archive.read(sheet_name)
            try:
                root = ET.fromstring(xml_data)
            except ET.ParseError:
                continue
            for row in root.findall(".//{http://schemas.openxmlformats.org/spreadsheetml/2006/main}row"):
                cells = []
                for cell in row.findall("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}c"):
                    cell_type = cell.get("t")
                    value_node = cell.find("{http://schemas.openxmlformats.org/spreadsheetml/2006/main}v")
                    if value_node is None:
                        continue
                    text = value_node.text or ""
                    if cell_type == "s":
                        try:
                            idx = int(text)
                        except ValueError:
                            cell_text = text
                        else:
                            cell_text = shared_strings[idx] if 0 <= idx < len(shared_strings) else text
                    else:
                        cell_text = text
                    if cell_text.strip():
                        cells.append(cell_text.strip())
                if cells:
                    yield ", ".join(cells)


def _parse_shared_strings(data: bytes) -> List[str]:
    try:
        root = ET.fromstring(data)
    except ET.ParseError:
        return []
    ns = {"s": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
    result: List[str] = []
    for si in root.findall("s:si", ns):
        texts: List[str] = []
        for t in si.findall(".//s:t", ns):
            texts.append(t.text or "")
        result.append(_normalize_whitespace("".join(texts)))
    return result


def _extract_pdf(path: Path) -> Iterator[str]:
    data = path.read_bytes()
    for block in re.finditer(br"BT\s*(.*?)\s*ET", data, re.DOTALL):
        chunk = block.group(1)
        texts = []
        for match in re.finditer(br"\((.*?)\)\s*(Tj|'|TJ)", chunk, re.DOTALL):
            raw = match.group(1)
            decoded = _decode_pdf_string(raw)
            if decoded:
                texts.append(decoded)
        if texts:
            yield " ".join(texts)


def _decode_pdf_string(raw: bytes) -> str:
    result = []
    i = 0
    while i < len(raw):
        ch = raw[i]
        if ch == 0x5C:  # backslash escape
            i += 1
            if i >= len(raw):
                break
            esc = raw[i]
            if esc in b"nrtbf()\\":
                mapping = {
                    ord("n"): "\n",
                    ord("r"): "\r",
                    ord("t"): "\t",
                    ord("b"): "\b",
                    ord("f"): "\f",
                    ord("("): "(",
                    ord(")"): ")",
                    ord("\\"): "\\",
                }
                result.append(mapping.get(esc, chr(esc)))
                i += 1
            elif 48 <= esc <= 55:  # octal sequence
                oct_digits = bytes([esc])
                i += 1
                for _ in range(2):
                    if i < len(raw) and 48 <= raw[i] <= 55:
                        oct_digits += bytes([raw[i]])
                        i += 1
                    else:
                        break
                result.append(chr(int(oct_digits, 8)))
            else:
                result.append(chr(esc))
                i += 1
        else:
            result.append(chr(ch))
            i += 1
    return _normalize_whitespace("".join(result))


def _normalize_whitespace(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def _build_namespace_map(root: ET.Element) -> Dict[str, str]:
    ns_map: Dict[str, str] = {}
    for key, value in root.attrib.items():
        if key.startswith("{http://www.w3.org/2000/xmlns/}"):
            prefix = key.split("}", 1)[1]
            ns_map[prefix] = value
    if "w" not in ns_map:
        ns_map["w"] = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    return ns_map
