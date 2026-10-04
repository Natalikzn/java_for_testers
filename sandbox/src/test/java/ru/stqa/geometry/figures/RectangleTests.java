package ru.stqa.geometry.figures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RectangleTests {
    @Test
    void cannotCreateRectangleWithNegativeSide() {
        try {
            new Rectangle(-5.0, 5.0);
            Assertions.fail();
        } catch (IllegalArgumentException exception) {
            // Ok
        }
    }

    @Test
    void testEquality() {
        var r1 = new Rectangle(5.0, 6.0);
        var r2 = new Rectangle(5.0, 6.0);
        Assertions.assertEquals(r1,r2);
    }

    @Test
    void testEquality2() {
        var r1 = new Rectangle(5.0, 6.0);
        var r2 = new Rectangle(6.0, 5.0);
        Assertions.assertEquals(r1,r2);
    }

}
