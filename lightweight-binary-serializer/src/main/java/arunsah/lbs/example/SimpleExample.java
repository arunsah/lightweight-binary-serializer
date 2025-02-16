package arunsah.lbs.example;

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