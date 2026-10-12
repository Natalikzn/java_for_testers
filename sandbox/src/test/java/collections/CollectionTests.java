package collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CollectionTests {

    @Test
    void arrayTests() {
        var array = new String[]{"a", "b", "c"};
        //var array = new String[3]; // создание массива с пустыми значениями = null
        Assertions.assertEquals(3, array.length);
        array[0] = "a";

        Assertions.assertEquals("a", array[0]);

        array[0] = "a1";

        Assertions.assertEquals("a1", array[0]);
    }

    @Test
    void listTests() {
        //var list = List.of("a", "b", "c");
        var list = new ArrayList<>(List.of("a", "b", "c"));
        Assertions.assertEquals(3, list.size());

        //list.add("a");
        //list.add("b");
        //list.add("c");

        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals("a", list.get(0));

        list.set(0, "a1");
        Assertions.assertEquals("a1", list.get(0));
    }
}
