"""Comprehensive code base analysis helper.

This script analyses a target directory to provide:

1. Security heuristics across source files.
2. Open-source dependency composition extraction.
3. Basic compliance checks against a built-in license policy.
4. A lightweight IFPUG-like functional size estimation based on code heuristics.

The tool is intentionally dependency-free to keep it portable.  It focuses on
surface-level insights that can be produced without remote services.
"""

from __future__ import annotations

import argparse
import json
import os
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Iterable, Iterator, List, Optional, Sequence, Tuple


# ---------------------------------------------------------------------------
# Generic file helpers


EXCLUDED_DIRS = {
    ".git",
    "node_modules",
    "vendor",
    "__pycache__",
    "build",
    "dist",
    "target",
    ".gradle",
    ".idea",
    ".vscode",
    "out",
}


def iter_source_files(root: Path) -> Iterator[Path]:
    """Yield text files from the directory tree while skipping common caches."""

    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in EXCLUDED_DIRS]
        for filename in filenames:
            path = Path(dirpath, filename)
            if path.suffix.lower() in {
                ".py",
                ".java",
                ".js",
                ".ts",
                ".tsx",
                ".jsx",
                ".go",
                ".rb",
                ".php",
                ".cs",
                ".scala",
                ".kt",
                ".swift",
                ".sql",
                ".html",
                ".xml",
                ".json",
                ".yaml",
                ".yml",
                ".sh",
                ".properties",
                ".conf",
                ".ini",
                ".env",
            }:
                yield path


def read_text(path: Path) -> str:
    """Read text from a file while tolerating encoding issues."""

    with path.open("r", encoding="utf-8", errors="ignore") as f:
        return f.read()


# ---------------------------------------------------------------------------
# Security analysis


@dataclass
class SecurityFinding:
    file: Path
    line: int
    pattern_id: str
    severity: str
    description: str
    recommendation: str


@dataclass
class SecurityPattern:
    pattern_id: str
    regex: re.Pattern[str]
    severity: str
    description: str
    recommendation: str
    skip_strings: bool = True


SECURITY_PATTERNS: Sequence[SecurityPattern] = (
    SecurityPattern(
        pattern_id="insecure-eval",
        regex=re.compile(r"\beval\s*\(", re.IGNORECASE),
        severity="high",
        description="Dynamic evaluation detected.",
        recommendation="Avoid eval/exec; prefer explicit parsing or safe APIs.",
    ),
    SecurityPattern(
        pattern_id="insecure-exec",
        regex=re.compile(r"\bexec\s*\(", re.IGNORECASE),
        severity="high",
        description="Code execution through exec().",
        recommendation="Ensure inputs are trusted or refactor to explicit calls.",
    ),
    SecurityPattern(
        pattern_id="child-process-exec",
        regex=re.compile(r"child_process\.(exec|execSync)\s*\(", re.IGNORECASE),
        severity="high",
        description="Node.js child_process exec detected.",
        recommendation="Validate user inputs or use spawn with argument arrays.",
    ),
    SecurityPattern(
        pattern_id="runtime-exec",
        regex=re.compile(r"Runtime\.getRuntime\(\)\.exec\s*\(", re.IGNORECASE),
        severity="high",
        description="Java Runtime.exec invocation detected.",
        recommendation="Avoid concatenating user input into Runtime.exec commands.",
    ),
    SecurityPattern(
        pattern_id="shell-true",
        regex=re.compile(r"subprocess\.(Popen|run|call)\s*\(.*shell\s*=\s*True", re.IGNORECASE),
        severity="medium",
        description="Shell command with shell=True detected.",
        recommendation="Use list arguments without shell=True and validate inputs.",
    ),
    SecurityPattern(
        pattern_id="os-system",
        regex=re.compile(r"os\.system\s*\(", re.IGNORECASE),
        severity="medium",
        description="os.system call found.",
        recommendation="Prefer subprocess without shell and sanitize inputs.",
    ),
    SecurityPattern(
        pattern_id="processbuilder",
        regex=re.compile(r"new\s+ProcessBuilder\s*\(", re.IGNORECASE),
        severity="medium",
        description="ProcessBuilder usage requires rigorous input validation.",
        recommendation="Provide static command arguments and validate any external input.",
    ),
    SecurityPattern(
        pattern_id="yaml-load",
        regex=re.compile(r"yaml\.load\s*\(", re.IGNORECASE),
        severity="medium",
        description="Unsafe yaml.load usage.",
        recommendation="Use yaml.safe_load for untrusted YAML content.",
    ),
    SecurityPattern(
        pattern_id="requests-verify-false",
        regex=re.compile(
            r"requests\.(get|post|put|patch|delete|head|options)\s*\(.*verify\s*=\s*False",
            re.IGNORECASE | re.DOTALL,
        ),
        severity="high",
        description="TLS certificate verification disabled in requests call.",
        recommendation="Remove verify=False or provide a trusted CA bundle.",
    ),
    SecurityPattern(
        pattern_id="ssl-unverified-context",
        regex=re.compile(r"ssl\._create_unverified_context", re.IGNORECASE),
        severity="high",
        description="Unverified SSL context creation detected.",
        recommendation="Rely on default SSL context or supply a verified CA store.",
    ),
    SecurityPattern(
        pattern_id="pickle-load",
        regex=re.compile(r"pickle\.(load|loads)\s*\(", re.IGNORECASE),
        severity="medium",
        description="Pickle deserialization is unsafe with untrusted data.",
        recommendation="Validate pickle source or migrate to a safer format.",
    ),
    SecurityPattern(
        pattern_id="hashlib-weak",
        regex=re.compile(r"hashlib\.(md5|sha1)\s*\(", re.IGNORECASE),
        severity="medium",
        description="Weak cryptographic hash usage detected.",
        recommendation="Upgrade to SHA-256 or stronger hashing algorithms.",
    ),
    SecurityPattern(
        pattern_id="node-weak-hash",
        regex=re.compile(r"crypto\.createHash\(['\"](md5|sha1)['\"]\)", re.IGNORECASE),
        severity="medium",
        description="Weak cryptographic hash in Node.js crypto module.",
        recommendation="Use modern hashes such as sha256 or sha512.",
    ),
    SecurityPattern(
        pattern_id="weak-random",
        regex=re.compile(r"random\.(random|randrange|randint|choice)\s*\(", re.IGNORECASE),
        severity="low",
        description="Use of non-cryptographic random API.",
        recommendation="Prefer secrets module for security-sensitive randomness.",
    ),
    SecurityPattern(
        pattern_id="hardcoded-password",
        regex=re.compile(r"password\s*=\s*['\"]", re.IGNORECASE),
        severity="high",
        description="Possible hard-coded password.",
        recommendation="Load secrets from environment variables or vault services.",
    ),
    SecurityPattern(
        pattern_id="api-key-literal",
        regex=re.compile(r"api[_-]?key\s*=\s*['\"]", re.IGNORECASE),
        severity="high",
        description="Hard-coded API key or credential detected.",
        recommendation="Inject keys through configuration management, not source.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="secret-key-literal",
        regex=re.compile(r"secret[_-]?key\s*=\s*['\"]", re.IGNORECASE),
        severity="high",
        description="Hard-coded secret key literal.",
        recommendation="Use environment variables or secrets storage services.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="aws-access-key",
        regex=re.compile(r"AKIA[0-9A-Z]{16}"),
        severity="high",
        description="Possible AWS access key ID committed to source.",
        recommendation="Rotate the key and store credentials outside source control.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="private-key-block",
        regex=re.compile(r"-----BEGIN (?:RSA|DSA|EC|OPENSSH) PRIVATE KEY-----"),
        severity="critical",
        description="Private key material present in repository.",
        recommendation="Remove the key, rotate credentials, and store securely.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="tempfile-mktemp",
        regex=re.compile(r"tempfile\.mktemp\s*\(", re.IGNORECASE),
        severity="medium",
        description="Insecure temporary file creation with mktemp().",
        recommendation="Use NamedTemporaryFile or mkstemp for race-free temp files.",
    ),
    SecurityPattern(
        pattern_id="urllib3-disable-warnings",
        regex=re.compile(r"urllib3\.disable_warnings", re.IGNORECASE),
        severity="low",
        description="Disabling TLS warnings may hide certificate issues.",
        recommendation="Address the root TLS problem instead of suppressing warnings.",
    ),
    SecurityPattern(
        pattern_id="jwt-no-verify",
        regex=re.compile(r"jwt\.(?:decode|decode_async)\s*\([^)]*verify\s*=\s*False", re.IGNORECASE),
        severity="high",
        description="JWT signature verification disabled.",
        recommendation="Always validate JWT signatures with strong keys and algorithms.",
    ),
    SecurityPattern(
        pattern_id="jwt-none-alg",
        regex=re.compile(r"['\"]alg['\"]\s*:\s*['\"]none['\"]", re.IGNORECASE),
        severity="critical",
        description="JWT configured to allow 'none' algorithm.",
        recommendation="Disallow the 'none' algorithm and enforce strong JWT signing.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="weak-default-credential",
        regex=re.compile(
            r"DEFAULT_(?:PASSWORD|PASS|USER|USERNAME)\s*=\s*['\"](?:password|admin|123456|12345)['\"]",
            re.IGNORECASE,
        ),
        severity="high",
        description="Default credential appears to remain enabled.",
        recommendation="Disable default accounts and require immediate password rotation.",
    ),
    SecurityPattern(
        pattern_id="plaintext-token",
        regex=re.compile(r"token\s*=\s*['\"][A-Za-z0-9\-_]{16,}['\"]"),
        severity="high",
        description="Long token literal detected in source.",
        recommendation="Store sensitive tokens in a secret manager or environment variables.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="session-cookie-insecure",
        regex=re.compile(
            r"SESSION_COOKIE_(?:SECURE|HTTPONLY)\s*=\s*False|SESSION_EXPIRE_AT_BROWSER_CLOSE\s*=\s*False",
            re.IGNORECASE,
        ),
        severity="medium",
        description="Session cookie protections disabled.",
        recommendation="Enable Secure/HttpOnly flags and bind sessions to authenticated users.",
    ),
    SecurityPattern(
        pattern_id="csrf-disabled",
        regex=re.compile(
            r"csrf\s*\([^)]*\)\.disable\(\)|@csrf_exempt|CSRF_COOKIE_SECURE\s*=\s*False|CSRF_TRUSTED_ORIGINS\s*=\s*\['\*'\]",
            re.IGNORECASE,
        ),
        severity="high",
        description="CSRF defenses disabled or weakened.",
        recommendation="Keep CSRF protection enabled and restrict trusted origins.",
    ),
    SecurityPattern(
        pattern_id="mfa-disabled",
        regex=re.compile(
            r"(MFA|MULTI_FACTOR|TWO_FACTOR)[-_A-Z]*ENABLED\s*=\s*False|requireMfa\s*=\s*false",
            re.IGNORECASE,
        ),
        severity="medium",
        description="Multi-factor authentication appears to be disabled.",
        recommendation="Require MFA for privileged and high-risk user journeys.",
    ),
    SecurityPattern(
        pattern_id="permit-all",
        regex=re.compile(r"\.permitAll\s*\(|@AllowAnonymous|AllowAny", re.IGNORECASE),
        severity="high",
        description="Endpoint marked as permit all / anonymous access.",
        recommendation="Ensure public endpoints exclude administrative or sensitive operations.",
    ),
    SecurityPattern(
        pattern_id="management-security-disabled",
        regex=re.compile(
            r"management\.security\.enabled\s*=\s*false|management\.endpoints\.web\.exposure\.include\s*=\s*\*",
            re.IGNORECASE,
        ),
        severity="high",
        description="Management endpoints exposed without security.",
        recommendation="Protect admin/monitoring endpoints with strong authentication.",
    ),
    SecurityPattern(
        pattern_id="idor-parameter",
        regex=re.compile(r"request\.(?:args|get_json|form|POST|params)\.(?:get|\[)['\"][^'\"]*id['\"]", re.IGNORECASE),
        severity="medium",
        description="Identifier pulled directly from user input; review for IDOR risks.",
        recommendation="Enforce authorization on resource access regardless of supplied IDs.",
    ),
    SecurityPattern(
        pattern_id="frontend-flag-auth",
        regex=re.compile(r"request\.(?:args|form)\.get\(['\"]is_admin['\"]", re.IGNORECASE),
        severity="high",
        description="Backend relying on client-supplied admin flag.",
        recommendation="Derive privileges from server-side session or tokens, not client flags.",
    ),
    SecurityPattern(
        pattern_id="sql-string-concat",
        regex=re.compile(r"execute\s*\([^)]*(?:\+|f\"|\.format\(|%\s*\()", re.IGNORECASE),
        severity="high",
        description="Dynamic SQL construction detected.",
        recommendation="Use parameterized queries or ORM query builders instead of string concat.",
    ),
    SecurityPattern(
        pattern_id="sql-raw",
        regex=re.compile(r"RawSQL\s*\(|createNativeQuery\s*\(", re.IGNORECASE),
        severity="medium",
        description="Raw SQL execution APIs in use.",
        recommendation="Validate inputs rigorously or migrate to parameterized abstractions.",
    ),
    SecurityPattern(
        pattern_id="template-autoescape-disabled",
        regex=re.compile(r"Environment\s*\([^)]*autoescape\s*=\s*False", re.IGNORECASE),
        severity="medium",
        description="Template engine auto-escape disabled.",
        recommendation="Keep autoescape enabled or escape user content manually.",
    ),
    SecurityPattern(
        pattern_id="template-from-string",
        regex=re.compile(r"render_template_string\s*\(|Template\s*\([^)]*request\.", re.IGNORECASE),
        severity="high",
        description="Template rendered from raw user input.",
        recommendation="Avoid rendering templates built from untrusted strings.",
    ),
    SecurityPattern(
        pattern_id="ldap-injection",
        regex=re.compile(r"ldap[^\n]*=\s*f?['\"][^'\"]*{", re.IGNORECASE),
        severity="high",
        description="LDAP query string built dynamically.",
        recommendation="Sanitize LDAP filters and use parameterized APIs.",
    ),
    SecurityPattern(
        pattern_id="xss-innerhtml",
        regex=re.compile(r"innerHTML\s*=\s*(request\.|req\.|location\.)", re.IGNORECASE),
        severity="high",
        description="DOM innerHTML populated with user-controlled data.",
        recommendation="Escape or sanitize data before assigning to innerHTML.",
    ),
    SecurityPattern(
        pattern_id="xss-dangerous-set",
        regex=re.compile(r"dangerouslySetInnerHTML\s*\(", re.IGNORECASE),
        severity="medium",
        description="React dangerouslySetInnerHTML used.",
        recommendation="Ensure content is sanitized before injecting into the DOM.",
    ),
    SecurityPattern(
        pattern_id="open-redirect",
        regex=re.compile(r"redirect\s*\(\s*(request\.|req\.|HttpServletRequest)", re.IGNORECASE),
        severity="medium",
        description="Redirect target derived from user input.",
        recommendation="Whitelist redirect targets or use server-side mapping.",
    ),
    SecurityPattern(
        pattern_id="ssrf-risk",
        regex=re.compile(r"(requests|httpx|urllib|fetch)\.[^(]*\([^)]*(request|req)\.", re.IGNORECASE),
        severity="high",
        description="Server-side request initiated with user-controlled input.",
        recommendation="Validate outbound request destinations to mitigate SSRF.",
    ),
    SecurityPattern(
        pattern_id="file-upload-unvalidated",
        regex=re.compile(r"save\s*\([^)]*request\.files", re.IGNORECASE),
        severity="high",
        description="File upload saved using user supplied filename.",
        recommendation="Validate file type, size, and destination; use secure_filename equivalents.",
    ),
    SecurityPattern(
        pattern_id="path-traversal",
        regex=re.compile(r"os\.path\.join\s*\([^)]*(request\.|req\.)", re.IGNORECASE),
        severity="high",
        description="Filesystem path built from request input.",
        recommendation="Normalize and validate paths to prevent directory traversal.",
    ),
    SecurityPattern(
        pattern_id="dotdot-path",
        regex=re.compile(r"\.\.\/"),
        severity="medium",
        description="Potential directory traversal sequence present.",
        recommendation="Sanitize user input to strip traversal sequences before file access.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="archive-extractall",
        regex=re.compile(r"(zipfile|tarfile)\.[^\n]*extractall", re.IGNORECASE),
        severity="medium",
        description="Archive extraction using extractall can allow path traversal.",
        recommendation="Validate archive members before extraction.",
    ),
    SecurityPattern(
        pattern_id="cors-any",
        regex=re.compile(
            r"Access-Control-Allow-Origin\s*[:=]\s*['\"]\*['\"]|CORS_ORIGIN_ALLOW_ALL\s*=\s*True|app\.use\(cors\(\{[^}]*origin\s*:\s*['\"]\*['\"]",
            re.IGNORECASE,
        ),
        severity="high",
        description="CORS allows any origin.",
        recommendation="Restrict allowed origins to trusted domains.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="cors-allow-credentials",
        regex=re.compile(r"Access-Control-Allow-Credentials\s*[:=]\s*['\"]?true['\"]?", re.IGNORECASE),
        severity="medium",
        description="CORS responses allow credentials.",
        recommendation="Combine credentialed CORS with strict origin allow lists.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="security-headers-disabled",
        regex=re.compile(r"frameOptions\(\)\.disable\(\)|X-Frame-Options\s*[:=]\s*['\"]?ALLOW", re.IGNORECASE),
        severity="medium",
        description="Security headers intentionally disabled.",
        recommendation="Enable frame busting and related security headers.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="hsts-disabled",
        regex=re.compile(r"SECURE_HSTS_SECONDS\s*=\s*0|HSTS\s*=\s*False", re.IGNORECASE),
        severity="medium",
        description="HTTP Strict Transport Security disabled.",
        recommendation="Enable HSTS to enforce HTTPS on supported domains.",
    ),
    SecurityPattern(
        pattern_id="cdn-no-sri",
        regex=re.compile(r"<script[^>]+src=\"https?://[^\"]+\"(?![^>]*integrity=)", re.IGNORECASE),
        severity="medium",
        description="External script loaded without Subresource Integrity.",
        recommendation="Add SRI hashes to externally hosted scripts.",
    ),
    SecurityPattern(
        pattern_id="rate-limit-disabled",
        regex=re.compile(r"@limiter\.exempt|throttle_classes\s*=\s*\[\s*\]|limiter\.enabled\s*=\s*False", re.IGNORECASE),
        severity="medium",
        description="Rate limiting or throttling disabled on endpoint.",
        recommendation="Apply rate limiting or quota controls to critical workflows.",
    ),
    SecurityPattern(
        pattern_id="workflow-step-client",
        regex=re.compile(r"request\.(?:args|get_json|form)\.get\(['\"]step['\"]", re.IGNORECASE),
        severity="medium",
        description="Workflow step taken directly from client input.",
        recommendation="Validate business process state transitions server-side.",
    ),
    SecurityPattern(
        pattern_id="dependency-floating-version",
        regex=re.compile(r"(==\s*(?:latest|master|dev)|version\s*[:=]\s*['\"]\*['\"])", re.IGNORECASE),
        severity="medium",
        description="Dependency pinned to floating/latest version.",
        recommendation="Pin dependencies to immutable versions and monitor CVEs.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="dependency-git-remote",
        regex=re.compile(r"git\+https?://", re.IGNORECASE),
        severity="medium",
        description="Dependency pulled directly from remote VCS.",
        recommendation="Mirror and vet third-party repositories before consumption.",
        skip_strings=False,
    ),
    SecurityPattern(
        pattern_id="http-url",
        regex=re.compile(r"http://", re.IGNORECASE),
        severity="low",
        description="Non-HTTPS URL detected.",
        recommendation="Prefer HTTPS endpoints to protect data in transit.",
    ),
)


class SecurityAnalyzer:
    def analyze(self, root: Path) -> List[SecurityFinding]:
        findings: List[SecurityFinding] = []
        for path in iter_source_files(root):
            try:
                text = read_text(path)
            except OSError:
                continue
            for pattern in SECURITY_PATTERNS:
                for match in pattern.regex.finditer(text):
                    if pattern.skip_strings and self._is_probably_in_string(text, match.start()):
                        continue
                    line_no = text.count("\n", 0, match.start()) + 1
                    findings.append(
                        SecurityFinding(
                            file=path,
                            line=line_no,
                            pattern_id=pattern.pattern_id,
                            severity=pattern.severity,
                            description=pattern.description,
                            recommendation=pattern.recommendation,
                        )
                    )
        return findings

    @staticmethod
    def _is_probably_in_string(text: str, index: int) -> bool:
        """Heuristic check to reduce false positives inside quoted strings."""

        in_single = False
        in_double = False
        escape = False
        for char in text[:index]:
            if escape:
                escape = False
                continue
            if char == "\\":
                escape = True
                continue
            if char == "'" and not in_double:
                in_single = not in_single
            elif char == '"' and not in_single:
                in_double = not in_double
        return in_single or in_double


# ---------------------------------------------------------------------------
# Open-source composition analysis


@dataclass
class Dependency:
    name: str
    version: Optional[str]
    source_file: Path
    ecosystem: str
    license: Optional[str] = None


class OpenSourceAnalyzer:
    def __init__(self, root: Path) -> None:
        self.root = root

    def analyze(self) -> List[Dependency]:
        deps: List[Dependency] = []
        deps.extend(self._scan_requirements())
        deps.extend(self._scan_package_json())
        deps.extend(self._scan_pom())
        deps.extend(self._scan_gradle())
        return deps

    def _scan_requirements(self) -> List[Dependency]:
        deps: List[Dependency] = []
        for path in self.root.rglob("requirements*.txt"):
            try:
                content = read_text(path)
            except OSError:
                continue
            for raw_line in content.splitlines():
                line = raw_line.strip()
                if not line or line.startswith("#"):
                    continue
                name, version = self._split_requirement(line)
                deps.append(
                    Dependency(
                        name=name,
                        version=version,
                        source_file=path,
                        ecosystem="python",
                    )
                )
        return deps

    @staticmethod
    def _split_requirement(line: str) -> Tuple[str, Optional[str]]:
        for sep in ["==", ">=", "<=", "~=", "!=", "=="]:
            if sep in line:
                name, version = line.split(sep, 1)
                return name.strip(), version.strip()
        return line, None

    def _scan_package_json(self) -> List[Dependency]:
        deps: List[Dependency] = []
        for path in self.root.rglob("package.json"):
            try:
                data = json.loads(read_text(path) or "{}")
            except (OSError, json.JSONDecodeError):
                continue
            license_value = data.get("license")
            for section in ("dependencies", "devDependencies", "peerDependencies"):
                section_data = data.get(section) or {}
                if isinstance(section_data, dict):
                    for name, version in section_data.items():
                        deps.append(
                            Dependency(
                                name=name,
                                version=str(version),
                                source_file=path,
                                ecosystem="node",
                                license=license_value if isinstance(license_value, str) else None,
                            )
                        )
        return deps

    def _scan_pom(self) -> List[Dependency]:
        deps: List[Dependency] = []
        for path in self.root.rglob("pom.xml"):
            try:
                text = read_text(path)
            except OSError:
                continue
            pattern = re.compile(
                r"<dependency>\s*"
                r"<groupId>(?P<group>[^<]+)</groupId>\s*"
                r"<artifactId>(?P<artifact>[^<]+)</artifactId>\s*"
                r"(?:<version>(?P<version>[^<]+)</version>)?",
                re.MULTILINE,
            )
            for match in pattern.finditer(text):
                name = f"{match.group('group')}:{match.group('artifact')}"
                deps.append(
                    Dependency(
                        name=name,
                        version=match.group("version"),
                        source_file=path,
                        ecosystem="maven",
                    )
                )
        return deps

    def _scan_gradle(self) -> List[Dependency]:
        deps: List[Dependency] = []
        for path in self.root.rglob("build.gradle"):
            try:
                text = read_text(path)
            except OSError:
                continue
            pattern = re.compile(r"['\"]([\w\-\.]+:[\w\-\.]+:[\w\-\.]+)['\"]")
            for match in pattern.finditer(text):
                name = match.group(1)
                parts = name.split(":")
                version = parts[2] if len(parts) == 3 else None
                deps.append(
                    Dependency(
                        name="{}:{}".format(parts[0], parts[1] if len(parts) > 1 else ""),
                        version=version,
                        source_file=path,
                        ecosystem="gradle",
                    )
                )
        return deps


# ---------------------------------------------------------------------------
# Compliance analysis


RESTRICTED_LICENSE_KEYWORDS = {
    "gpl",
    "agpl",
    "lgpl",
    "mpl",
}

PERMISSIVE_LICENSE_KEYWORDS = {
    "mit",
    "bsd",
    "apache",
    "unlicense",
}


@dataclass
class ComplianceIssue:
    dependency: Optional[str]
    message: str
    severity: str


class ComplianceAnalyzer:
    def __init__(self, root: Path, dependencies: Sequence[Dependency]):
        self.root = root
        self.dependencies = dependencies

    def analyze(self) -> Tuple[List[ComplianceIssue], Optional[str]]:
        issues: List[ComplianceIssue] = []
        project_license = self._detect_project_license()

        if project_license is None:
            issues.append(
                ComplianceIssue(
                    dependency=None,
                    message="Project license not found. Consider adding a LICENSE file.",
                    severity="medium",
                )
            )

        for dep in self.dependencies:
            license_info = (dep.license or "").lower()
            if any(keyword in license_info for keyword in RESTRICTED_LICENSE_KEYWORDS):
                issues.append(
                    ComplianceIssue(
                        dependency=dep.name,
                        message="Dependency appears to use a copyleft license (review obligations).",
                        severity="high",
                    )
                )
            elif license_info and not any(
                keyword in license_info for keyword in PERMISSIVE_LICENSE_KEYWORDS
            ):
                issues.append(
                    ComplianceIssue(
                        dependency=dep.name,
                        message=f"Unrecognized license string '{dep.license}'.",
                        severity="low",
                    )
                )
            elif not license_info:
                issues.append(
                    ComplianceIssue(
                        dependency=dep.name,
                        message="License unknown; verify before distribution.",
                        severity="medium",
                    )
                )

        return issues, project_license

    def _detect_project_license(self) -> Optional[str]:
        for candidate in ["LICENSE", "LICENSE.txt", "COPYING", "COPYRIGHT"]:
            path = self.root / candidate
            if path.exists():
                try:
                    text = read_text(path)
                except OSError:
                    return None
                # Use the first non-empty line as a descriptor.
                for line in text.splitlines():
                    stripped = line.strip()
                    if stripped:
                        return stripped
                return candidate
        return None


# ---------------------------------------------------------------------------
# Metrics & IFPUG estimation


@dataclass
class FileMetrics:
    path: Path
    lines_of_code: int = 0
    function_defs: int = 0
    class_defs: int = 0
    http_endpoints: Dict[str, int] = field(default_factory=lambda: {"get": 0, "post": 0, "put": 0, "delete": 0, "patch": 0})
    db_entities: int = 0
    db_operations: int = 0
    external_calls: int = 0
    query_functions: int = 0
    input_functions: int = 0
    output_functions: int = 0


class MetricsCollector:
    FUNCTION_KEYWORDS = {
        "input": ("create", "add", "insert", "update", "delete", "save", "post"),
        "output": ("get", "list", "export", "download", "report", "read"),
        "query": ("find", "search", "query", "filter", "check"),
    }

    def collect(self, root: Path) -> List[FileMetrics]:
        metrics: List[FileMetrics] = []
        for path in iter_source_files(root):
            try:
                text = read_text(path)
            except OSError:
                continue
            fm = FileMetrics(path=path)
            fm.lines_of_code = self._count_loc(text)
            fm.function_defs = self._count_function_defs(path, text, fm)
            fm.class_defs = len(re.findall(r"^\s*(class|interface|struct)\s+\w+", text, re.MULTILINE))
            self._count_http_endpoints(text, fm)
            fm.db_entities = self._count_db_entities(text)
            fm.db_operations = self._count_db_operations(text)
            fm.external_calls = self._count_external_calls(text)
            metrics.append(fm)
        return metrics

    @staticmethod
    def _count_loc(text: str) -> int:
        return sum(1 for line in text.splitlines() if line.strip())

    def _count_function_defs(self, path: Path, text: str, fm: FileMetrics) -> int:
        extension = path.suffix.lower()
        names: List[str] = []
        if extension == ".py":
            names.extend(re.findall(r"^\s*def\s+(\w+)", text, re.MULTILINE))
        elif extension in {".js", ".ts", ".tsx", ".jsx"}:
            names.extend(re.findall(r"function\s+(\w+)", text))
            names.extend(re.findall(r"const\s+(\w+)\s*=\s*\([^)]*\)\s*=>", text))
        elif extension in {".java", ".kt", ".scala", ".go", ".cs"}:
            names.extend(re.findall(r"\b(?:public|private|protected|static|final|synchronized|async|def|fun|void|int|String|boolean|double|float|List|Map|Set|var|val)+\s+\w+\s+(\w+)\s*\(", text))
        elif extension == ".php":
            names.extend(re.findall(r"function\s+(\w+)\s*\(", text))
        elif extension == ".rb":
            names.extend(re.findall(r"^\s*def\s+(\w+)", text, re.MULTILINE))

        for name in names:
            lowered = name.lower()
            if any(keyword in lowered for keyword in self.FUNCTION_KEYWORDS["input"]):
                fm.input_functions += 1
            elif any(keyword in lowered for keyword in self.FUNCTION_KEYWORDS["output"]):
                fm.output_functions += 1
            elif any(keyword in lowered for keyword in self.FUNCTION_KEYWORDS["query"]):
                fm.query_functions += 1

        return len(names)

    @staticmethod
    def _count_http_endpoints(text: str, fm: FileMetrics) -> None:
        patterns = {
            "get": [r"@GetMapping", r"\.get\(", r"router\.get", r"@app\.get", r"@router\.get"],
            "post": [r"@PostMapping", r"\.post\(", r"router\.post", r"@app\.post", r"@router\.post"],
            "put": [r"@PutMapping", r"\.put\(", r"router\.put", r"@app\.put", r"@router\.put"],
            "delete": [r"@DeleteMapping", r"\.delete\(", r"router\.delete", r"@app\.delete", r"@router\.delete"],
            "patch": [r"@PatchMapping", r"\.patch\(", r"router\.patch", r"@app\.patch", r"@router\.patch"],
        }
        for method, regexes in patterns.items():
            for pattern in regexes:
                count = len(re.findall(pattern, text))
                fm.http_endpoints[method] += count

    @staticmethod
    def _count_db_entities(text: str) -> int:
        patterns = [r"@Entity", r"CREATE\s+TABLE", r"class\s+\w+Repository", r"extends\s+JpaRepository"]
        return sum(len(re.findall(pattern, text, re.IGNORECASE)) for pattern in patterns)

    @staticmethod
    def _count_db_operations(text: str) -> int:
        patterns = [r"SELECT\s+", r"INSERT\s+", r"UPDATE\s+", r"DELETE\s+", r"\.save\(", r"\.find\("]
        return sum(len(re.findall(pattern, text, re.IGNORECASE)) for pattern in patterns)

    @staticmethod
    def _count_external_calls(text: str) -> int:
        patterns = [r"requests\.", r"axios\.", r"fetch\(", r"httpClient\.", r"RestTemplate\.", r"WebClient\."]
        return sum(len(re.findall(pattern, text)) for pattern in patterns)


@dataclass
class IfpugReport:
    external_inputs: int
    external_outputs: int
    external_inquiries: int
    internal_logical_files: int
    external_interface_files: int
    function_points: int
    details: Dict[str, int]


class IfpugEstimator:
    EI_WEIGHT = 3
    EO_WEIGHT = 4
    EQ_WEIGHT = 3
    ILF_WEIGHT = 7
    EIF_WEIGHT = 5

    def estimate(self, metrics: Sequence[FileMetrics]) -> IfpugReport:
        aggregated = FileMetrics(path=Path("<aggregate>"))
        for fm in metrics:
            aggregated.lines_of_code += fm.lines_of_code
            aggregated.function_defs += fm.function_defs
            aggregated.class_defs += fm.class_defs
            aggregated.db_entities += fm.db_entities
            aggregated.db_operations += fm.db_operations
            aggregated.external_calls += fm.external_calls
            aggregated.query_functions += fm.query_functions
            aggregated.input_functions += fm.input_functions
            aggregated.output_functions += fm.output_functions
            for method, count in fm.http_endpoints.items():
                aggregated.http_endpoints[method] += count

        external_inputs = aggregated.input_functions + aggregated.http_endpoints["post"] + aggregated.http_endpoints["put"] + aggregated.http_endpoints["patch"] + aggregated.http_endpoints["delete"]
        external_outputs = aggregated.output_functions + aggregated.http_endpoints["get"]
        external_inquiries = aggregated.query_functions
        internal_logical_files = max(aggregated.db_entities, 0)
        external_interface_files = max(aggregated.external_calls // 2, 0)

        function_points = (
            external_inputs * self.EI_WEIGHT
            + external_outputs * self.EO_WEIGHT
            + external_inquiries * self.EQ_WEIGHT
            + internal_logical_files * self.ILF_WEIGHT
            + external_interface_files * self.EIF_WEIGHT
        )

        details = {
            "lines_of_code": aggregated.lines_of_code,
            "function_defs": aggregated.function_defs,
            "class_defs": aggregated.class_defs,
            "http_endpoints_get": aggregated.http_endpoints["get"],
            "http_endpoints_post": aggregated.http_endpoints["post"],
            "http_endpoints_put": aggregated.http_endpoints["put"],
            "http_endpoints_delete": aggregated.http_endpoints["delete"],
            "http_endpoints_patch": aggregated.http_endpoints["patch"],
            "db_entities": aggregated.db_entities,
            "db_operations": aggregated.db_operations,
            "external_calls": aggregated.external_calls,
            "input_functions": aggregated.input_functions,
            "output_functions": aggregated.output_functions,
            "query_functions": aggregated.query_functions,
        }

        return IfpugReport(
            external_inputs=external_inputs,
            external_outputs=external_outputs,
            external_inquiries=external_inquiries,
            internal_logical_files=internal_logical_files,
            external_interface_files=external_interface_files,
            function_points=function_points,
            details=details,
        )


# ---------------------------------------------------------------------------
# Presentation helpers


def format_security_findings(findings: Sequence[SecurityFinding]) -> str:
    if not findings:
        return "No obvious security issues detected by heuristic scan."
    lines = ["Security Findings:"]
    for finding in findings:
        lines.append(
            f"- [{finding.severity.upper()}] {finding.pattern_id} in {finding.file} (line {finding.line}): "
            f"{finding.description} -> {finding.recommendation}"
        )
    return "\n".join(lines)


def format_dependencies(deps: Sequence[Dependency]) -> str:
    if not deps:
        return "No dependencies discovered in standard manifest files."
    lines = ["Discovered Dependencies:"]
    for dep in deps:
        version = dep.version or "(unspecified)"
        license_part = f", license={dep.license}" if dep.license else ""
        lines.append(f"- [{dep.ecosystem}] {dep.name} {version} (from {dep.source_file}){license_part}")
    return "\n".join(lines)


def format_compliance(issues: Sequence[ComplianceIssue], project_license: Optional[str]) -> str:
    lines: List[str] = []
    if project_license:
        lines.append(f"Project license detected: {project_license}")
    else:
        lines.append("Project license unknown.")

    if not issues:
        lines.append("No compliance issues identified by heuristic policy.")
    else:
        lines.append("Compliance considerations:")
        for issue in issues:
            scope = issue.dependency or "project"
            lines.append(f"- [{issue.severity.upper()}] {scope}: {issue.message}")
    return "\n".join(lines)


def format_ifpug(report: IfpugReport) -> str:
    lines = ["IFPUG Function Point Estimation:"]
    lines.append(f"- External Inputs (EI): {report.external_inputs}")
    lines.append(f"- External Outputs (EO): {report.external_outputs}")
    lines.append(f"- External Inquiries (EQ): {report.external_inquiries}")
    lines.append(f"- Internal Logical Files (ILF): {report.internal_logical_files}")
    lines.append(f"- External Interface Files (EIF): {report.external_interface_files}")
    lines.append(f"- Estimated Function Points: {report.function_points}")
    lines.append("Supporting metrics:")
    for key, value in sorted(report.details.items()):
        lines.append(f"  * {key}: {value}")
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# CLI


def run_analysis(root: Path) -> Dict[str, object]:
    security_analyzer = SecurityAnalyzer()
    findings = security_analyzer.analyze(root)

    oss_analyzer = OpenSourceAnalyzer(root)
    dependencies = oss_analyzer.analyze()

    compliance_analyzer = ComplianceAnalyzer(root, dependencies)
    compliance_issues, project_license = compliance_analyzer.analyze()

    metrics_collector = MetricsCollector()
    file_metrics = metrics_collector.collect(root)
    ifpug_estimator = IfpugEstimator()
    ifpug_report = ifpug_estimator.estimate(file_metrics)

    return {
        "security_findings": [
            {
                "file": str(finding.file),
                "line": finding.line,
                "pattern_id": finding.pattern_id,
                "severity": finding.severity,
                "description": finding.description,
                "recommendation": finding.recommendation,
            }
            for finding in findings
        ],
        "dependencies": [
            {
                "name": dep.name,
                "version": dep.version,
                "source_file": str(dep.source_file),
                "ecosystem": dep.ecosystem,
                "license": dep.license,
            }
            for dep in dependencies
        ],
        "compliance": {
            "issues": [
                {
                    "dependency": issue.dependency,
                    "message": issue.message,
                    "severity": issue.severity,
                }
                for issue in compliance_issues
            ],
            "project_license": project_license,
        },
        "ifpug": {
            "external_inputs": ifpug_report.external_inputs,
            "external_outputs": ifpug_report.external_outputs,
            "external_inquiries": ifpug_report.external_inquiries,
            "internal_logical_files": ifpug_report.internal_logical_files,
            "external_interface_files": ifpug_report.external_interface_files,
            "function_points": ifpug_report.function_points,
            "details": ifpug_report.details,
        },
    }


def main(argv: Optional[Sequence[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Static analysis helper for security, OSS, compliance, and IFPUG metrics.")
    parser.add_argument("--path", default=".", help="Target directory to analyze.")
    parser.add_argument(
        "--format",
        choices=["text", "json"],
        default="text",
        help="Output format.",
    )
    args = parser.parse_args(argv)

    root = Path(args.path).resolve()
    if not root.exists():
        parser.error(f"Path '{root}' does not exist.")

    report = run_analysis(root)

    if args.format == "json":
        json.dump(report, fp=os.sys.stdout, ensure_ascii=False, indent=2)
        os.sys.stdout.write("\n")
    else:
        text_parts = [
            format_security_findings(
                [SecurityFinding(**finding) for finding in report["security_findings"]]
            ),
            format_dependencies([Dependency(**dep) for dep in report["dependencies"]]),
            format_compliance(
                [ComplianceIssue(**issue) for issue in report["compliance"]["issues"]],
                report["compliance"]["project_license"],
            ),
            format_ifpug(
                IfpugReport(
                    external_inputs=report["ifpug"]["external_inputs"],
                    external_outputs=report["ifpug"]["external_outputs"],
                    external_inquiries=report["ifpug"]["external_inquiries"],
                    internal_logical_files=report["ifpug"]["internal_logical_files"],
                    external_interface_files=report["ifpug"]["external_interface_files"],
                    function_points=report["ifpug"]["function_points"],
                    details=report["ifpug"]["details"],
                )
            ),
        ]
        print("\n\n".join(text_parts))
    return 0


if __name__ == "__main__":  # pragma: no cover - CLI entry point
    raise SystemExit(main())

