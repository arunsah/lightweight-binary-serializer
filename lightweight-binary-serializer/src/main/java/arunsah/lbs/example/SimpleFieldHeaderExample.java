package arunsah.lbs.example;

import arunsah.lbs.BinaryInput;
import arunsah.lbs.BinaryInputImpl;
import arunsah.lbs.BinaryOutput;
import arunsah.lbs.BinaryOutputImpl;
import arunsah.lbs.FieldHeader;
import arunsah.lbs.FieldType;

import java.nio.ByteBuffer;

public class SimpleFieldHeaderExample {

    public static void main(String[] args) {
        System.out.println("==================================================");
        testFieldHeaderNormal();

        System.out.println("==================================================");
        testFieldHeaderExtendedInt32Array();
    }

    /**
     * Test normal field header encoding/decoding.
     * We test two cases:
     * 1. A field with a small Field ID (less than 15).
     * 2. A field with a larger Field ID (>= 15), which uses extension bytes.
     */
    private static void testFieldHeaderNormal() {
        // Case 1: Small Field ID (e.g., fieldId = 3, type = INT32).
        System.out.println("Case 1: Small Field ID (e.g., fieldId = 3, type = INT32).");
        {
            ByteBuffer buffer = ByteBuffer.allocate(10);
            BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);
            int fieldId = 3;
            FieldType dataType = FieldType.INT32;
            out.writeFieldHeader(dataType, fieldId);

            ExampleUtil.printBufferInfo(System.out, buffer);

            // Prepare buffer for reading.
            buffer.flip();
            BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
            FieldHeader header = in.readFieldHeader();
            if (header.getFieldID() != fieldId || header.getFieldType() != dataType) {
                throw new AssertionError("Normal field header test (small id) failed. Expected fieldId="
                        + fieldId + ", type=" + dataType + "; Got: " + header);
            }
        }

        // Case 2: Extended Field ID (e.g., fieldId = 100, type = INT32).
        System.out.println("Case 2: Extended Field ID (e.g., fieldId = 256, type = INT32).");
        {
            ByteBuffer buffer = ByteBuffer.allocate(20);
            BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);
            int fieldId = 256; // greater than 14
            FieldType dataType = FieldType.INT32;
            out.writeFieldHeader(dataType, fieldId);

            ExampleUtil.printBufferInfo(System.out, buffer);

            buffer.flip();
            BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
            FieldHeader header = in.readFieldHeader();
            if (header.getFieldID() != fieldId || header.getFieldType() != dataType) {
                throw new AssertionError("Normal field header test (extended id) failed. Expected fieldId="
                        + fieldId + ", type=" + dataType + "; Got: " + header);
            }
        }
        System.out.println("testFieldHeaderNormal passed.");
    }

    /**
     * Test the header encoding/decoding for array fields.
     */
    private static void testFieldHeaderExtendedInt32Array() {
        // Example: fieldId = 5, extension type = INT32 (for int arrays).
        ByteBuffer buffer = ByteBuffer.allocate(20);
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);
        int fieldId = 5;
        FieldType dataType = FieldType.INT32_ARRAY;

        // Write array field header.
        out.writeFieldHeader(dataType, fieldId);

        ExampleUtil.printBufferInfo(System.out, buffer);

        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
        FieldHeader header = in.readFieldHeader();
        // For array fields, our design returns a FieldHeader with fieldId from extension and type equals the extension type.
        if (header.getFieldID() != fieldId || header.getFieldType() != dataType) {
            throw new AssertionError("Array field header test failed. Expected fieldId="
                    + fieldId + ", data type=" + dataType + "; Got: " + header);
        }
        System.out.println("testFieldHeaderExtendedInt32Array passed.");
    }

}
