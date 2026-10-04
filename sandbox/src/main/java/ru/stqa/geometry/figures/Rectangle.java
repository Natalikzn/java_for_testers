package ru.stqa.geometry.figures;

import java.util.Objects;

public record Rectangle(double a, double b) {

    public Rectangle {
        if (a < 0 || b < 0) {
            throw new IllegalArgumentException("Сторона прямоугольника не может быть отрицательной");
        }
    }

    public static void printRectangleArea(Rectangle r) {
        String text = String.format("Площадь прямоугольника со сторонами %f и %f = %f", r.a, r.b, r.rectangleArea());
        System.out.println(text);
    }

    public double rectangleArea() {

        return this.a * this.b;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rectangle rectangle = (Rectangle) o;
        return (Double.compare(this.a, rectangle.a) == 0 && Double.compare(this.b, rectangle.b) == 0)
                || (Double.compare(this.a, rectangle.b) == 0 && Double.compare(this.b, rectangle.a) == 0);
    }

    @Override
    public int hashCode() {
        return 1;
        //Objects.hash(a, b);
    }
}
