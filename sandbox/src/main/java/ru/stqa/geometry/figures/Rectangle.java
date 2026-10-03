package ru.stqa.geometry.figures;

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
}
