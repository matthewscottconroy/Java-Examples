# Steganography and Information Hiding

## Two Ways to Keep a Secret

There are two fundamentally different strategies for protecting a message:

**Cryptography** hides the *meaning* of a message. The message's existence is
known; only its content is secret. A locked safe is visible; its contents are
not.

**Steganography** hides the *existence* of a message. The cover medium (an
image, a document, an audio file) appears innocent and unremarkable. An observer
does not know there is anything to look for.

The name comes from Greek: *steganos* (covered, concealed) + *graphos* (writing).
The practice is ancient — Greek historians describe shaving a messenger's head,
tattooing a message on the scalp, and waiting for the hair to grow back before
sending him. Modern digital steganography achieves the same goal with mathematics.

---

## The Human Visual System as a Target

The key insight behind image steganography is a property of human perception:
**we cannot reliably detect small changes in color**.

A pixel in a color image stores three 8-bit values: Red, Green, Blue. Each
channel ranges from 0 to 255. The least significant bit (LSB) of each channel
controls a change of 1 part in 255 — a color difference of less than 0.4%.

Compare:
```
Original pixel:    R = 1001 0110  (150)   G = 0110 1100  (108)   B = 1110 0001  (225)
Modified pixel:    R = 1001 0111  (151)   G = 0110 1101  (109)   B = 1110 0001  (225)
                           ^                       ^
                     bit changed               bit changed
```

The color shift is invisible. Yet two bits of secret data have been embedded in
a single pixel — one bit in the red channel, one in the green.

With three channels per pixel, a W×H image can store W×H×3 bits of payload. A
modest 512×512 image holds nearly 100 kilobytes of hidden data.

---

## Why PNG, Not JPEG?

The choice of image format is not incidental. **JPEG** uses *lossy compression*:
it discards fine details (including slight color variations) to achieve small
file sizes. If you embed data in the LSBs of a JPEG-source image, those bits
will be destroyed when the image is saved or transmitted in JPEG format.

**PNG** uses *lossless compression*: it compresses the image without any loss
of information. Every pixel value is preserved exactly. An LSB-encoded PNG
can be saved and transmitted without disturbing the hidden payload.

This illustrates a general principle in information hiding: the cover medium
must have enough *redundancy* to absorb the hidden data, and the transmission
channel must preserve that redundancy exactly.

---

## The Pixel Traversal Order

Naively, you might embed bits in pixels left-to-right, top-to-bottom. An
attacker who suspects steganography would check the LSBs in that order.

This simulator uses a **keyed random permutation** of the pixel order. The
passphrase is hashed with SHA-256, producing a 256-bit seed. A deterministic
Fisher-Yates shuffle uses that seed to generate a random ordering of all pixels.
The hidden bits are embedded in that shuffled order.

Without the passphrase, an attacker does not know which pixels carry the message,
in what order, or what the bits mean even if extracted. The permutation acts as
a first layer of security.

---

## Encryption: Protecting the Content

Steganography hides the message's existence; encryption protects its meaning.
This simulator combines both.

Before embedding, the message is encrypted with **AES-128-CBC**:

1. **Key derivation**: the passphrase is hashed with SHA-256; the first 16 bytes
   of the hash become the AES key. SHA-256 ensures that even a short passphrase
   ("cat") produces a full 128-bit key.

2. **Random IV**: a fresh 16-byte **initialization vector** is generated
   cryptographically randomly for each encoding. The IV ensures that encoding
   the same message twice with the same passphrase produces different ciphertext
   each time, preventing pattern analysis.

3. **CBC mode**: each 16-byte plaintext block is XOR'd with the previous
   ciphertext block before encryption. This chains the blocks together: changing
   one plaintext byte affects all subsequent ciphertext blocks, preventing
   block-level substitution attacks.

The payload stored in the image is:
```
[ 32-bit length, big-endian ] [ 16-byte IV ] [ AES-CBC ciphertext ]
```

The 32-bit length field allows the receiver to extract exactly the right number
of bits from the image. The IV is transmitted in plaintext (it must be: the
receiver needs it to decrypt), but it is not secret — its purpose is randomness,
not secrecy.

---

## Integrity Verification

How does the receiver know the decrypted data is correct — that the passphrase
was right and no corruption occurred?

A **CRC32 checksum** is appended to the plaintext before encryption. After
decryption, the receiver recomputes the CRC and verifies it matches. If the
passphrase is wrong, AES produces garbage, the CRC check fails, and the message
is rejected.

This is a general pattern: **encrypt-then-MAC** (Message Authentication Code).
The CRC here plays the MAC role — a short checksum that detects corruption or
tampering. Real production systems use HMAC-SHA256 or AES-GCM for stronger
authentication guarantees.

---

## What Steganographic Analysis Looks For

An analyst who suspects a PNG file contains hidden data might apply:

**Statistical analysis**: in an unaltered image, the LSBs of pixels are
correlated with the image's natural texture — they fluctuate in a way that
reflects the image content. If every LSB has been overwritten with pseudorandom
encrypted data, the statistical distribution of LSBs becomes artificially
uniform. This is detectable.

**Visual inspection of the bit plane**: extracting the entire LSB plane and
displaying it as a black-and-white image reveals patterns. A clean image shows
structure (edges, gradients); an image with embedded data shows apparent noise.

**Chi-square attack**: pairs of values that differ only in the LSB (e.g., 100
and 101) should appear in natural proportions in a clean image. Sequential LSB
embedding disturbs these proportions. The keyed random permutation used here
partially mitigates this by distributing disturbances non-sequentially.

The field of **steganalysis** (detecting steganography) and **steganography**
are in constant adversarial development — an arms race analogous to
cryptanalysis and cryptography.

---

## Significance in Computing

Steganography sits at the intersection of information theory, cryptography, and
signal processing. Its applications range from the practical to the profound:

**Digital watermarking**: copyright holders embed invisible identifiers in
images and audio. If a watermarked file is found without authorization, the
embedded ID identifies its origin. The music and film industries use this to
trace leaks.

**Network steganography**: data can be hidden in protocol headers, timing
between packets, or unused fields in network frames. Malware has been observed
using covert channels in DNS queries, ICMP packets, and TCP sequence numbers
to exfiltrate data while evading firewalls.

**Censorship circumvention**: in environments where encrypted traffic is blocked
or suspicious, steganography can hide communications inside innocuous-looking
image transfers.

**Forensics**: digital forensics investigators look for steganographic content
in suspected devices. The presence of steganographic tools, high-entropy
regions in images, or statistical anomalies in LSBs can be evidence of hidden
communication.

---

## What to Try in the Tool

- Encode a short message in `AppleBananaCherry42.png` and then compare the
  original and encoded files side-by-side. They should look identical.
- Check the file sizes: because PNG uses lossless compression on pixel data,
  and the LSBs you changed are pseudo-random (high entropy), the compressed PNG
  may actually be *slightly larger* after encoding — the entropy of the pixel
  data increased.
- Try encoding a very long message and observe when the capacity is exceeded.
  The capacity formula `W × H × 3 / 8` gives the usable bytes.
- Run the test suite and observe the round-trip test: encode a message, decode
  it, verify they match exactly. Then change one bit of the passphrase and
  observe the CRC failure.
- Try encoding the same message twice with the same passphrase and compare the
  output images (using a hex editor on the raw pixel bytes if possible). They
  should differ — because the random IV changes every time.
