#!/usr/bin/env python3
"""Validate local markdown links and fragments within the Days 197–210 docs."""
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'docs/reboot/continuation-197-210'
files=list(BASE.glob('*.md'))+list((BASE/'contracts').glob('day-*.md'))
errors=[]
for source in files:
 text=source.read_text()
 for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)',text):
  if re.match(r'^[a-zA-Z][a-zA-Z0-9+.-]*:',target) or target.startswith('#'): continue
  path,_,fragment=target.partition('#');dest=(source.parent/path).resolve()
  if not dest.is_file(): errors.append(f'{source.relative_to(ROOT)}: missing {target}');continue
  if fragment:
   body=dest.read_text()
   anchors=set(re.findall(r'<a\s+id=["\']([^"\']+)["\']\s*>',body))
   anchors|=set(re.findall(r'^#{1,6}\s+.*?\{#([^}]+)\}',body,re.M))
   if fragment not in anchors: errors.append(f'{source.relative_to(ROOT)}: missing anchor #{fragment} in {dest.relative_to(ROOT)}')
if errors: raise SystemExit('\n'.join(errors))
print(f'validated local markdown links and fragments in {len(files)} Days 197–210 documents')
