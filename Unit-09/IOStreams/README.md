# Java I/O Streams — Graduated Examples

Four self-contained Maven projects that build from raw byte streams to the modern NIO.2 API.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-ByteStreams` | Byte streams, decorator pattern, ByteArray streams, buffering, DataStream |
| 2 | `02-CharacterStreams` | Reader/Writer, BufferedReader, InputStreamReader bridge, charset encoding |
| 3 | `03-ResourcesAndNIO` | try-with-resources, Files utility, Path API, directory walking |
| 4 | `04-ChannelsAndBuffers` | ByteBuffer mechanics, FileChannel, transferTo, memory-mapped files |

## Running any example

```
cd 01-ByteStreams
mvn compile exec:java
```

## The mental model

```
01  byte streams         InputStream / OutputStream — raw bytes, binary data
                         Decorator: File → Buffered → Data (wrap for more features)

02  character streams    Reader / Writer — text decoded via a Charset
                         Bridge: InputStreamReader wraps a byte stream with a Charset

03  NIO.2 Files API      Path + Files utility — most file tasks in one or two lines
                         Directory walking, attributes, watching

04  NIO channels         Channel + ByteBuffer — high-throughput, non-blocking capable
                         flip() / clear() lifecycle, transferTo, memory-mapped files
```
