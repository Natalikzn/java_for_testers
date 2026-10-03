public class Hello {
    public static void main(String[] args) {
        var x = 1;
        var y = 1;

        if (y == 0) {
            System.out.println("Деление на 0 запрещено");
        } else {
            var z = divide(x, y);
            System.out.println(z);
            System.out.println("Моя первая программа!");
        }
    }

    private static int divide(int x, int y) {
        var z = x / y;
        return z;
    }
}
