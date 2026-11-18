#!/usr/bin/env python3
"""Validate native ELF segments/RELRO and uncompressed APK entries for 16 KB pages."""
import argparse
from pathlib import Path
import struct
import zipfile

PAGE_SIZE = 16 * 1024


def validate_elf(data: bytes, label: str) -> None:
    """Read program headers directly so validation also works on Windows without an NDK."""
    if len(data) < 52 or data[:4] != b'\x7fELF' or data[4] not in (1, 2) or data[5] not in (1, 2):
        raise ValueError(f'{label}: invalid ELF header')
    endian = '<' if data[5] == 1 else '>'
    wide = data[4] == 2
    try:
        offset = struct.unpack_from(endian + ('Q' if wide else 'I'), data, 32 if wide else 28)[0]
        stride, count = struct.unpack_from(endian + 'HH', data, 54 if wide else 42)
        fmt = endian + ('IIQQQQQQ' if wide else 'IIIIIIII')
        if not count or stride < struct.calcsize(fmt) or offset + stride * count > len(data):
            raise ValueError(f'{label}: invalid ELF program headers')
        headers = []
        for index in range(count):
            values = struct.unpack_from(fmt, data, offset + stride * index)
            kind, flags, file_offset, address, _, _, memory_size, alignment = (
                values if wide else (values[0], values[6], *values[1:6], values[7]))
            headers.append((kind, flags, file_offset, address, memory_size, alignment))
    except struct.error as error:
        raise ValueError(f'{label}: truncated ELF header') from error
    loads = [header for header in headers if header[0] == 1]
    if not loads:
        raise ValueError(f'{label}: no ELF LOAD segments')
    for _, _, file_offset, address, _, alignment in loads:
        if alignment < PAGE_SIZE or alignment & (alignment - 1) or (address - file_offset) % PAGE_SIZE:
            raise ValueError(f'{label}: ELF LOAD alignment {alignment} is incompatible with 16 KB pages')
    for kind, _, _, address, memory_size, _ in headers:
        if kind != 0x6474e552:  # PT_GNU_RELRO
            continue
        end = address + memory_size
        for _, flags, _, load_address, load_size, _ in loads:
            if flags & 2 and load_address <= address < load_address + load_size:
                if end < load_address + load_size and end % PAGE_SIZE:
                    raise ValueError(f'{label}: ELF RELRO boundary is incompatible with 16 KB pages')


def validate_archive(path: Path) -> int:
    count = 0
    errors = []
    with zipfile.ZipFile(path) as archive, path.open('rb') as raw:
        for entry in archive.infolist():
            if not entry.filename.endswith('.so'):
                continue
            count += 1
            try:
                validate_elf(archive.read(entry), f'{path.name}!{entry.filename}')
                if path.suffix.lower() == '.apk' and entry.compress_type == zipfile.ZIP_STORED:
                    raw.seek(entry.header_offset)
                    header = raw.read(30)
                    name_size, extra_size = struct.unpack_from('<HH', header, 26)
                    data_offset = entry.header_offset + 30 + name_size + extra_size
                    if data_offset % PAGE_SIZE:
                        raise ValueError(f'{entry.filename}: APK ZIP entry is not 16 KB aligned')
            except (ValueError, struct.error) as error:
                errors.append(str(error))
    if errors:
        raise ValueError('\n'.join(errors))
    return count


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('archives', type=Path, nargs='+', help='APK or AAR files to validate')
    args = parser.parse_args()
    for path in args.archives:
        try:
            count = validate_archive(path)
            if not count:
                raise ValueError(f'{path}: no native libraries found')
        except (OSError, ValueError, zipfile.BadZipFile) as error:
            parser.exit(1, f'{error}\n')
        print(f'{path.name}: {count} native libraries support 16 KB pages (ELF/RELRO/ZIP).')


if __name__ == '__main__':
    main()
