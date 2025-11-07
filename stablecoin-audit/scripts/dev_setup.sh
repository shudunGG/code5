#!/usr/bin/env bash
set -euo pipefail

# Placeholder for initializing local environment.
echo "Setting up local environment..."
python3 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt 2>/dev/null || true
