package ru.stqa.geometry;

import ru.stqa.geometry.figures.Triangle;

public class Geometry {
    static void main() {
        //Square.printSquareArea(new Square(7.0));

        //Rectangle.printRectangleArea(3.0, 5.0);

        Triangle.printTrianglePerimeter(new Triangle(3.0, 4.0, 5.0));
        Triangle.printTriangleArea(new Triangle(3.0, 4.0, 5.0));
    }

}
