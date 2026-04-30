# Information Representation

Eight self-contained Maven modules covering how computers store, transform, protect, and measure information — from raw bytes to entropy, from Caesar ciphers to Hamming codes.

Each module is independent. Run any one with `mvn exec:java` or explore the concepts through its test suite (`mvn test`). All modules require **Java 21** and **Maven 3.6+**.

---

## Modules

| # | Directory | Topic |
|---|-----------|-------|
| 01 | [Encoding](01-Encoding/) | Base64, Hex, URL encoding, charset widths, magic-byte file detection |
| 02 | [Hashing](02-Hashing/) | SHA-256, MD5, HMAC, salted password hashing, consistent hashing |
| 03 | [Serialization](03-Serialization/) | Java object streams, custom binary format, key-value text format |
| 04 | [Compression](04-Compression/) | Run-length encoding, Huffman coding, GZIP |
| 05 | [Encryption](05-Encryption/) | Caesar cipher + frequency analysis, AES-256-CBC |
| 06 | [Randomness](06-Randomness/) | PRNG vs CSPRNG, seeding, Monte Carlo π, weighted sampling |
| 07 | [Information Theory](07-Information-Theory/) | Shannon entropy, self-information, redundancy, GZIP as complexity |
| 08 | [Error Detection](08-Error-Detection/) | Even parity, checksum-8, CRC-32, Hamming(7,4) |

---

## Running a module

```bash
cd 01-Encoding
mvn exec:java          # run Main
mvn test               # run the test suite
```

---

## Conceptual map

```
          How do we store binary data as text?
          ┌────────────────────────────────────┐
          │  01 Encoding                        │
          │  Base64 / Hex / URL / magic bytes   │
          └─────────────────┬──────────────────┘
                            │
          How do we verify and fingerprint data?
          ┌─────────────────▼──────────────────┐
          │  02 Hashing                         │
          │  SHA-256, HMAC, password hashing    │
          └─────────────────┬──────────────────┘
                            │
          How do we persist and transmit data?
     ┌────────────────────────────────────────────┐
     │  03 Serialization       04 Compression      │
     │  Object streams         RLE + Huffman        │
     │  Binary / text          + GZIP              │
     └──────────────────┬─────────────────────────┘
                        │
          How do we keep data secret or reliable?
     ┌──────────────────▼─────────────────────────┐
     │  05 Encryption          08 Error Detection   │
     │  Caesar + AES-CBC       Parity, CRC, Hamming │
     └──────────────────┬─────────────────────────┘
                        │
          How do we measure the information itself?
          ┌─────────────▼──────────────────────┐
          │  06 Randomness   07 Info Theory     │
          │  PRNG / CSPRNG   Entropy, redundancy│
          │  Monte Carlo     Kolmogorov ≈ GZIP  │
          └────────────────────────────────────┘
```

---

## Key ideas across modules

| Concept | Where it appears |
|---------|-----------------|
| Encoding ≠ encryption — anyone can reverse it | 01, 05 |
| A hash is a one-way fingerprint of arbitrary data | 02, 07 |
| Compression exploits redundancy (predictable structure) | 04, 07 |
| Entropy measures unpredictability — encrypted data looks random | 05, 07 |
| Random IVs ensure identical plaintexts encrypt differently | 05 |
| Error detection adds redundancy to catch corruption | 08 |
| Hamming codes detect AND correct; CRC-32 only detects | 08 |
| Kolmogorov complexity ≈ shortest program ≈ GZIP size | 07 |
