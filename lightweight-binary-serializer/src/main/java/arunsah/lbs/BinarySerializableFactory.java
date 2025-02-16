package arunsah.lbs;

/**
 * For the nested object case we also use a simple factory interface
 *
 * @param <T>
 */
public interface BinarySerializableFactory<T extends BinarySerializable> {
    T create();
}