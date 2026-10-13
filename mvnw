#!/bin/sh
# Portable project helper (not the official Maven Wrapper).
# Uses installed Maven when available, then a Maven distribution cached by the
# Maven Wrapper. No project-specific machine path or shell setup is required.
set -eu

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

if [ -n "${MAVEN_USER_HOME:-}" ]; then
    maven_cache="$MAVEN_USER_HOME/wrapper/dists"
elif [ -n "${HOME:-}" ]; then
    maven_cache="$HOME/.m2/wrapper/dists"
else
    maven_cache=""
fi

if [ -n "$maven_cache" ] && [ -d "$maven_cache" ]; then
    maven_bin=$(find "$maven_cache" -type f -path '*/bin/mvn' -print 2>/dev/null | sort -V | tail -n 1)
    if [ -n "$maven_bin" ]; then
        exec "$maven_bin" "$@"
    fi
fi

cat >&2 <<'EOF'
No Maven executable was found.
Install Maven 3.9+ or configure the official Maven Wrapper for this project,
then rerun this command. This project helper does not download Maven.
EOF
exit 127
