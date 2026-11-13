// Пакет tests — тестовые классы.
package tests;

import manager.ApplicationManager; // ApplicationManager — центральный объект управления браузером и хелперами.
import org.junit.jupiter.api.AfterEach; // @AfterEach — метод выполняется после КАЖДОГО теста.
import org.junit.jupiter.api.BeforeEach; // @BeforeEach — метод выполняется перед КАЖДЫМ тестом.

import java.io.File; // File — используется для получения списка файлов в каталоге.
import java.io.FileReader; // FileReader — поток чтения символов из файла настроек.
import java.io.IOException; // IOException — проверяемое исключение ввода-вывода.
import java.nio.file.Paths; // Paths — построение путей.
import java.util.Properties; // Properties — хранилище настроек «ключ=значение».
import java.util.Random; // Random — генератор случайных чисел для выбора случайного файла.

// Базовый класс всех тестов: общая инициализация и общие утилиты.
public class TestBase {
    // static — менеджер создаётся ОДИН раз на весь прогон тестов и переиспользуется.
    // protected — доступен наследникам (тестовым классам).
    protected static ApplicationManager app;

    // Выбрать случайный файл из каталога dir и вернуть его путь.
    public static String randomFile(String dir) {
        // Получаем имена всех файлов в каталоге (list() возвращает массив имён).
        var fileNames = new File(dir).list();
        // Если каталога нет или он пуст — работать не с чем.
        if (fileNames == null || fileNames.length == 0) {
            // Явная ошибка с указанием каталога.
            throw new IllegalStateException("В директории нет файлов: " + dir);
        }
        // Генератор случайных чисел.
        var rnd = new Random();
        // получаем случайное число, не превышающее количество файлов
        var index = rnd.nextInt(fileNames.length);
        // Склеиваем путь: каталог + случайное имя файла, возвращаем строкой.
        return Paths.get(dir, fileNames[index]).toString();
    }

    // @BeforeEach: подготовка перед каждым тестом.
    @BeforeEach
    public void setUp() throws IOException {
        // Инициализируем приложение только если оно ещё не создано (экономия ресурсов).
        if (app == null) {
            // Создаём контейнер настроек.
            var properties = new Properties();
            // Загружаем настройки из файла; System.getProperty("target", "local.properties")
            // берёт системное свойство target, а если его нет — использует local.properties.
            properties.load(new FileReader(System.getProperty("target", "local.properties")));
            // Создаём менеджер, если его ещё нет
            app = new ApplicationManager();

            // System.getProperty("browser", "firefox") — имя браузера из Gradle-свойства или firefox по умолчанию.
            app.init(System.getProperty("browser", "firefox"), properties);
        }
    }

    // Проверка БД на консисестентность, которая выполняется после каждого теста
    // @AfterEach гарантирует выполнение даже при падении теста (в большинстве случаев).
    @AfterEach
    void checkDatabaseConsistency() {
        // Прямой SQL-запрос: если есть «висячие» связи, будет выброшено исключение — тест упадёт.
        app.jdbc().checkConsistency();
    }
}
