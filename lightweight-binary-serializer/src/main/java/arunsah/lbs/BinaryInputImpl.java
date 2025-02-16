package arunsah.lbs;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BinaryInputImpl implements BinaryInput {

    private final ByteBuffer buffer;

    /**
     * Create instance of {@link BinaryInputImpl}
     *
     * @param buffer
     * @param byteOrder
     */
    public BinaryInputImpl(ByteBuffer buffer, ByteOrder byteOrder) {
        this.buffer = buffer;
        buffer.order(byteOrder);
    }

    /**
     * Static helper methods to create instance of {@link BinaryInputImpl} with {@link ByteOrder#BIG_ENDIAN}
     *
     * @param buffer
     * @return
     */
    public static BinaryInput bigEndianInput(ByteBuffer buffer) {
        return new BinaryInputImpl(buffer, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Static helper methods to create instance of {@link BinaryInputImpl} with {@link ByteOrder#LITTLE_ENDIAN}
     *
     * @param buffer
     * @return
     */
    public static BinaryInput littleEndianInput(ByteBuffer buffer) {
        return new BinaryInputImpl(buffer, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Reads a field header from the underlying ByteBuffer and returns a FieldHeader.
     * <p>
     * The header is stored in one byte. Its high 4 bits encode the field data type, and its low 4 bits are used
     * either to store the field ID (if the field ID is small) or, if equal to FieldID.EXTENSION (0xF), to indicate that
     * the full field ID is stored in subsequent extension bytes (encoded as a varint).
     * <p>
     * In the case where the high 4 bits equal FieldType.EXTENSION (our reserved value for extended types, such as arrays),
     * the low 4 bits are interpreted as the "extension type" (for example, the element type for an array).
     * Then, the field ID is read from the subsequent bytes using our 7‑bit varint encoding.
     *
     * @return a FieldHeader object containing:
     * - For normal fields: the data type and field ID.
     * - For extension fields: the extension type (stored in the low nibble) and the field ID.
     */
    @Override
    public FieldHeader readFieldHeader() {
        // Retrieve the next header byte from the buffer.
        byte header = buffer.get();

        FieldType fieldType = FieldType.getByFieldTypeValue(header);

        int fieldId = 0; // Initialize fieldId.

        // Check if the fieldType equals our reserved EXTENSION value (0xE).
        // This branch is used for extended fields such as arrays.
        if (fieldType.isExtensionType()) {

            // - The full field ID is not stored in the header; it is stored in the following extension bytes.
            //   We decode these bytes as a 7-bit varint.
            fieldId = readVarInt32();
            // Create and return a FieldHeader with the data type (EXTENSION),
            // the extension type in the low nibble, and the decoded field ID.
            return new FieldHeader(fieldType, fieldId);
        } else {
            // Normal field case:
            // The low 4 bits of the header contain the field ID if it fits in 4 bits.
            int fieldIdNibble = (header & 0x0F);
            // If the nibble value is less than the reserved EXTENSION marker (0xF), the field ID is stored directly.
            if (fieldIdNibble < Constant.FIELD_ID_EXTENSION) {
                fieldId = fieldIdNibble;
            } else {
                // Otherwise, if the low nibble equals 0xF, it means the field ID is too large to fit in 4 bits.
                // The base value is 15, and the remaining part is stored in extension bytes.
                fieldId = Constant.FIELD_ID_EXTENSION + readVarInt32();
            }
            // Return a normal FieldHeader with the extracted data type and field ID.
            return new FieldHeader(fieldType, fieldId);
        }
    }

    /**
     * Reads an integer encoded in a variable-length format using 7 bits per byte.
     * <p>
     * Each byte read from the buffer contributes 7 bits to the result:
     * - The lower 7 bits (bits 0-6) are used for the integer value.
     * - The most significant bit (MSB, bit 7) is the continuation flag:
     * If the MSB is 1, then another byte follows; if it is 0, this is the final byte.
     * <p>
     * The value is constructed in little-endian order (i.e. the first byte holds the least-significant 7 bits).
     *
     * @return The decoded integer.
     */
    @Override
    public int readVarInt32() {
        int value = 0;  // Accumulated value.
        int shift = 0;  // Number of bits already processed.
        byte b;
        do {
            // Read the next byte from the buffer.
            b = buffer.get();
            // Mask out the continuation flag (MSB) and shift the lower 7 bits into the correct position.
            value |= (b & 0x7F) << shift;
            // Increment the shift by 7 for the next group of bits.
            shift += 7;
            if (shift > 40) {
                throw new IllegalArgumentException("7-bit encoded int too large.");
            }
        } while ((b & 0x80) != 0); // Continue if MSB is 1 (i.e., more bytes follow).
        return value;
    }

    @Override
    public long readVarInt64() {
        long value = 0;  // Accumulated value.
        int shift = 0;  // Number of bits already processed.
        byte b;
        do {
            // Read the next byte from the buffer.
            b = buffer.get();
            // Mask out the continuation flag (MSB) and shift the lower 7 bits into the correct position.
            value |= (long) (b & 0x7F) << shift;
            // Increment the shift by 7 for the next group of bits.
            shift += 7;
            if (shift > 72) {
                throw new IllegalArgumentException("7-bit encoded long too large.");
            }
        } while ((b & 0x80) != 0); // Continue if MSB is 1 (i.e., more bytes follow).
        return value;
    }


    @Override
    public byte readInt8() {
        return buffer.get();
    }

    @Override
    public short readInt16() {
        return buffer.getShort();
    }

    @Override
    public int readInt32() {
        return buffer.getInt();
    }

    @Override
    public long readInt64() {
        return buffer.getLong();
    }

    @Override
    public float readFloat16() {
        return buffer.getFloat();
    }

    @Override
    public float readFloat32() {
        return buffer.getFloat();
    }

    @Override
    public double readFloat64() {
        return buffer.getDouble();
    }

    @Override
    public boolean readBoolean(FieldHeader header) {
        return header.fieldType == FieldType.BOOL_TRUE;
    }

    @Override
    public String readStringUTF8() {
        // Read string length as a varint
        int length = readVarInt32();
        byte[] data = new byte[length];
        buffer.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] readInt8Array() {
        int length = readVarInt32();
        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    @Override
    public short[] readInt16Array() {
        int length = readVarInt32();
        short[] values = new short[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getShort();
        }
        return values;
    }

    @Override
    public int[] readInt32Array() {
        int length = readVarInt32();
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getInt();
        }
        return values;
    }

    @Override
    public long[] readInt64Array() {
        int length = readVarInt32();
        long[] values = new long[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getLong();
        }
        return values;
    }

    @Override
    public float[] readFloat16Array() {
        int length = readVarInt32();
        float[] values = new float[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }

    @Override
    public float[] readFloat32Array() {
        int length = readVarInt32();
        float[] values = new float[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getFloat();
        }
        return values;
    }

    @Override
    public double[] readFloat64Array() {
        int length = readVarInt32();
        double[] values = new double[length];
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getDouble();
        }
        return values;
    }

    @Override
    public int[] readVarInt32Array() {
        int length = readVarInt32();
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            int value = 0;
            int shift = 0;
            byte b;
            do {
                b = buffer.get(); // get next byte
                value |= (b & 0x7F) << shift;
                shift += 7;
            } while ((b & 0x80) != 0);
            values[i] = value;
        }
        return values;
    }

    @Override
    public long[] readVarInt64Array() {
        int length = readVarInt32();
        long[] values = new long[length];
        for (int i = 0; i < length; i++) {
            long value = 0;
            int shift = 0;
            byte b;
            do {
                b = buffer.get(); // get next byte
                value |= (long) (b & 0x7F) << shift;
                shift += 7;
            } while ((b & 0x80) != 0);
            values[i] = value;
        }
        return values;
    }

    @Override
    public String[] readStringUTF8Array() {
        int size = readVarInt32();
        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            // Read string length as a varint
            int length = readVarInt32();
            byte[] data = new byte[length];
            buffer.get(data);
            values[i] = new String(data, StandardCharsets.UTF_8);
        }
        return values;
    }

    @Override
    public <T extends BinarySerializable> T readObject(BinarySerializableFactory<T> factory) {
        int length = readVarInt32();
        int oldLimit = buffer.limit();
        int newLimit = buffer.position() + length;

        buffer.limit(newLimit);
        T obj = factory.create();
        obj.deserialize(this);
        buffer.limit(oldLimit);
        return obj;
    }

    /**
     * @param reader provided lambda reads one element.
     * @param <T>
     * @return
     */
    @Override
    public <T> List<T> readList(Function<BinaryInputImpl, T> reader) {
        int length = readVarInt32();
        List<T> list = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            list.add(reader.apply(this));
        }
        return list;
    }

    /**
     * Reads a map. For each entry, first the key is read then the value.
     *
     * @param keyReader
     * @param valueReader
     * @param <K>
     * @param <V>
     * @return
     */
    @Override
    public <K, V> Map<K, V> readMap(Function<BinaryInputImpl, K> keyReader, Function<BinaryInputImpl, V> valueReader) {
        int length = readVarInt32();
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < length; i++) {
            K key = keyReader.apply(this);
            V value = valueReader.apply(this);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public boolean hasRemaining() {
        return buffer.hasRemaining();
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
