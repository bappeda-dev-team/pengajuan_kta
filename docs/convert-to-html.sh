#!/bin/bash
#
# Convert aws-deployment-guide.md ke HTML yang bisa di-print sebagai PDF
# Tidak perlu install apapun. Hanya butuh terminal & browser.
#
# Cara pakai:
#   ./convert-to-html.sh
#   Lalu buka file .html di browser → Cmd+P → Save as PDF
#

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INPUT="$SCRIPT_DIR/aws-deployment-guide.md"
OUTPUT="$SCRIPT_DIR/aws-deployment-guide.html"

# Simple markdown to HTML (basic conversion using built-in tools)
cat > "$OUTPUT" << 'HEADER'
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>AWS Deployment Guide - Pengajuan KTA (Bappeda)</title>
<style>
HEADER

cat >> "$OUTPUT" << 'STYLES'
@media print {
  body { font-size: 10pt; }
  pre { page-break-inside: avoid; }
  h1, h2, h3 { page-break-after: avoid; }
  .no-print { display: none; }
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  line-height: 1.7;
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  color: #1a1a2e;
  background: #fff;
}
h1 {
  color: #16213e;
  border-bottom: 3px solid #0f3460;
  padding-bottom: 12px;
  font-size: 28px;
}
h2 {
  color: #0f3460;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
  margin-top: 48px;
  font-size: 22px;
}
h3 {
  color: #16213e;
  margin-top: 32px;
  font-size: 17px;
}
pre {
  background: #1e293b;
  color: #e2e8f0;
  padding: 16px 20px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
  border-left: 4px solid #3b82f6;
}
code {
  background: #f1f5f9;
  color: #e11d48;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
}
pre code {
  background: none;
  color: #e2e8f0;
  padding: 0;
}
table {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
  font-size: 14px;
}
th, td {
  border: 1px solid #cbd5e1;
  padding: 10px 14px;
  text-align: left;
}
th {
  background: #0f3460;
  color: white;
  font-weight: 600;
}
tr:nth-child(even) { background: #f8fafc; }
tr:hover { background: #e2e8f0; }
blockquote {
  border-left: 4px solid #f59e0b;
  background: #fffbeb;
  padding: 12px 20px;
  margin: 16px 0;
  border-radius: 0 8px 8px 0;
  color: #92400e;
}
hr {
  border: none;
  border-top: 2px solid #e2e8f0;
  margin: 40px 0;
}
.print-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #0f3460;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  z-index: 1000;
}
.print-btn:hover { background: #1e40af; }
ul, ol { padding-left: 24px; }
li { margin-bottom: 4px; }
</style>
</head>
<body>
<button class="print-btn no-print" onclick="window.print()">Print / Save PDF</button>
STYLES

# Read markdown and convert to basic HTML
python3 -c "
import re, html as h

with open('$INPUT', 'r') as f:
    content = f.read()

lines = content.split('\n')
result = []
in_code = False
in_table = False
in_list = False
in_blockquote = False

for line in lines:
    # Code blocks
    if line.startswith('\`\`\`'):
        if in_code:
            result.append('</code></pre>')
            in_code = False
        else:
            lang = line[3:].strip()
            result.append(f'<pre><code>')
            in_code = True
        continue

    if in_code:
        result.append(h.escape(line))
        continue

    # Blockquote
    if line.startswith('> '):
        if not in_blockquote:
            result.append('<blockquote>')
            in_blockquote = True
        text = line[2:]
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        text = re.sub(r'\`(.+?)\`', r'<code>\1</code>', text)
        result.append(f'<p>{text}</p>')
        continue
    elif in_blockquote and not line.startswith('>'):
        result.append('</blockquote>')
        in_blockquote = False

    # Table
    if '|' in line and line.strip().startswith('|'):
        cells = [c.strip() for c in line.strip().strip('|').split('|')]
        if all(set(c) <= set('-: ') for c in cells):
            continue
        if not in_table:
            result.append('<table>')
            result.append('<tr>' + ''.join(f'<th>{c}</th>' for c in cells) + '</tr>')
            in_table = True
        else:
            row_cells = []
            for c in cells:
                c = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', c)
                c = re.sub(r'\`(.+?)\`', r'<code>\1</code>', c)
                row_cells.append(c)
            result.append('<tr>' + ''.join(f'<td>{c}</td>' for c in row_cells) + '</tr>')
        continue
    elif in_table:
        result.append('</table>')
        in_table = False

    # Headers
    if line.startswith('# '):
        result.append(f'<h1>{line[2:]}</h1>')
    elif line.startswith('## '):
        text = line[3:]
        anchor = text.lower().replace(' ', '-').replace('&', '').replace('(', '').replace(')', '')
        result.append(f'<h2 id=\"{anchor}\">{text}</h2>')
    elif line.startswith('### '):
        result.append(f'<h3>{line[4:]}</h3>')
    elif line.startswith('#### '):
        result.append(f'<h4>{line[5:]}</h4>')
    elif line.startswith('---'):
        result.append('<hr>')
    elif line.startswith('- [ ] '):
        text = line[6:]
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        text = re.sub(r'\`(.+?)\`', r'<code>\1</code>', text)
        result.append(f'<p>&#9744; {text}</p>')
    elif line.startswith('- '):
        text = line[2:]
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        text = re.sub(r'\`(.+?)\`', r'<code>\1</code>', text)
        result.append(f'<p>&#8226; {text}</p>')
    elif line.strip() == '':
        result.append('<br>')
    else:
        text = line
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        text = re.sub(r'\`(.+?)\`', r'<code>\1</code>', text)
        text = re.sub(r'\[(.+?)\]\((.+?)\)', r'<a href=\"\2\">\1</a>', text)
        result.append(f'<p>{text}</p>')

if in_table:
    result.append('</table>')
if in_blockquote:
    result.append('</blockquote>')

print('\n'.join(result))
" >> "$OUTPUT"

cat >> "$OUTPUT" << 'FOOTER'
</body>
</html>
FOOTER

echo ""
echo "==================================="
echo " HTML berhasil di-generate!"
echo " File: $OUTPUT"
echo "==================================="
echo ""
echo "Cara convert ke PDF:"
echo "  1. Buka file di browser: open $OUTPUT"
echo "  2. Tekan Cmd+P (atau klik tombol 'Print / Save PDF')"
echo "  3. Pilih 'Save as PDF'"
echo "  4. Klik Save"
echo ""
