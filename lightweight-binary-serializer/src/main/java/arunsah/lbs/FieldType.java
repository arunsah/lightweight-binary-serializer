package arunsah.lbs;

import java.util.HashMap;
import java.util.Map;

public enum FieldType {
    UNUSED(0x00), // unused
    BOOL_TRUE(0x01), // boolean true value
    BOOL_FALSE(0x02), // boolean false value
    INT8(0x03), // 1-byte signed integer
    INT16(0x04), // 2-byte signed integer
    INT32(0x05), // 4-byte signed integer
    INT64(0x06), // 4-byte signed integer
    FLOAT16(0x07), // 2-byte float (half)
    FLOAT32(0x08), // 4-byte float
    FLOAT64(0x09), // 8-byte float (double)
    VAR_INT32(0x0A), // variable integer (7-bit encoding, MSB is for continuation)
    VAR_INT64(0x0B), // variable long (7-bit encoding, MSB is for continuation)
    STRING_UTF8(0x0C), // UTF‑8 string (with length prefix; 7-bit integer encoding)
    INT8_ARRAY(0x0D), // 1-byte array (with length prefix; 7-bit integer encoding)
    EXTENSION(0x0E), // Represented type extension flag
    RESERVED(0x0F), // reserved

    // Extended types – we reserve the high range (0xE0 and above) for less commonly used types.
    EXTENSION_RESERVED(0xE0), // reserved
    OBJECT(0xE1), // Complex nested object (serialized recursively)
    LIST(0xE2), // List of object (serialized recursively)
    MAP(0xE3), // Key-Value Map of object (serialized recursively)
    INT16_ARRAY(0xE4), // 2-byte signed integer array (with length prefix; 7-bit integer encoding)
    INT32_ARRAY(0xE5), // 4-byte signed integer array (with length prefix; 7-bit integer encoding)
    INT64_ARRAY(0xE6), // 4-byte signed integer array (with length prefix; 7-bit integer encoding)
    FLOAT16_ARRAY(0xE7), // 2-byte float (half) array (with length prefix; 7-bit integer encoding)
    FLOAT32_ARRAY(0xE8), // 4-byte float array (with length prefix; 7-bit integer encoding)
    FLOAT64_ARRAY(0xE9), // 8-byte float (double) array (with length prefix; 7-bit integer encoding)
    VAR_INT32_ARRAY(0xEA), // variable integer (7-bit encoding, MSB is for continuation) array
    VAR_INT64_ARRAY(0xEB), // variable long (7-bit encoding, MSB is for continuation) array
    STRING_UTF8_ARRAY(0xEC), // UTF‑8 string array (with length prefix; 7-bit integer encoding)
    RESERVE1(0xED), // reserve
    RESERVE2(0xEE), // reserve
    RESERVE3(0xEF); // reserve

    private final byte value;
    private static final Map<Byte, FieldType> fieldTypeByValueMap = new HashMap<>();

    static {
        for (FieldType fieldType : values()) {
            fieldTypeByValueMap.put(fieldType.getValue(), fieldType);
        }
    }

    FieldType(int value) {
        this.value = (byte) value;
    }

    /**
     * Returns the 8-bit value associated with this type.
     */
    public byte getValue() {
        return value;
    }

    public static FieldType getByFieldTypeValue(byte header) {
        if (isExtensionType(header)) {
            return fieldTypeByValueMap.get(header);
        } else {
            return fieldTypeByValueMap.get((byte) ((header & 0xF0) >> 4));
        }
    }

    public boolean isBooleanType() {
        return this == BOOL_FALSE || this == BOOL_TRUE;
    }

    public boolean isExtensionType() {
        return ((this.getValue() & 0xF0) >> 4) == FieldType.EXTENSION.getValue();
    }

    public static boolean isExtensionType(byte value) {
        return ((value & 0xF0) >> 4) == FieldType.EXTENSION.getValue();
    }

}
