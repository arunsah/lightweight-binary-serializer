# Lightweight Binary Serializer (LBS)

> **Lightweight Binary Serializer (LBS)** is a high-performance, compact, and extensible library for binary data exchange. It uses self-describing field headers with variable-length encoding to efficiently serialize primitives, arrays, and nested objects, making it ideal for search engines, graph databases, messaging systems, and other high-performance applications.

---

## Introduction

The Lightweight Binary Serializer is a high-performance, compact, and extensible binary encoding framework designed to efficiently serialize and deserialize structured data. It uses a self-describing field header that packs a field’s data type in the high 4 bits and its field identifier in the low 4 bits. For field identifiers that exceed the 4-bit limit, LBS employs a variable-length 7-bit varint extension with a continuation flag, ensuring that even very large field IDs can be encoded compactly.

For more complex types—such as arrays, nested objects, lists, and maps—LBS reserves specific type codes (or extension types) to capture additional metadata like element types. Length prefixes for strings and arrays are also encoded as variable-length integers, providing efficient space usage for small values while retaining the flexibility to represent large sizes.

Designed with schema evolution and cross-language interoperability in mind, the serializer is optimized for minimal overhead, making it ideal for applications like search engines, graph databases, messaging systems, and IoT communications.

## Features

- **Compact Encoding:**  
  Each field is prefixed with a one-byte header that packs the data type (high 4 bits) and the field identifier (low 4 bits). Field IDs exceeding 4 bits are encoded using a 7-bit variable-length (varint) scheme.

- **Self-Describing Format:**  
  The header format makes it easy to decode data without external schema files. Extended types (such as arrays and nested objects) are supported via a reserved extension space.

- **Flexible Data Types:**  
  Supports primitives, strings, byte arrays, arrays, and nested objects, with built-in support for schema evolution.

- **Variable-Length Encoding:**  
  Employs 7-bit varint encoding for field IDs and length prefixes, ensuring efficient storage for small numbers.

- **Cross-Language Interoperability:**  
  Designed to be simple and portable, making it easy to implement in multiple languages.

- **Influenced by Industry Leaders:**  
  The design draws inspiration from **Protocol Buffers** for its efficient varint encoding and schema evolution capabilities.

## Getting Started

### Installation

_(TODO: Explain how to include the library in project – e.g., via Maven, or as a jar file.)_

### Usage Example

```java
import arunsah.lbs.BinaryInput;
import arunsah.lbs.BinaryInputImpl;
import arunsah.lbs.BinaryOutput;
import arunsah.lbs.BinaryOutputImpl;
import arunsah.lbs.FieldHeader;
import arunsah.lbs.FieldType;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SimpleExample {
    public static void main(String[] args) {
        // Create a ByteBuffer with sufficient capacity.
        ByteBuffer buffer = ByteBuffer.allocate(20);

        // Create an instance of BinaryOutput with BIG_ENDIAN byte order.
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        // Serialize data (for example, a simple int field and a string field).
        out.writeFieldHeader(FieldType.INT8, 1).writeInt8((byte) 100);
        out.writeFieldHeader(FieldType.STRING_UTF8, 2).writeStringUTF8("Hello, LBS!");

        // Flip the buffer for reading.
        buffer.flip();

        // Display Encoded Data
        // [49, 100, -62, 11, 72, 101, 108, 108, 111, 44, 32, 76, 66, 83, 33, 0, 0, 0, 0, 0]
        System.out.println(Arrays.toString(buffer.array()));
        StringBuilder hexValue = new StringBuilder();
        for (byte b : buffer.array()) {
            hexValue.append(String.format("%02X ", b & 0xff));
        }
        // 31 64 C2 0B 48 65 6C 6C 6F 2C 20 4C 42 53 21 00 00 00 00 00
        System.out.println(hexValue);

        // Create an instance of BinaryInput.
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);

        // Read back the fields.
        FieldHeader header = in.readFieldHeader();
        byte value = in.readInt8();
        // FieldHeader{fieldType=INT8, fieldID=1} : 100
        System.out.println(header + " : " + value);

        header = in.readFieldHeader();
        String stringValue = in.readStringUTF8();
        // FieldHeader{fieldType=STRING_UTF8, fieldID=2} : Hello, LBS!
        System.out.println(header + " : " + stringValue);
    }
}
```

## API Overview

- **BinaryOutput:**  
  Methods to write field headers, primitives, arrays, and nested objects to a ByteBuffer.

- **BinaryInput:**  
  Methods to read field headers, decode variable-length integers, and reconstruct objects from a ByteBuffer.

- **Field Types:**  
  A set of predefined type codes for common data types, with support for extended types via a nested extension type enumeration.

---

## Field Header Encoding

Each field in the Lightweight Binary Serializer is prefixed by a one‐byte header that encodes both its data type and field identifier. For commonly used (normal) fields, the header is formatted as follows:

```
   +---------------------+---------------------+
   | Data Type (4 bits)  | Field ID (4 bits)   |
   +---------------------+---------------------+
```

- **Data Type (High 4 bits):**  
  This indicates the field’s type (for example, INT32, STRING_UTF8, BOOL_TRUE, etc.).
- **Field ID (Low 4 bits):**  
  If the field ID is less than 15, it is stored directly in these 4 bits.  
  If the field ID is 15 or greater, the low nibble is set to `0xF` (the reserved marker) and the remaining part of the field ID is stored in one or more extension bytes using a 7‑bit varint encoding (where each extension byte uses the MSB as a continuation flag).

### Example 1: Normal Field with Small Field ID

For a field of type INT32 with field ID 3, the header byte is:

```
   [ 0x05 | 0x03 ]   →   0x53
```

- Here, `0x05` (INT32) in the high nibble and `0x03` in the low nibble.

### Example 2: Normal Field with Extended Field ID

For a field of type INT32 with field ID 100:

1. The base header byte is:
   ```
   [ 0x05 | 0x0F ]   →   0x5F
   ```
2. Then, the extended field ID is stored as a varint:  
   Since 100 is greater than 14, we encode `100 - 15 = 85` in varint form. (For example, 85 fits in one byte: 0x55 because its MSB is 0.)

The full header becomes:

```
   Base header: 0x5F, Extension bytes: 0x55.
```

### Example 3: Array Field Header

For an array field (such as an array of INT32 values), the header uses a reserved type code for extended types. For example, if the array field is represented by the EXTENSION type (0x0E) and the low nibble stores the element type (say, INT32, which is 0x3):

1. The base header is:
   ```
   [ 0x0E | 0x03 ]   →   0xE3
   ```
2. Since arrays do not store the field ID in the base header, the field ID is stored in the extension bytes immediately following the header, using the same varint encoding (if the field ID is less than 15 it would be stored directly; otherwise a marker is used and additional bytes follow).

For example, for an array field with field ID 7, the extension bytes would simply be the varint-encoded 7.

---

## Supported Field Types

The following table lists the field types supported by LBS as defined in our `FieldType` enum:

| **FieldType** | **Value** | **Description**                                          |
| ------------- | --------- | -------------------------------------------------------- |
| UNUSED        | 0x00      | Unused                                                   |
| BOOL_TRUE     | 0x01      | Boolean true value                                       |
| BOOL_FALSE    | 0x02      | Boolean false value                                      |
| INT8          | 0x03      | 1-byte signed integer                                    |
| INT16         | 0x04      | 2-byte signed integer                                    |
| INT32         | 0x05      | 4-byte signed integer                                    |
| INT64         | 0x06      | 8-byte signed integer                                    |
| FLOAT16       | 0x07      | 2-byte float (half precision)                            |
| FLOAT32       | 0x08      | 4-byte float                                             |
| FLOAT64       | 0x09      | 8-byte float (double)                                    |
| VAR_INT32     | 0x0A      | Variable-length 32-bit integer (7-bit encoding)          |
| VAR_INT64     | 0x0B      | Variable-length 64-bit integer (7-bit encoding)          |
| STRING_UTF8   | 0x0C      | UTF‑8 string (with length prefix encoded as varint)      |
| INT8_ARRAY    | 0x0D      | Array of 1-byte integers (with length prefix)            |
| EXTENSION     | 0x0E      | Indicates that the full type is provided by an extension |
| RESERVED      | 0x0F      | Reserved / custom type                                   |

Extended types (for less commonly used types) include:

| **Extended Type**  | **Value** | **Description**                                   |
| ------------------ | --------- | ------------------------------------------------- |
| EXTENSION_RESERVED | 0xE0      | Reserved                                          |
| OBJECT             | 0xE1      | Complex nested object (serialized recursively)    |
| LIST               | 0xE2      | List of objects (serialized recursively)          |
| MAP                | 0xE3      | Key-Value map of objects (serialized recursively) |
| INT16_ARRAY        | 0xE4      | Array of 2-byte integers (with length prefix)     |
| INT32_ARRAY        | 0xE5      | Array of 4-byte integers (with length prefix)     |
| INT64_ARRAY        | 0xE6      | Array of 8-byte integers (with length prefix)     |
| FLOAT16_ARRAY      | 0xE7      | Array of 2-byte floats (half precision)           |
| FLOAT32_ARRAY      | 0xE8      | Array of 4-byte floats (with length prefix)       |
| FLOAT64_ARRAY      | 0xE9      | Array of 8-byte floats (with length prefix)       |
| VAR_INT32_ARRAY    | 0xEA      | Array of variable-length 32-bit integers          |
| VAR_INT64_ARRAY    | 0xEB      | Array of variable-length 64-bit integers          |
| STRING_UTF8_ARRAY  | 0xEC      | Array of UTF‑8 strings (with length prefix)       |
| RESERVE1           | 0xED      | Reserved                                          |
| RESERVE2           | 0xEE      | Reserved                                          |
| RESERVE3           | 0xEF      | Reserved                                          |

_Note: The values above are expressed in hexadecimal._

---

## Contact

For questions or feature requests, please open an issue.
