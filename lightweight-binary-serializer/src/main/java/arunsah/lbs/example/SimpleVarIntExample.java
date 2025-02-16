package arunsah.lbs.example;

import arunsah.lbs.BinaryInput;
import arunsah.lbs.BinaryInputImpl;
import arunsah.lbs.BinaryOutput;
import arunsah.lbs.BinaryOutputImpl;
import arunsah.lbs.FieldHeader;
import arunsah.lbs.FieldType;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class SimpleVarIntExample {

    public static final int[] INT32_ARRAY_VALUES = {0, 1, 127, 128, 300, 16384, 1_000_000, -0, -1, -127, -128, -300, -16384, -1_000_000};
    public static final long[] INT64_ARRAY_VALUES = {0, 1, 127, 128, 300, 16384, 1_000_000, 1000000000000000001L, -1000000000000000001L, 1000000000000000000L, 1234567890123456789L, -1234567890123456789L, 9223372036854775807L, -9223372036854775808L};

    /**
     * Test the variable-length integer encoding and decoding.
     * We write several integer values using writeVarInt32() and
     * then read them back using readVarInt32().
     */
    private static void testVarIntBigEndian() {

        for (int value : INT32_ARRAY_VALUES) {
            // Allocate a buffer with enough space.
            ByteBuffer buffer = ByteBuffer.allocate(8);
            BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);
            out.writeVarInt32(value);

            System.out.println("Value = " + value);
            ExampleUtil.printBufferInfo(System.out, buffer);

            // Prepare buffer for reading.
            buffer.flip();
            BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
            int decoded = in.readVarInt32();
            if (decoded != value) {
                throw new AssertionError("VarInt test failed for value " + value + ". Decoded: " + decoded);
            }
        }
        System.out.println("testVarIntBigEndian passed.");
    }

    /**
     * Test the variable-length integer encoding and decoding.
     * We write several integer values using writeVarInt32() and
     * then read them back using readVarInt32().
     */
    private static void testVarIntLittleEndian() {

        for (int value : INT32_ARRAY_VALUES) {
            // Allocate a buffer with enough space.
            ByteBuffer buffer = ByteBuffer.allocate(8);
            BinaryOutput out = BinaryOutputImpl.littleEndianOutput(buffer);
            out.writeVarInt32(value);

            System.out.println("Value = " + value);
            ExampleUtil.printBufferInfo(System.out, buffer);

            // Prepare buffer for reading.
            buffer.flip();
            BinaryInput in = BinaryInputImpl.littleEndianInput(buffer);
            int decoded = in.readVarInt32();
            if (decoded != value) {
                throw new AssertionError("VarInt test failed for value " + value + ". Decoded: " + decoded);
            }
        }
        System.out.println("testVarIntLittleEndian passed.");
    }

    /**
     * Test the variable-length integer encoding and decoding.
     * We write several integer values using writeVarInt64() and
     * then read them back using readVarInt64().
     */
    private static void testVarInt64BigEndian() {

        for (long value : INT64_ARRAY_VALUES) {
            // Allocate a buffer with enough space.
            ByteBuffer buffer = ByteBuffer.allocate(20);
            BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);
            out.writeVarInt64(value);

            System.out.println("Value = " + value);
            ExampleUtil.printBufferInfo(System.out, buffer);

            // Prepare buffer for reading.
            buffer.flip();
            BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
            long decoded = in.readVarInt64();
            if (decoded != value) {
                throw new AssertionError("VarInt test failed for value " + value + ". Decoded: " + decoded);
            }
        }
        System.out.println("testVarInt64BigEndian passed.");
    }

    /**
     * Test the variable-length integer encoding and decoding.
     * We write several integer values using writeVarInt64() and
     * then read them back using readVarInt64().
     */
    private static void testVarInt64LittleEndian() {

        for (long value : INT64_ARRAY_VALUES) {
            // Allocate a buffer with enough space.
            ByteBuffer buffer = ByteBuffer.allocate(16);
            BinaryOutput out = BinaryOutputImpl.littleEndianOutput(buffer);
            out.writeVarInt64(value);

            System.out.println("Value = " + value);
            ExampleUtil.printBufferInfo(System.out, buffer);

            // Prepare buffer for reading.
            buffer.flip();
            BinaryInput in = BinaryInputImpl.littleEndianInput(buffer);
            long decoded = in.readVarInt64();
            if (decoded != value) {
                throw new AssertionError("VarInt test failed for value " + value + ". Decoded: " + decoded);
            }
        }
        System.out.println("testVarInt64LittleEndian passed.");
    }

    /**
     * Test writing and reading of an int array field.
     * We write an int array field using writeInt32Array() and then read it back using readInt32Array().
     */
    private static void testIntArrayField() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        int fieldId = 20; // Use a field ID that may require extension if desired.
        int[] values = {5, 10, 15, 20, 25, 30};
        out.writeFieldHeader(FieldType.INT32_ARRAY, fieldId).writeInt32Array(values);

        ExampleUtil.printBufferInfo(System.out, buffer);

        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
        FieldHeader h = in.readFieldHeader();
        // In our array field, the base header’s low nibble encodes the extension type.
        // We expect the extension type to be INT32.
        if (h.getFieldType() != FieldType.INT32_ARRAY) {
            throw new AssertionError(String.format("Int array field test failed. Expected fieldType %s, got %s OR " +
                            "Expected extension type %s, got %s", FieldType.EXTENSION, h.getFieldType(), FieldType.INT32,
                    h.getFieldType()));
        }
        if (h.getFieldID() != fieldId) {
            throw new AssertionError(String.format("Int array field test failed. Expected fieldId %d, got %d", fieldId, h.getFieldID()));
        }
        int[] readValues = in.readInt32Array();
        if (!Arrays.equals(values, readValues)) {
            throw new AssertionError("Int array field test failed. Expected " + Arrays.toString(values) + ", got " + Arrays.toString(readValues));
        }
        System.out.println("testIntArrayField passed.");
    }

    private static void testVarIntArrayField() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        int fieldId = 20; // Use a field ID that may require extension if desired.
        long[] values = {5, 10, 15, 20, 25, 30};
        out.writeFieldHeader(FieldType.VAR_INT64_ARRAY, fieldId).writeVarInt64Array(values);

        ExampleUtil.printBufferInfo(System.out, buffer);

        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);
        FieldHeader h = in.readFieldHeader();
        // In our array field, the base header’s low nibble encodes the extension type.
        // We expect the extension type to be INT32.
        if (h.getFieldType() != FieldType.VAR_INT64_ARRAY) {
            throw new AssertionError(String.format("Int array field test failed. Expected fieldType %s, got %s OR " +
                            "Expected extension type %s, got %s", FieldType.EXTENSION, h.getFieldType(), FieldType.INT32,
                    h.getFieldType()));
        }
        if (h.getFieldID() != fieldId) {
            throw new AssertionError(String.format("Int array field test failed. Expected fieldId %d, got %d", fieldId, h.getFieldID()));
        }
        long[] readValues = in.readVarInt64Array();
        if (!Arrays.equals(values, readValues)) {
            throw new AssertionError("Int array field test failed. Expected " + Arrays.toString(values) + ", got " + Arrays.toString(readValues));
        }
        System.out.println("testVarIntArrayField passed.");
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        testVarIntBigEndian();

        System.out.println("==================================================");
        testVarIntLittleEndian();

        System.out.println("==================================================");
        testVarInt64BigEndian();

        System.out.println("==================================================");
        testVarInt64LittleEndian();

        System.out.println("==================================================");
        testIntArrayField();

        System.out.println("==================================================");
        testVarIntArrayField();
    }

}
