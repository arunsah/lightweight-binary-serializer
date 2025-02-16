package arunsah.lbs;

/**
 * Any DTO (or nested object) should implement this interface.
 */
public interface BinarySerializable {
    void serialize(BinaryOutput out);

    void deserialize(BinaryInput in);
}
