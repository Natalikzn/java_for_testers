package tests;

import manager.ApplicationManager;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Paths;
import java.util.Random;

public class TestBase {

    protected static ApplicationManager app;


    @BeforeEach
    public void setUp() {
        if (app == null) {
            // Создаём менеджер, если его ещё нет
            app = new ApplicationManager();
        }
// Получаем имя браузера из системных свойств (по умолчанию — firefox)
        app.init(System.getProperty("browser", "firefox"));

    }

    public static String randomFile(String dir) {
        var fileNames = new File(dir).list();
        if (fileNames == null || fileNames.length == 0) {
            throw new IllegalStateException("В директории нет файлов: " + dir);
        }
        var rnd = new Random();
        var index = rnd.nextInt(fileNames.length); // получаем случайное число, не превышающее количество файлов
        return Paths.get(dir, fileNames[index]).toString();
    }
}
