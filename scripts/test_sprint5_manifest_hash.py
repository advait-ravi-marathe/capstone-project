import hashlib
import unittest
from scripts.check_sprint5_manifest_hash import builder_manifest_hash

class Sprint5ManifestHashTest(unittest.TestCase):
    def test_lf_builder_command_substitution_removes_trailing_lf(self):
        fixture=b'Manifest-Version: 1.0\nStart-Class: assignments.sprint5.Sprint5LocalApplication\n\n'
        # Simulates printf '%s' "$manifest" after Bash command substitution.
        expected=hashlib.sha256(fixture.rstrip(b'\n')).hexdigest()
        self.assertEqual(expected,builder_manifest_hash(fixture))

    def test_crlf_builder_command_substitution_preserves_cr_bytes(self):
        fixture=b'Manifest-Version: 1.0\r\nStart-Class: assignments.sprint5.Sprint5LocalApplication\r\n\r\n'
        expected=hashlib.sha256(fixture.rstrip(b'\n')).hexdigest()
        self.assertEqual(expected,builder_manifest_hash(fixture))
        self.assertNotEqual(hashlib.sha256(fixture.decode().encode()).hexdigest(),expected)

    def test_hash_changes_if_internal_or_nontrailing_bytes_change(self):
        one=b'Manifest-Version: 1.0\r\n\r\n'
        two=b'Manifest-Version: 1.1\r\n\r\n'
        self.assertNotEqual(builder_manifest_hash(one),builder_manifest_hash(two))

if __name__=='__main__': unittest.main()
