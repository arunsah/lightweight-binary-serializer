package arunsah.lbs;

public class FieldHeader {
    public final FieldType fieldType;
    public final int fieldID;

    public FieldHeader(FieldType fieldType, int fieldID) {
        this.fieldType = fieldType;
        this.fieldID = fieldID;
    }

    @Override
    public String toString() {
        return "FieldHeader{fieldType=" + fieldType + ", fieldID=" + fieldID + "}";
    }

    public int getFieldID() {
        return fieldID;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

}
