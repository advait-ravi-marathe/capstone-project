#!/usr/bin/env python3
"""Match the shell package builder's command substitution hash semantics."""
import hashlib

def builder_manifest_hash(raw: bytes) -> str:
    # Bash command substitution removes all trailing LF bytes. CR bytes from CRLF remain.
    payload=raw.rstrip(b'\n')
    return hashlib.sha256(payload).hexdigest()

if __name__=='__main__':
    import sys
    data=sys.stdin.buffer.read()
    print(builder_manifest_hash(data))
