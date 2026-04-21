# Steganography

Hide and recover AES-encrypted secret messages inside PNG images using the
Least Significant Bit (LSB) method.  The modified image looks visually
identical to the original, and the hidden data is unreadable without the
correct passphrase.

Two tools are provided:

- **`Steganography.java`** — hides/extracts a text message string
- **`SteganographyForFiles.java`** — hides/extracts arbitrary binary files

---

## Requirements

- Java 11 or later (no build tool needed — single source file each)

---

## What is LSB steganography?

Every pixel in a colour image stores three 8-bit channel values: red,
green, and blue.  Changing only the lowest-order bit (the LSB) of each
channel produces a colour shift of at most 1/255 — invisible to the human
eye.

This program uses three channels per pixel, so a **W × H** image can
store up to **W × H × 3 bits** of payload data.

```
Before:  R = 1001 0110   G = 0110 1100   B = 1110 0001
After:   R = 1001 0111   G = 0110 1101   B = 1110 0001
                 ^                ^               ^ (unchanged)
          bit 0 written    bit 1 written
```

To make the pixel order unpredictable, the traversal uses a deterministic
Fisher-Yates shuffle seeded from the SHA-256 hash of the passphrase.

---

## Compile and run

### Compile

```bash
javac Steganography.java
```

### Hide a message

```bash
java Steganography encode original.png secret.png "My secret text" mypassphrase
```

### Reveal a message

```bash
java Steganography decode secret.png mypassphrase
```

### Check image capacity (no encoding)

```bash
java Steganography info original.png
```

---

## Running the tests

`TestRunner` is a static inner class of `Steganography`.  No display,
audio hardware, or external files are needed.

```bash
javac Steganography.java && java 'Steganography$TestRunner'
```

Expected output:

```
=== Steganography Tests ===
  PASS: encode/decode round-trip ("Hello")
  PASS: encode/decode empty string
  PASS: capacity overflow throws IllegalArgumentException
  PASS: AES encrypt/decrypt round-trip
  PASS: CRC32 consistency

5 passed, 0 failed.
```

---

## Hiding and revealing text — walkthrough

1. **Compile** the file once with `javac Steganography.java`.
2. **Choose a PNG cover image** — any lossless PNG works.
   JPEG images are not supported because JPEG compression would destroy
   the embedded bits.
3. **Encode** by running the encode command above.  A new PNG is written
   that is pixel-identical in appearance to the original.
4. **Verify** with `java Steganography decode secret.png mypassphrase`.
5. **Share** `secret.png`.  Without the passphrase, the image looks like
   any ordinary photo.

---

## AES encryption details

| Property | Value |
|----------|-------|
| Algorithm | AES-128-CBC with PKCS#5 padding |
| Key derivation | SHA-256(passphrase UTF-8 bytes), first 16 bytes used |
| IV | 16 cryptographically random bytes generated fresh per encode |
| Integrity check | CRC32 appended to plaintext before encryption |

### Key derivation

```java
byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes("UTF-8"));
byte[] aesKey = Arrays.copyOf(hash, 16);  // AES-128: 128-bit key
```

### Payload layout

```
[ 32 bits: payload length, big-endian                 ]
[ 16 bytes: random IV                                 ]
[ N bytes: AES-CBC ciphertext                         ]
  └─ decrypts to: [ message UTF-8 bytes ] [ 4-byte CRC32 ]
```

The 32-bit length header is also spread across LSBs using the first 32
pixel-channels in the shuffled order.

---

## Capacity limits

| Image size | Raw pixels | Usable bits | Usable bytes | Approx. text chars |
|------------|-----------|-------------|-------------|-------------------|
| 100 × 100 | 10,000 | 30,000 | 3,750 | ~3,710 |
| 512 × 512 | 262,144 | 786,432 | 98,304 | ~98,264 |
| 1920 × 1080 | 2,073,600 | 6,220,800 | 777,600 | ~777,560 |

**Overhead per message:** 4 (length header) + 16 (IV) + 4 (CRC32) + up to 16
(AES PKCS#5 padding) = **40 bytes minimum**.

The minimum valid payload for any message (including the empty string) is
**32 bytes**: 16-byte IV + at least one 16-byte AES ciphertext block for the
4-byte CRC + padding.

---

## SteganographyForFiles.java

`SteganographyForFiles.java` is a variant that embeds an **arbitrary binary
file** rather than a text string.  The algorithm is identical: AES-128-CBC
encryption, CRC32 integrity, and LSB pixel embedding with a key-shuffled
pixel order.

### Usage

```bash
javac SteganographyForFiles.java

# Embed a file
java SteganographyForFiles encode carrier.png output.png secret.pdf mypassphrase

# Extract the embedded file
java SteganographyForFiles decode output.png recovered.pdf mypassphrase
```

### Differences from Steganography.java

| Feature | Steganography.java | SteganographyForFiles.java |
|---------|-------------------|---------------------------|
| Payload type | UTF-8 text string | Arbitrary binary file |
| Input source | Command-line argument | File path argument |
| Output target | Printed to stdout | Written to file path |
| Capacity unit | Characters | Bytes |

---

## Included sample files

| File | Description |
|------|-------------|
| `hamlet.txt` | Source text for encoding examples |
| `steg1.png` | Example output with a hidden message |
| `AppleBananaCherry42.png` | Cover image used in lab exercises |
