#!/bin/sh
set -eu
repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
output_dir=$(mktemp -d)
trap 'rm -rf "$output_dir"' EXIT HUP INT TERM
sources="\
$repo_root/src/main/java/coverstory/platform/crypto/EncryptionKey.java \
$repo_root/src/main/java/coverstory/platform/crypto/EncryptionKeyProvider.java \
$repo_root/src/main/java/coverstory/platform/crypto/EncryptedField.java \
$repo_root/src/main/java/coverstory/platform/crypto/FieldCipher.java \
$repo_root/src/main/java/coverstory/platform/crypto/AesGcmFieldCipher.java \
$repo_root/src/test/java/coverstory/platform/crypto/FieldCipherSmokeTest.java"
if command -v javac >/dev/null 2>&1; then
    # Intentionally word-split the fixed local Java source list.
    # shellcheck disable=SC2086
    javac -d "$output_dir" $sources
elif java --list-modules | grep -q '^jdk.compiler@'; then
    # The current runtime image includes the compiler module but omits its javac launcher.
    # shellcheck disable=SC2086
    java --module jdk.compiler/com.sun.tools.javac.Main -d "$output_dir" $sources
else
    echo "A Java 21 compiler is required (install a JDK or enable jdk.compiler)." >&2
    exit 2
fi
java -cp "$output_dir" coverstory.platform.crypto.FieldCipherSmokeTest
