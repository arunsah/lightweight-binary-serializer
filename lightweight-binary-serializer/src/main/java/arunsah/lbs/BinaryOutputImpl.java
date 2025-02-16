package arunsah.lbs;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class BinaryOutputImpl implements BinaryOutput {

    private final ByteBuffer buffer;

    /**
     * Create instance of {@link BinaryOutputImpl}
     *
     * @param buffer
     * @param byteOrder
     */
    public BinaryOutputImpl(ByteBuffer buffer, ByteOrder byteOrder) {
        this.buffer = buffer;
        buffer.order(byteOrder);
    }

    /**
     * Static helper methods to create instance of {@link BinaryOutputImpl} with {@link ByteOrder#BIG_ENDIAN}
     *
     * @param buffer
     * @return
     */
    public static BinaryOutput bigEndianOutput(ByteBuffer buffer) {
        return new BinaryOutputImpl(buffer, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Static helper methods to create instance of {@link BinaryOutputImpl} with {@link ByteOrder#LITTLE_ENDIAN}
     *
     * @param buffer
     * @return
     */
    public static BinaryOutput littleEndianOutput(ByteBuffer buffer) {
        return new BinaryOutputImpl(buffer, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Writes a field header to the underlying ByteBuffer.
     * <p>
     * The header format depends on whether the field is "normal" or an "extension" field:
     * <p>
     * For normal fields:
     * - If fieldId is less than 15, the header byte is composed as follows:
     * [ dataType (high 4 bits) | fieldId (low 4 bits) ]
     * - If fieldId is 15 or greater, the header's low nibble is set to 0xF as a marker,
     * and the extra field ID value (fieldId - 15) is written in subsequent extension bytes
     * using a 7-bit varint encoding.
     * <p>
     * For extension fields (e.g., arrays), the dataType is expected to have its high nibble equal
     * to FieldType.EXTENSION. In that case, the base header byte is written as-is (with the low nibble
     * representing the extension type, such as the element type for an array), and the fieldId is written
     * afterward as a varint.
     *
     * @param fieldType The data type byte. For normal fields, it encodes the type in its high 4 bits.
     *                  For extension fields (like arrays), it should have its high 4 bits equal to FieldType.EXTENSION.
     * @param fieldId   The numeric field identifier (avoid using zero field id; consider it as reserved)
     * @return This BinaryOutput instance (to allow chaining).
     */
    @Override
    public BinaryOutput writeFieldHeader(FieldType fieldType, int fieldId) {
        // Extract the high nibble from the dataType.
        // This represents the field's data type (or extension indicator) in our header.
        if (fieldType.isExtensionType()) {
            // Write the provided dataType byte as-is. Its low nibble contains the extension type
            buffer.put(fieldType.getValue());
            // Write the full fieldId using a variable-length integer (varint).
            // (For extension fields, the entire fieldId is stored in the extension bytes.)
            writeVarInt32(fieldId);
        } else { // Normal field
            // For normal fields, determine whether the fieldId can be stored directly.
            if (fieldId < Constant.FIELD_ID_EXTENSION) {
                // If fieldId is less than the reserved marker (0xF), encode it directly.
                // Compose the header: shift the dataType into the high nibble, and OR with the fieldId in the low nibble.
                byte headerDataTypeWithFieldId = (byte) ((fieldType.getValue() << 4) | (fieldId & 0x0F));
                buffer.put(headerDataTypeWithFieldId);
            } else {
                // If the fieldId is too large to fit in 4 bits:
                // Write a header with the low nibble set to the marker 0xF (i.e., Constant.FIELD_ID_EXTENSION).
                byte headerDataTypeWithFieldIdExtension = (byte) ((fieldType.getValue() << 4) | Constant.FIELD_ID_EXTENSION);
                buffer.put(headerDataTypeWithFieldIdExtension);
                // Then write the extra part of the fieldId as a varint.
                // Since the first 15 values (0-14) are encoded directly, we subtract 15.
                writeVarInt32(fieldId - Constant.FIELD_ID_EXTENSION);
            }
        }
        return this;
    }

    @Override
    public BinaryOutput writeFieldHeader(FieldHeader fieldHeader) {
        return writeFieldHeader(fieldHeader.getFieldType(), fieldHeader.getFieldID());
    }

    /**
     * Writes an integer using 7-bit variable-length encoding.
     * <p>
     * The algorithm works as follows:
     * - While the value does not fit in 7 bits (i.e., there are bits set outside the lower 7),
     * write out the lower 7 bits ORed with 0x80 (to set the continuation flag).
     * - Then shift the value right by 7 bits.
     * - When the remaining value fits in 7 bits, write it out without the continuation flag.
     * <p>
     * This method is used both for encoding extended Field IDs and for other variable-length integers.
     *
     * @param value The integer value to encode.
     * @return This BinaryOutput instance (to allow chaining).
     */
    @Override
    public BinaryOutput writeVarInt32(int value) {
        // Loop until all bits have been processed.
        while ((value & ~0x7F) != 0) {
            // (value & ~0x7F) != 0 checks if there are any bits beyond the lower 7.
            // Write the lower 7 bits of value and set the MSB (continuation flag).
            byte part = (byte) ((value & 0x7F) | 0x80);
            buffer.put(part);
            // Logical right shift by 7 bits to process the next 7 bits.
            value >>>= 7;
        }
        // Write the final byte (MSB = 0, meaning this is the last byte).
        buffer.put((byte) (value & 0x7F));
        return this;
    }

    /**
     * Writes an long using 7-bit variable-length encoding.
     * <p>
     * The algorithm works as follows:
     * - While the value does not fit in 7 bits (i.e., there are bits set outside the lower 7),
     * write out the lower 7 bits ORed with 0x80 (to set the continuation flag).
     * - Then shift the value right by 7 bits.
     * - When the remaining value fits in 7 bits, write it out without the continuation flag.
     * <p>
     * This method is used both for encoding extended Field IDs and for other variable-length integers.
     *
     * @param value The integer value to encode.
     * @return This BinaryOutput instance (to allow chaining).
     */
    @Override
    public BinaryOutput writeVarInt64(long value) {
        // Loop until all bits have been processed.
        while ((value & ~0x7F) != 0) {
            // (value & ~0x7F) != 0 checks if there are any bits beyond the lower 7.
            // Write the lower 7 bits of value and set the MSB (continuation flag).
            byte part = (byte) ((value & 0x7F) | 0x80);
            buffer.put(part);
            // Logical right shift by 7 bits to process the next 7 bits.
            value >>>= 7;
        }
        // Write the final byte (MSB = 0, meaning this is the last byte).
        buffer.put((byte) (value & 0x7F));
        return this;
    }


    @Override
    public BinaryOutput writeInt8(byte value) {
        buffer.put(value);
        return this;
    }


    @Override
    public BinaryOutput writeInt16(short value) {
        buffer.putShort(value);
        return this;
    }

    @Override
    public BinaryOutput writeInt32(int value) {
        buffer.putInt(value);
        return this;
    }

    @Override
    public BinaryOutput writeInt64(long value) {
        buffer.putLong(value);
        return this;
    }

    @Override
    public BinaryOutput writeFloat16(float value) {
        buffer.putFloat(value);
        return this;
    }

    @Override
    public BinaryOutput writeFloat32(float value) {
        buffer.putFloat(value);
        return this;
    }

    @Override
    public BinaryOutput writeFloat64(double value) {
        buffer.putDouble(value);
        return this;
    }

    @Override
    public BinaryOutput writeBoolean(int fieldId, boolean value) {
        writeFieldHeader(value ? FieldType.BOOL_TRUE : FieldType.BOOL_FALSE, fieldId);
        return this;
    }


    @Override
    public BinaryOutput writeStringUTF8(String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt32(data.length);
        buffer.put(data);
        return this;
    }

    @Override
    public BinaryOutput writeInt8Array(byte[] data) {
        writeVarInt32(data.length);
        buffer.put(data);
        return this;
    }


    @Override
    public BinaryOutput writeInt16Array(short[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putShort(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeInt32Array(int[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putInt(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeInt64Array(long[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putLong(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeFloat16Array(float[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putFloat(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeFloat32Array(float[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putFloat(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeFloat64Array(double[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            buffer.putDouble(values[i]);
        }
        return this;
    }

    @Override
    public BinaryOutput writeVarInt32Array(int[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            while ((value & ~0x7F) != 0) {
                byte part = (byte) ((value & 0x7F) | 0x80);
                buffer.put(part);
                value >>>= 7;
            }
            buffer.put((byte) (value & 0x7F));
        }
        return this;
    }

    @Override
    public BinaryOutput writeVarInt64Array(long[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            long value = values[i];
            while ((value & ~0x7F) != 0) {
                byte part = (byte) ((value & 0x7F) | 0x80);
                buffer.put(part);
                value >>>= 7;
            }
            buffer.put((byte) (value & 0x7F));
        }
        return this;
    }


    @Override
    public BinaryOutput writeStringUTF8Array(String[] values) {
        writeVarInt32(values.length);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            byte[] data = value.getBytes(StandardCharsets.UTF_8);
            writeVarInt32(data.length);
            buffer.put(data);
        }
        return this;
    }

    /**
     * Write an object (must implement BinarySerializable).
     *
     * @param obj
     * @param temporaryBufferSize
     * @return
     */
    @Override
    public BinaryOutput writeObject(BinarySerializable obj, int temporaryBufferSize) {
        // Serialize the nested object into a temporary buffer
        ByteBuffer objectBuffer = ByteBuffer.allocate(temporaryBufferSize);
        BinaryOutput binaryOutput = new BinaryOutputImpl(objectBuffer, buffer.order());
        obj.serialize(binaryOutput);
        objectBuffer.flip();

        int length = objectBuffer.remaining();
        writeVarInt32(length);

        // add to buffer
        buffer.put(objectBuffer);
        return this;
    }

    @Override
    public BinaryOutput writeObject(BinarySerializable obj) {
        return writeObject(obj, Constant.DEFAULT_INTERMEDIATE_BUFFER_SIZE);
    }

    /**
     * Write a list of homogeneous elements.
     *
     * @param list
     * @param writer lambda writes each element.
     * @param <T>
     */
    @Override
    public <T> void writeList(List<T> list, BiConsumer<BinaryOutput, T> writer) {
        writeVarInt32(list.size());
        for (int i = 0; i < list.size(); i++) {
            writer.accept(this, list.get(i));
        }
    }

    @Override
    public <K, V> void writeMap(Map<K, V> map, BiConsumer<BinaryOutput, K> keyWriter, BiConsumer<BinaryOutput, V> valueWriter) {
        writeVarInt32(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(this, entry.getKey());
            valueWriter.accept(this, entry.getValue());
        }
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public int getBufferPosition() {
        return buffer.position();
    }

}
