// Пакет common содержит общие утилиты, используемые разными частями проекта
package common;

import java.util.Random;

// Утилитный класс с общими функциями для тестов и генератора данных
public class CommonFunctions {

    // Статический метод: строит случайную строку длиной n из строчных латинских букв
    public static String randomString(int n) {
        var rnd = new Random();
        var result = "";
        for (int i = 0; i < n; i++) {
            result = result + (char) ('a' + rnd.nextInt(26));
        }
        return result;
    }
}
