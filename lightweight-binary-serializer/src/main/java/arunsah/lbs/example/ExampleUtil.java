package arunsah.lbs.example;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExampleUtil {

    public static void printBufferInfo(PrintStream out, ByteBuffer buffer) {
        out.println(buffer);
        out.println(Arrays.toString(buffer.array()));
        StringBuilder hexValue = new StringBuilder();
        for (byte b : buffer.array()) {
            hexValue.append(String.format("%02X ", b & 0xff));
        }
        out.println(hexValue);
    }

}
