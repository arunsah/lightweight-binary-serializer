package arunsah.lbs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface BinaryInput {

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
    FieldHeader readFieldHeader();

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
    int readVarInt32();

    long readVarInt64();

    byte readInt8();

    short readInt16();

    int readInt32();

    long readInt64();

    float readFloat16();

    float readFloat32();

    double readFloat64();

    boolean readBoolean(FieldHeader header);

    String readStringUTF8();

    byte[] readInt8Array();

    short[] readInt16Array();

    int[] readInt32Array();

    long[] readInt64Array();

    float[] readFloat16Array();

    float[] readFloat32Array();

    double[] readFloat64Array();

    int[] readVarInt32Array();

    long[] readVarInt64Array();

    String[] readStringUTF8Array();

    <T extends BinarySerializable> T readObject(BinarySerializableFactory<T> factory);

    /**
     * @param reader provided lambda reads one element.
     * @param <T>
     * @return
     */
    <T> List<T> readList(Function<BinaryInputImpl, T> reader);

    /**
     * Reads a map. For each entry, first the key is read then the value.
     *
     * @param keyReader
     * @param valueReader
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> Map<K, V> readMap(Function<BinaryInputImpl, K> keyReader, Function<BinaryInputImpl, V> valueReader);

    boolean hasRemaining();

    ByteBuffer getBuffer();
}
