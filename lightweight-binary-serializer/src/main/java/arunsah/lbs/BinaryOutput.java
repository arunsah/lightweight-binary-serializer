package arunsah.lbs;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface BinaryOutput {

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
    BinaryOutput writeFieldHeader(FieldType fieldType, int fieldId);

    BinaryOutput writeFieldHeader(FieldHeader fieldHeader);

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
    BinaryOutput writeVarInt32(int value);

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
    BinaryOutput writeVarInt64(long value);

    BinaryOutput writeInt8(byte value);

    BinaryOutput writeInt16(short value);

    BinaryOutput writeInt32(int value);

    BinaryOutput writeInt64(long value);

    BinaryOutput writeFloat16(float value);

    BinaryOutput writeFloat32(float value);

    BinaryOutput writeFloat64(double value);

    BinaryOutput writeBoolean(int fieldId, boolean value);

    BinaryOutput writeStringUTF8(String value);

    BinaryOutput writeInt8Array(byte[] data);

    BinaryOutput writeInt16Array(short[] values);

    BinaryOutput writeInt32Array(int[] values);

    BinaryOutput writeInt64Array(long[] values);

    BinaryOutput writeFloat16Array(float[] values);

    BinaryOutput writeFloat32Array(float[] values);

    BinaryOutput writeFloat64Array(double[] values);

    BinaryOutput writeVarInt32Array(int[] values);

    BinaryOutput writeVarInt64Array(long[] values);

    BinaryOutput writeStringUTF8Array(String[] values);

    /**
     * Write an object (must implement BinarySerializable).
     *
     * @param obj
     * @param temporaryBufferSize
     * @return
     */
    BinaryOutput writeObject(BinarySerializable obj, int temporaryBufferSize);

    BinaryOutput writeObject(BinarySerializable obj);

    /**
     * Write a list of homogeneous elements.
     *
     * @param list
     * @param writer lambda writes each element.
     * @param <T>
     */
    <T> void writeList(List<T> list, BiConsumer<BinaryOutput, T> writer);

    <K, V> void writeMap(Map<K, V> map, BiConsumer<BinaryOutput, K> keyWriter,
                         BiConsumer<BinaryOutput, V> valueWriter);

    ByteBuffer getBuffer();

    int getBufferPosition();
}
