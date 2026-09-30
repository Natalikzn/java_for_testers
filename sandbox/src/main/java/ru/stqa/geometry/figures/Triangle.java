package ru.stqa.geometry.figures;

import static java.lang.Math.sqrt;

public record Triangle(double a, double b, double c) {


    public static void printTrianglePerimeter(Triangle t) {
        var text = String.format("Периметр треугольника со сторонами %f, %f  и %f = %f", t.a, t.b, t.c, t.trianglePerimeter());
        System.out.println(text);
    }

    public static void printTriangleArea(Triangle t) {
        var text = String.format("Площадь треугольника со сторонами %f, %f  и %f = %f", t.a, t.b, t.c, t.triangleArea());
        System.out.println(text);
    }

    // Считаем периметр
    public double trianglePerimeter() {
        return this.a + this.b + this.c;
    }

    // Считаем площадь
    public double triangleArea() {
        double p = trianglePerimeter() / 2;
        double value = p * (p - a) * (p - b) * (p - c);
        return sqrt(value);
    }


}



