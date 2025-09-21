"""Excel reporting helpers for the IFPUG analysis."""

from __future__ import annotations

import datetime as _dt
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List
from xml.sax.saxutils import escape

from .ifpug import SizingResult


@dataclass
class Sheet:
    name: str
    rows: List[List[str]]


class SimpleWorkbook:
    """A very small XLSX writer using only the standard library."""

    def __init__(self) -> None:
        self.sheets: List[Sheet] = []

    def add_sheet(self, name: str, rows: Iterable[Iterable[object]]) -> None:
        safe_name = _sanitize_sheet_name(name)
        existing = {sheet.name for sheet in self.sheets}
        if safe_name in existing:
            base = safe_name[:28]
            counter = 1
            candidate = f"{base}_{counter}"
            while candidate in existing:
                counter += 1
                candidate = f"{base}_{counter}"
            safe_name = candidate
        normalized_rows: List[List[str]] = []
        for row in rows:
            normalized_rows.append([_format_cell_value(value) for value in row])
        self.sheets.append(Sheet(safe_name, normalized_rows))

    def save(self, path: Path) -> None:
        path = path.expanduser().resolve()
        path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
            archive.writestr("[Content_Types].xml", _content_types_xml(len(self.sheets)))
            archive.writestr("_rels/.rels", _rels_xml())
            archive.writestr("docProps/app.xml", _app_xml(self.sheets))
            archive.writestr("docProps/core.xml", _core_xml())
            archive.writestr("xl/_rels/workbook.xml.rels", _workbook_rels_xml(len(self.sheets)))
            archive.writestr("xl/styles.xml", _styles_xml())
            archive.writestr("xl/workbook.xml", _workbook_xml(self.sheets))
            for index, sheet in enumerate(self.sheets, start=1):
                archive.writestr(f"xl/worksheets/sheet{index}.xml", _sheet_xml(sheet))


def build_report(result: SizingResult, output_path: Path) -> Path:
    workbook = SimpleWorkbook()

    summary_rows = [["类别", "功能数量", "总FP"]]
    logical_total = sum(entry.function_points for entry in result.logical_files)
    summary_rows.append(["ILF/EIF", str(len(result.logical_files)), str(logical_total)])
    for tx_type in ("EI", "EO", "EQ"):
        matching = [entry for entry in result.transactions if entry.item.type.upper() == tx_type]
        summary_rows.append([tx_type, str(len(matching)), str(sum(e.function_points for e in matching))])
    summary_rows.append(["Total", str(len(result.logical_files) + len(result.transactions)), str(result.total_function_points)])
    workbook.add_sheet("Summary", summary_rows)

    workbook.add_sheet("ILF&EIF", _logical_file_rows(result.logical_files))
    for tx_type in ("EI", "EO", "EQ"):
        workbook.add_sheet(tx_type, _transaction_rows(result.transactions, tx_type))

    workbook.save(output_path)
    return output_path


def _logical_file_rows(entries) -> List[List[str]]:
    rows = [[
        "ILF的名称",
        "DET名称",
        "RET名称",
        "DET的数量",
        "RET的数量",
        "ILF或EIF及DET和RET的数量的统计复杂度",
        "FP的数量",
    ]]
    for entry in entries:
        rows.append([
            f"{entry.item.type.upper()} - {entry.item.name}",
            "\n".join(entry.item.dets) if entry.item.dets else "",
            "\n".join(entry.item.rets) if entry.item.rets else "",
            str(entry.det_count),
            str(entry.ret_count),
            entry.complexity,
            str(entry.function_points),
        ])
    if len(rows) == 1:
        rows.append(["", "", "", "0", "0", "", "0"])
    return rows


def _transaction_rows(entries, tx_type: str) -> List[List[str]]:
    header = [
        f"{tx_type}的名称",
        "DET名称",
        "FTR名称",
        "DET的数量",
        "FTR的数量",
        "复杂度",
        "FP的数量",
    ]
    rows = [header]
    for entry in entries:
        if entry.item.type.upper() != tx_type:
            continue
        rows.append([
            entry.item.name,
            "\n".join(entry.item.dets) if entry.item.dets else "",
            "\n".join(entry.item.ftrs) if entry.item.ftrs else "",
            str(entry.det_count),
            str(entry.ftr_count),
            entry.complexity,
            str(entry.function_points),
        ])
    if len(rows) == 1:
        rows.append(["", "", "", "0", "0", "", "0"])
    return rows


def _format_cell_value(value: object) -> str:
    if value is None:
        return ""
    if isinstance(value, (int, float)):
        return str(value)
    return str(value)


def _content_types_xml(sheet_count: int) -> str:
    override_template = (
        '<Override PartName="/xl/worksheets/sheet{index}.xml" '
        'ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
    )
    overrides = "".join(override_template.format(index=i) for i in range(1, sheet_count + 1))
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
        "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
        "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
        "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
        "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
        "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
        "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
        f"{overrides}"
        "</Types>"
    )


def _rels_xml() -> str:
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
        "</Relationships>"
    )


def _workbook_rels_xml(sheet_count: int) -> str:
    rel_template = (
        '<Relationship Id="rId{index}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" '
        'Target="worksheets/sheet{index}.xml"/>'
    )
    relationships = "".join(rel_template.format(index=i) for i in range(1, sheet_count + 1))
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        f"{relationships}"
        "</Relationships>"
    )


def _styles_xml() -> str:
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
        "<fonts count=\"1\"><font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/><family val=\"2\"/></font></fonts>"
        "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>"
        "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
        "<cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs>"
        "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
        "</styleSheet>"
    )


def _workbook_xml(sheets: List[Sheet]) -> str:
    sheet_template = '<sheet name="{name}" sheetId="{index}" r:id="rId{index}"/>'
    sheets_xml = "".join(sheet_template.format(name=escape(sheet.name), index=i) for i, sheet in enumerate(sheets, start=1))
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
        "<sheets>"
        f"{sheets_xml}"
        "</sheets>"
        "</workbook>"
    )


def _sheet_xml(sheet: Sheet) -> str:
    row_xml_parts = []
    max_col = 0
    for row_index, row in enumerate(sheet.rows, start=1):
        cells_xml = []
        for col_index, value in enumerate(row, start=1):
            if value == "":
                continue
            cell_ref = f"{_column_letter(col_index)}{row_index}"
            cells_xml.append(
                f'<c r="{cell_ref}" t="inlineStr"><is><t>{escape(value)}</t></is></c>'
            )
            max_col = max(max_col, col_index)
        row_body = "".join(cells_xml)
        row_xml_parts.append(f'<row r="{row_index}">{row_body}</row>')
    if max_col == 0:
        dimension = "A1:A1"
    else:
        dimension = f"A1:{_column_letter(max_col)}{len(sheet.rows)}"
    sheet_data = "".join(row_xml_parts)
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
        "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
        f"<dimension ref=\"{dimension}\"/>"
        "<sheetViews><sheetView workbookViewId=\"0\"/></sheetViews>"
        "<sheetFormatPr defaultRowHeight=\"15\"/>"
        f"<sheetData>{sheet_data}</sheetData>"
        "</worksheet>"
    )


def _app_xml(sheets: List[Sheet]) -> str:
    titles = ''.join(f'<vt:lpstr>{escape(sheet.name)}</vt:lpstr>' for sheet in sheets)
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
        "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
        "<Application>Python IFPUG Tool</Application>"
        f"<TitlesOfParts><vt:vector size=\"{len(sheets)}\" baseType=\"lpstr\">{titles}</vt:vector></TitlesOfParts>"
        "</Properties>"
    )


def _core_xml() -> str:
    timestamp = _dt.datetime.now(_dt.timezone.utc).isoformat().replace("+00:00", "Z")
    return (
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
        "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" "
        "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        "<dc:creator>IFPUG Tool</dc:creator>"
        "<cp:lastModifiedBy>IFPUG Tool</cp:lastModifiedBy>"
        f"<dcterms:created xsi:type=\"dcterms:W3CDTF\">{timestamp}</dcterms:created>"
        f"<dcterms:modified xsi:type=\"dcterms:W3CDTF\">{timestamp}</dcterms:modified>"
        "</cp:coreProperties>"
    )


def _column_letter(index: int) -> str:
    result = ""
    while index:
        index, remainder = divmod(index - 1, 26)
        result = chr(65 + remainder) + result
    return result or "A"


def _sanitize_sheet_name(name: str) -> str:
    invalid_chars = {"\\", "/", "*", "?", ":", "[", "]"}
    sanitized = ''.join('_' if ch in invalid_chars else ch for ch in name)
    sanitized = sanitized.strip().strip("'")
    if not sanitized:
        sanitized = "Sheet"
    return sanitized[:31]
