import io
from pathlib import Path
import struct
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from android_page_sizes import validate_archive, validate_elf


def elf(alignment=16384, *, wide=True, relro_end=None, load_end=32768, offset=0):
    data = bytearray(512)
    data[:7] = b'\x7fELF' + bytes([2 if wide else 1, 1, 1])
    count = 2 if relro_end is not None else 1
    if wide:
        struct.pack_into('<Q', data, 32, 64)
        struct.pack_into('<HH', data, 54, 56, count)
        struct.pack_into('<IIQQQQQQ', data, 64, 1, 6, offset, 0, 0, 512, load_end, alignment)
        if count == 2:
            struct.pack_into('<IIQQQQQQ', data, 120, 0x6474e552, 4, 0, 0, 0, 256, relro_end, 1)
    else:
        struct.pack_into('<I', data, 28, 52)
        struct.pack_into('<HH', data, 42, 32, count)
        struct.pack_into('<IIIIIIII', data, 52, 1, offset, 0, 0, 512, load_end, 6, alignment)
        if count == 2:
            struct.pack_into('<IIIIIIII', data, 84, 0x6474e552, 0, 0, 0, 256, relro_end, 4, 1)
    return bytes(data)


class AndroidPageSizesTest(unittest.TestCase):
    def test_old_4kb_kernel_is_rejected_for_both_elf_classes(self):
        for wide in (True, False):
            with self.subTest(wide=wide), self.assertRaisesRegex(ValueError, 'LOAD alignment 4096'):
                validate_elf(elf(4096, wide=wide), 'kernel')

    def test_16kb_and_64kb_segments_are_supported(self):
        for alignment in (16384, 65536):
            for wide in (True, False):
                with self.subTest(alignment=alignment, wide=wide):
                    validate_elf(elf(alignment, wide=wide), 'kernel')

    def test_offset_must_be_congruent_with_virtual_address(self):
        with self.assertRaisesRegex(ValueError, 'LOAD alignment'):
            validate_elf(elf(offset=4096), 'kernel')

    def test_relro_must_not_protect_adjacent_writable_data(self):
        for wide in (True, False):
            with self.subTest(wide=wide), self.assertRaisesRegex(ValueError, 'RELRO boundary'):
                validate_elf(elf(wide=wide, relro_end=4096), 'kernel')
            validate_elf(elf(wide=wide, relro_end=16384), 'kernel')
            validate_elf(elf(wide=wide, relro_end=4096, load_end=4096), 'kernel')

    def test_malformed_or_truncated_elf_is_rejected(self):
        for data in (b'', b'not an ELF', elf()[:100]):
            with self.subTest(length=len(data)), self.assertRaises(ValueError):
                validate_elf(data, 'kernel')

    def test_aar_checks_native_segments_without_apk_zip_constraint(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'sdk.aar'
            for alignment in (4096, 16384):
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr('jni/arm64-v8a/kernel.so', elf(alignment))
                if alignment == 4096:
                    with self.assertRaisesRegex(ValueError, 'kernel.so'):
                        validate_archive(path)
                else:
                    self.assertEqual(1, validate_archive(path))

    def test_apk_uncompressed_library_requires_zip_alignment(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'app.apk'
            name = 'lib/arm64-v8a/kernel.so'
            for aligned in (False, True):
                info = zipfile.ZipInfo(name)
                if aligned:
                    padding = 16384 - 30 - len(name.encode())
                    info.extra = struct.pack('<HH', 0xd935, padding - 4) + bytes(padding - 4)
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr(info, elf())
                if aligned:
                    self.assertEqual(1, validate_archive(path))
                else:
                    with self.assertRaisesRegex(ValueError, 'ZIP entry'):
                        validate_archive(path)

    def test_apk_compressed_library_still_requires_elf_alignment(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'app.apk'
            with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as archive:
                archive.writestr('lib/arm64-v8a/kernel.so', elf(4096))
            with self.assertRaisesRegex(ValueError, 'LOAD alignment'):
                validate_archive(path)


if __name__ == '__main__':
    unittest.main()
