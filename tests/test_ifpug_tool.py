from pathlib import Path
import sys
import zipfile

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from ifpug_calculator.document_loader import Document
from ifpug_calculator.ifpug import size_items
from ifpug_calculator.parser import parse_documents
from ifpug_calculator.report import build_report


def test_parse_and_size_basic():
    doc = Document(
        Path("req.txt"),
        [
            "ILF Customer",
            "DET: Customer Name, Customer Email",
            "RET: Customer Master",
            "EIF Product Catalog",
            "DET: Product Name, Product Category",
            "RET: Product Group",
            "EI Register Customer",
            "DET: Customer Name, Customer Email",
            "FTR: Customer, Sales",
            "EO Send Welcome Email",
            "DET: Email Content, Customer Name",
            "FTR: Customer",
        ],
    )
    parse_result = parse_documents([doc])
    sizing = size_items(parse_result.logical_files, parse_result.transactions)

    assert len(sizing.logical_files) == 2
    assert sizing.logical_files[0].function_points == 7
    assert sizing.logical_files[1].function_points == 5

    assert len(sizing.transactions) == 2
    fp_values = {item.item.type: item.function_points for item in sizing.transactions}
    assert fp_values["EI"] == 3
    assert fp_values["EO"] == 4

    assert sizing.total_function_points == sum(entry.function_points for entry in sizing.logical_files + sizing.transactions)


def test_report_generation(tmp_path):
    doc = Document(
        Path("req.txt"),
        [
            "ILF Customer",
            "DET: Name, Email",
            "RET: Master",
            "EI Register",
            "DET: Name",
            "FTR: Customer",
        ],
    )
    parse_result = parse_documents([doc])
    sizing = size_items(parse_result.logical_files, parse_result.transactions)
    output = tmp_path / "report.xlsx"
    build_report(sizing, output)
    assert output.exists()
    with zipfile.ZipFile(output) as archive:
        workbook_xml = archive.read("xl/workbook.xml").decode("utf-8")
    sheets = _extract_sheet_names(workbook_xml)
    assert {"Summary", "ILF&EIF", "EI"}.issubset(sheets)


def _extract_sheet_names(workbook_xml: str) -> set[str]:
    import xml.etree.ElementTree as ET

    root = ET.fromstring(workbook_xml)
    ns = {"ns": "http://schemas.openxmlformats.org/spreadsheetml/2006/main"}
    sheets_node = root.find("ns:sheets", ns)
    if sheets_node is None:
        return set()
    return {sheet.get("name") for sheet in sheets_node if sheet.get("name")}
