package arunsah.lbs.example;

import arunsah.lbs.BinaryInput;
import arunsah.lbs.BinaryInputImpl;
import arunsah.lbs.BinaryOutput;
import arunsah.lbs.BinaryOutputImpl;
import arunsah.lbs.FieldHeader;
import arunsah.lbs.FieldType;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SimplePrimitiveExample {

    /**
     * Test writing and reading of primitive values.
     * We test a series of fields (byte, short, int, long, float, double, boolean, string, byte array).
     * Each field is written into a ByteBuffer using BinaryOutput and then read back using BinaryInput.
     */
    private static void testPrimitiveFields() {
        System.out.println("testPrimitiveFields");
        ByteBuffer buffer = ByteBuffer.allocate(100);
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        // For each field, we assign a unique field ID.
        out.writeFieldHeader(FieldType.INT8, 1).writeInt8((byte) 42);
        out.writeFieldHeader(FieldType.INT16,2).writeInt16((short) 1234);
        out.writeFieldHeader(FieldType.INT32,3).writeInt32(123456);
        out.writeFieldHeader(FieldType.INT64,4).writeInt64(123456789L);
        out.writeFieldHeader(FieldType.FLOAT32,5).writeFloat32(3.14f);
        out.writeFieldHeader(FieldType.FLOAT64,6).writeFloat64(2.71828);
        out.writeBoolean(7, true);
        out.writeFieldHeader(FieldType.STRING_UTF8,8).writeStringUTF8("Hello, World!");
        byte[] byteArray = {10, 20, 30, 40, 50};
        out.writeFieldHeader(FieldType.INT8_ARRAY,9).writeInt8Array(byteArray);

        ExampleUtil.printBufferInfo(System.out, buffer);

        // Prepare buffer for reading.
        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);

        // Read back fields in order.
        FieldHeader h;
        // Field 1: byte
        h = in.readFieldHeader();
        byte byteVal = in.readInt8();
        if (byteVal != 42) {
            throw new AssertionError("Primitive field test (byte) failed. Expected 42, got " + byteVal);
        }
        // Field 2: short
        h = in.readFieldHeader();
        short shortVal = in.readInt16();
        if (shortVal != 1234) {
            throw new AssertionError("Primitive field test (short) failed. Expected 1234, got " + shortVal);
        }
        // Field 3: int
        h = in.readFieldHeader();
        int intVal = in.readInt32();
        if (intVal != 123456) {
            throw new AssertionError("Primitive field test (int) failed. Expected 123456, got " + intVal);
        }
        // Field 4: long
        h = in.readFieldHeader();
        long longVal = in.readInt64();
        if (longVal != 123456789L) {
            throw new AssertionError("Primitive field test (long) failed. Expected 123456789, got " + longVal);
        }
        // Field 5: float
        h = in.readFieldHeader();
        float floatVal = in.readFloat32();
        if (Math.abs(floatVal - 3.14f) > 0.0001) {
            throw new AssertionError("Primitive field test (float) failed. Expected 3.14, got " + floatVal);
        }
        // Field 6: double
        h = in.readFieldHeader();
        double doubleVal = in.readFloat64();
        if (Math.abs(doubleVal - 2.71828) > 0.00001) {
            throw new AssertionError("Primitive field test (double) failed. Expected 2.71828, got " + doubleVal);
        }
        // Field 7: boolean
        h = in.readFieldHeader();
        boolean boolVal;
        if (h.getFieldType() == FieldType.BOOL_TRUE) {
            boolVal = true;
        } else if (h.getFieldType() == FieldType.BOOL_FALSE) {
            boolVal = false;
        } else {
            throw new AssertionError("Primitive field test (boolean) failed. Unexpected type: " + h.getFieldType());
        }
        if (!boolVal) {
            throw new AssertionError("Primitive field test (boolean) failed. Expected true.");
        }

        // Field 8: string
        h = in.readFieldHeader();
        String strVal = in.readStringUTF8();
        if (!"Hello, World!".equals(strVal)) {
            throw new AssertionError("Primitive field test (string) failed. Expected 'Hello, World!', got '" + strVal + "'");
        }
        // Field 9: byte array
        h = in.readFieldHeader();
        byte[] readArray = in.readInt8Array();
        if (!Arrays.equals(byteArray, readArray)) {
            throw new AssertionError("Primitive field test (byte array) failed. Expected "
                    + Arrays.toString(byteArray) + ", got " + Arrays.toString(readArray));
        }
        System.out.println("testPrimitiveFields passed.");
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        testPrimitiveFields();
    }
}
