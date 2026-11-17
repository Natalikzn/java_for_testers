// Пакет tests — тестовые классы.
package tests;

import manager.ApplicationManager; // ApplicationManager — центральный объект управления браузером и хелперами.
import model.ContactData; // ContactData — модель контакта (используется в проверках списков).
import org.junit.jupiter.api.AfterEach; // @AfterEach — метод выполняется после КАЖДОГО теста.
import org.junit.jupiter.api.Assertions; // Assertions — методы проверок JUnit (нужны в общих assert-хелперах).
import org.junit.jupiter.api.BeforeEach; // @BeforeEach — метод выполняется перед КАЖДЫМ тестом.

import java.io.File; // File — используется для получения списка файлов в каталоге.
import java.io.FileReader; // FileReader — поток чтения символов из файла настроек.
import java.io.IOException; // IOException — проверяемое исключение ввода-вывода.
import java.nio.file.Paths; // Paths — построение путей.
import java.util.ArrayList; // ArrayList — изменяемый список.
import java.util.Comparator; // Comparator — сортировка контактов по числовому id.
import java.util.LinkedHashMap; // LinkedHashMap — карта атрибутов с предсказуемым порядком.
import java.util.List; // List — интерфейс списка.
import java.util.Map; // Map — интерфейс карты «атрибут → значение».
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

    // Проверка БД на консистентность, которая выполняется после каждого теста.
    // @AfterEach гарантирует выполнение даже при падении теста (в большинстве случаев).
    @AfterEach
    void checkDatabaseConsistency() {
        // Прямой SQL-запрос: если есть «висячие» связи, будет выброшено исключение — тест упадёт.
        app.jdbc().checkConsistency();
    }

    // ==================== Проверки списков контактов по данным БД ====================

    // Снимок списка контактов ИЗ БАЗЫ ДАННЫХ.
    //
    // Почему отдельный метод, а не прямой вызов app.hbm().getContactList() в каждом тесте:
    //   1) в одном месте зафиксировано, что источник данных — БД, а не страница UI,
    //      поэтому все тесты создания/удаления/модификации проверяют одно и то же;
    //   2) список сразу упорядочен по числовому id, чтобы сравнение «до/после»
    //      не зависело от порядка выдачи строк (без ORDER BY СУБД порядок не гарантирует).
    protected static List<ContactData> contactListFromDb() {
        // getContactList() читает таблицу addressbook через Hibernate и уже сортирует по id.
        return app.hbm().getContactList();
    }

    // Копия списка, упорядоченная по возрастанию числового id.
    // Нужна для построения ожидаемого результата: копия мутируется (add/remove/set),
    // поэтому исходный список из БД не трогаем.
    protected static List<ContactData> sortedById(List<ContactData> contacts) {
        // Компаратор по числовому значению id: id в модели — строка, поэтому parseInt.
        var byId = Comparator.comparingInt((ContactData contact) -> Integer.parseInt(contact.id()));
        // new ArrayList<>(...) создаёт независимую копию, затем сортируем её.
        var sorted = new ArrayList<>(contacts);
        sorted.sort(byId);
        return sorted;
    }

    // Превращает список контактов в список карт «атрибут → значение», упорядоченный по id.
    // Именно эти карты и сравниваются между собой: в карте есть все «плоские» атрибуты
    // контакта из таблицы addressbook (включая невидимые в UI), но нет фото
    // (см. ContactData#comparableAttributes).
    private static List<Map<String, String>> attributeRows(List<ContactData> contacts) {
        var rows = new ArrayList<Map<String, String>>();
        // sortedById гарантирует одинаковый порядок строк в ожидаемом и фактическом списках.
        for (var contact : sortedById(contacts)) {
            // comparableAttributes() возвращает LinkedHashMap: порядок атрибутов стабилен,
            // поэтому сообщения об ошибках читаются одинаково от прогона к прогону.
            rows.add(contact.comparableAttributes());
        }
        return rows;
    }

    // Полная сверка двух списков контактов «по всем сравнимым атрибутам».
    //
    // Что проверяется:
    //   * количество контактов;
    //   * набор id (то есть что добавлен/удалён именно нужный контакт);
    //   * значения всех текстовых атрибутов каждого контакта — имя, отчество, фамилия,
    //     никнейм, должность, компания, адрес, все телефоны и факс, три e-mail и
    //     домашняя страница (ровно те колонки, что реально есть в таблице addressbook);
    //   * фото НЕ сравнивается (blob, см. комментарий в ContactData).
    //
    // Почему не Assertions.assertEquals(expected, actual) напрямую по ContactData:
    // equals в ContactData сравнивает ТОЛЬКО id, поэтому такая проверка пропустила бы
    // контакт с правильным id, но испорченными именем/телефоном/адресом.
    protected static void assertContactListEquals(String step, List<ContactData> expected, List<ContactData> actual) {
        // Приводим оба списка к спискам карт атрибутов в одинаковом порядке.
        var expectedRows = attributeRows(expected);
        var actualRows = attributeRows(actual);

        // Быстрый путь: списки полностью совпали — проверка пройдена.
        if (expectedRows.equals(actualRows)) {
            return;
        }

        // Медленный путь: строим подробный отчёт о различиях и «валим» тест с ним.
        // Это сильно экономит время разбора падений: сразу видно, какой контакт
        // и какой именно атрибут разошёлся, вместо «два списка не равны».
        Assertions.fail(buildContactListDiff(step, expectedRows, actualRows));
    }

    // Формирует человекочитаемый отчёт о различиях двух списков карт атрибутов.
    private static String buildContactListDiff(String step,
                                               List<Map<String, String>> expected,
                                               List<Map<String, String>> actual) {
        // StringBuilder — эффективная сборка многострочного сообщения.
        var report = new StringBuilder();
        report.append("Списки контактов из БД различаются (").append(step).append(").").append(System.lineSeparator());
        report.append("Ожидалось контактов: ").append(expected.size())
                .append(", фактически: ").append(actual.size()).append(System.lineSeparator());

        // Проходим по общим индексам и сравниваем построчно.
        var common = Math.min(expected.size(), actual.size());
        for (int i = 0; i < common; i++) {
            // Карта атрибутов ожидаемого контакта.
            var expectedRow = expected.get(i);
            // Карта атрибутов фактического контакта.
            var actualRow = actual.get(i);
            // Если строки совпали — печатать нечего.
            if (expectedRow.equals(actualRow)) {
                continue;
            }
            // Собираем только отличающиеся атрибуты в формате «имя: ожидалось ... / было ...».
            var differences = new LinkedHashMap<String, String>();
            for (var attribute : expectedRow.keySet()) {
                var expectedValue = expectedRow.get(attribute);
                var actualValue = actualRow.get(attribute);
                if (!expectedValue.equals(actualValue)) {
                    differences.put(attribute, "ожидалось '" + expectedValue + "', фактически '" + actualValue + "'");
                }
            }
            report.append("  Контакт #").append(i + 1)
                    .append(" (id=").append(expectedRow.get("id"))
                    .append(") — отличия: ").append(differences).append(System.lineSeparator());
        }

        // Если фактический список длиннее — печатаем «лишние» контакты.
        for (int i = common; i < actual.size(); i++) {
            report.append("  Лишний контакт в БД: ").append(actual.get(i)).append(System.lineSeparator());
        }
        // Если фактический список короче — печатаем «недостающие» контакты.
        for (int i = common; i < expected.size(); i++) {
            report.append("  Не найден в БД: ").append(expected.get(i)).append(System.lineSeparator());
        }
        return report.toString();
    }
}
