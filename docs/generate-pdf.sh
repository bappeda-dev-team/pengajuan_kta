#!/bin/bash
#
# Convert aws-deployment-guide.md ke PDF
#
# Cara pakai:
#   1. Install pandoc:  brew install pandoc
#   2. Install basictex: brew install --cask basictex
#      Lalu restart terminal, kemudian:
#      sudo tlmgr update --self
#      sudo tlmgr install collection-fontsrecommended
#   3. Jalankan: ./generate-pdf.sh
#
# Alternatif (tanpa LaTeX, via HTML):
#   1. Install pandoc: brew install pandoc
#   2. Install wkhtmltopdf: brew install --cask wkhtmltopdf
#   3. Jalankan: ./generate-pdf.sh html
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INPUT="$SCRIPT_DIR/aws-deployment-guide.md"
OUTPUT="$SCRIPT_DIR/aws-deployment-guide.pdf"

if ! command -v pandoc &> /dev/null; then
    echo "ERROR: pandoc belum terinstall."
    echo "Install dengan: brew install pandoc"
    exit 1
fi

if [ "$1" = "html" ]; then
    echo "Converting via HTML..."
    pandoc "$INPUT" \
        -o "$OUTPUT" \
        --from markdown \
        --pdf-engine=wkhtmltopdf \
        --metadata title="AWS Deployment Guide - Pengajuan KTA" \
        --metadata author="Bappeda Dev Team" \
        --metadata date="$(date +%Y-%m-%d)" \
        -V margin-top=20mm \
        -V margin-bottom=20mm \
        -V margin-left=15mm \
        -V margin-right=15mm
else
    echo "Converting via LaTeX..."
    pandoc "$INPUT" \
        -o "$OUTPUT" \
        --from markdown \
        --pdf-engine=xelatex \
        --metadata title="AWS Deployment Guide - Pengajuan KTA" \
        --metadata author="Bappeda Dev Team" \
        --metadata date="$(date +%Y-%m-%d)" \
        -V geometry:margin=2cm \
        -V fontsize=11pt \
        -V monofont="Courier New" \
        -V colorlinks=true \
        -V linkcolor=blue \
        -V urlcolor=blue \
        --toc \
        --toc-depth=3 \
        --highlight-style=tango
fi

echo "PDF generated: $OUTPUT"
