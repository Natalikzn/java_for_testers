package ru.stqa.geometry.figures;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TriangleTests {

    @Test
    void canCalculateArea() {
        var t = new Triangle(3.0, 4.0, 5.0);

        double result = t.triangleArea();
        Assertions.assertEquals(6.0, result);
    }

    @Test
    void canCalculatePerimeter() {
        Assertions.assertEquals(12.0, new Triangle(3.0, 4.0, 5.0).trianglePerimeter());
    }

    @Test
    void cannotCreateTriangleWithNegativeSide() {
        try {
            new Triangle(-3.0, 4.0, 5.0);
            Assertions.fail();
        } catch (IllegalArgumentException exception) {
            //Ок
        }
    }


    @Test
    void triangleInequality() {
        try {
            new Triangle(1.0, 2.0, 3.0);
            Assertions.fail();
        } catch (IllegalArgumentException exception) {
            //Ок
        }
    }

    @Test
    void testEquality() {
        var t1 = new Triangle(3.0, 4.0, 5.0);
        var t2 = new Triangle(3.0, 4.0, 5.0);
        Assertions.assertEquals(t1,t2);
    }
    @Test
    void testEquality2() {
        var t1 = new Triangle(4.0, 5.0, 3.0);
        var t2 = new Triangle(4.0, 5.0, 3.0);
        Assertions.assertEquals(t1,t2);
    }
    @Test
    void testEquality3() {
        var t1 = new Triangle(5.0, 3.0, 4.0);
        var t2 = new Triangle(5.0, 3.0, 4.0);
        Assertions.assertEquals(t1,t2);
    }
    @Test
    void testEquality4() {
        var t1 = new Triangle(3.0, 5.0, 4.0);
        var t2 = new Triangle(3.0, 5.0, 4.0);
        Assertions.assertEquals(t1,t2);
    }
    @Test
    void testEquality5() {
        var t1 = new Triangle(4.0, 3.0, 5.0);
        var t2 = new Triangle(4.0, 3.0, 5.0);
        Assertions.assertEquals(t1,t2);
    }
    @Test
    void testEquality6() {
        var t1 = new Triangle(5.0, 4.0, 3.0);
        var t2 = new Triangle(5.0, 4.0, 3.0);
        Assertions.assertEquals(t1,t2);
    }
}

