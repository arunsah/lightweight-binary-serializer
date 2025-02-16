package arunsah.lbs.example;

import arunsah.lbs.BinaryInput;
import arunsah.lbs.BinaryInputImpl;
import arunsah.lbs.BinaryOutput;
import arunsah.lbs.BinaryOutputImpl;
import arunsah.lbs.BinarySerializable;
import arunsah.lbs.FieldHeader;
import arunsah.lbs.FieldType;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComplexCompositeObjectsExample {

    public static class Person implements BinarySerializable {

        private static final int FIELD_ID_ID = 1;
        private static final int FIELD_NAME_ID = 2;
        private static final int FIELD_ACTIVE_ID = 3;
        private static final int FIELD_DOB_ID = 415;

        private int id;
        private String name;
        private boolean active;
        private Date dob;

        @Override
        public void serialize(BinaryOutput out) {
            out.writeFieldHeader(FieldType.INT8, FIELD_ID_ID).writeInt8((byte) id);
            out.writeFieldHeader(FieldType.STRING_UTF8, FIELD_NAME_ID).writeStringUTF8(name);
            out.writeBoolean(FIELD_ACTIVE_ID, active);
            out.writeFieldHeader(FieldType.INT64, FIELD_DOB_ID).writeInt64(dob.getTime());
        }

        @Override
        public void deserialize(BinaryInput in) {
            while (in.hasRemaining()) {
                FieldHeader header = in.readFieldHeader();
                if (header.getFieldID() == FIELD_ID_ID && header.getFieldType() == FieldType.INT8) {
                    id = in.readInt8();
                } else if (header.getFieldID() == FIELD_NAME_ID && header.getFieldType() == FieldType.STRING_UTF8) {
                    name = in.readStringUTF8();
                } else if (header.getFieldID() == FIELD_ACTIVE_ID) {
                    active = header.getFieldType() == FieldType.BOOL_TRUE;
                } else if (header.getFieldID() == FIELD_DOB_ID && header.getFieldType() == FieldType.INT64) {
                    dob = new Date(in.readInt64());
                } else {
                    System.out.println("Unknown Field Header: " + header);
                }
            }
        }


        public Person() {
        }

        public Person(int id, String name, boolean active, Date dob) {
            this.id = id;
            this.name = name;
            this.active = active;
            this.dob = dob;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Date getDob() {
            return dob;
        }

        public void setDob(Date dob) {
            this.dob = dob;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Person that = (Person) o;
            return id == that.id && active == that.active && Objects.equals(name, that.name) && Objects.equals(dob, that.dob);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, active, dob);
        }

        @Override
        public String toString() {
            return "Person{" + "id=" + id + ", name='" + name + '\'' + ", active=" + active + ", dob=" + dob + '}';
        }

    }

    private static void testPerson() {
        ByteBuffer buffer = ByteBuffer.allocate(30);
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        Person person = new Person(123, "harry", true, Date.from(Instant.now()));
        System.out.println(person);
        person.serialize(out);

        ExampleUtil.printBufferInfo(System.out, buffer);

        // Prepare buffer for reading
        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);

        Person deserializedPerson = new Person();
        deserializedPerson.deserialize(in);
        System.out.println("Deserialized Person: " + deserializedPerson);
        System.out.println("Is equal: " + (person.equals(deserializedPerson)));
    }


    public static class Group implements BinarySerializable {

        private static final int FIELD_GROUP_NAME_ID = 1;
        private static final int FIELD_MEMBERSHIP_LIST_ID = 2;
        private static final int FIELD_MEMBERS_MAP_ID = 3;

        private String groupName;
        public List<Person> members;
        public Map<String, Person> memberByRole;

        public Group() {
            this.members = new ArrayList<>();
            this.memberByRole = new HashMap<>();
        }

        public Group(String groupName, List<Person> members, Map<String, Person> memberByRole) {
            this.groupName = groupName;
            this.members = members;
            this.memberByRole = memberByRole;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<Person> getMembers() {
            return members;
        }

        public void setMembers(List<Person> members) {
            this.members = members;
        }

        public Map<String, Person> getMemberByRole() {
            return memberByRole;
        }

        public void setMemberByRole(Map<String, Person> memberByRole) {
            this.memberByRole = memberByRole;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Group group = (Group) o;
            return Objects.equals(groupName, group.groupName) && Objects.equals(members, group.members) && Objects.equals(memberByRole, group.memberByRole);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupName, members, memberByRole);
        }

        @Override
        public String toString() {
            return "Group{" + "groupName='" + groupName + '\'' + ", members=" + members + ", memberByRole=" + memberByRole + '}';
        }

        @Override
        public void serialize(BinaryOutput out) {
            out.writeFieldHeader(FieldType.STRING_UTF8, FIELD_GROUP_NAME_ID).writeStringUTF8(groupName);
            out.writeFieldHeader(FieldType.LIST, FIELD_MEMBERSHIP_LIST_ID).writeList(members, BinaryOutput::writeObject);
            out.writeFieldHeader(FieldType.MAP, FIELD_MEMBERS_MAP_ID).writeMap(memberByRole, BinaryOutput::writeStringUTF8, BinaryOutput::writeObject);
        }

        @Override
        public void deserialize(BinaryInput in) {
            while (in.hasRemaining()) {
                FieldHeader header = in.readFieldHeader();
                if (header.getFieldID() == FIELD_GROUP_NAME_ID && header.getFieldType() == FieldType.STRING_UTF8) {
                    groupName = in.readStringUTF8();
                } else if (header.getFieldID() == FIELD_MEMBERSHIP_LIST_ID && header.getFieldType() == FieldType.LIST) {
                    members = in.readList(binaryInput -> binaryInput.readObject(Person::new));
                } else if (header.getFieldID() == FIELD_MEMBERS_MAP_ID && header.getFieldType() == FieldType.MAP) {
                    memberByRole = in.readMap(BinaryInput::readStringUTF8, binaryInput -> binaryInput.readObject(Person::new));
                } else {
                    System.out.println("Unknown Field Header: " + header);
                }
            }
        }
    }

    private static void testGroup() {
        // Allocate a ByteBuffer with sufficient capacity.
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // Choose byte order (we use BIG_ENDIAN here, but you could choose LITTLE_ENDIAN if desired).
        BinaryOutput out = BinaryOutputImpl.bigEndianOutput(buffer);

        // Create a sample Group and serialize it.
        Person p1 = new Person(1, "tom", true, Date.from(Instant.now()));
        Person p2 = new Person(2, "harry", false, Date.from(Instant.now()));
        Person p3 = new Person(3, "jerry", true, Date.from(Instant.now()));
        Map<String, Person> roleToMember = new HashMap<>();
        roleToMember.put("admin", p1);
        roleToMember.put("user", p2);
        roleToMember.put("guest", p3);

        Group group = new Group("Group1", Arrays.asList(p1, p2), roleToMember);
        group.serialize(out);

        System.out.println(group);

        ExampleUtil.printBufferInfo(System.out, buffer);

        // Prepare buffer for reading.
        buffer.flip();
        BinaryInput in = BinaryInputImpl.bigEndianInput(buffer);

        // Deserialize into a new Group instance.
        Group deserializedGroup = new Group();
        deserializedGroup.deserialize(in);

        System.out.println("Deserialized Group: " + deserializedGroup);
        System.out.println("Is equal: " + deserializedGroup.equals(group));
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        testPerson();

        System.out.println("==================================================");
        testGroup();
    }
}
