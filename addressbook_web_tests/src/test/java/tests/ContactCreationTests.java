// Пакет tests — тесты, работающие через UI и БД.
package tests;

// Модель группы — нужна для создания группы и привязки контакта.

import common.CommonFunctions;
import model.ContactData;
import model.GroupData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TypeReference — позволяет Jackson десериализовать обобщённый тип List<ContactData>.
// ObjectMapper — чтение JSON-файла в объекты.
// Утилита генерации случайных строк.
// Модель контакта.
// Методы-проверки JUnit.
// @Test — обычный тест.
// @ParameterizedTest — тест, выполняемый несколько раз с разными данными.
// @MethodSource — указывает метод-поставщик набора параметров.
// File — чтение файла данных.
// IOException — проверяемое исключение при чтении файла.
// ArrayList — изменяемый список.
// Comparator нужен для упорядочивания списков контактов по id перед сравнением
// List — интерфейс списка.
// Статический импорт: обращаемся к общему менеджеру как app.


// Наследуем базовую настройку TestBase — получаем @BeforeEach/@AfterEach и поле app.
public class ContactCreationTests extends TestBase {
    // Поставщик данных для параметризованного теста: читает контакты из contacts.json.
    public static List<ContactData> contactProvider() throws IOException {
        // Аккумулятор набора тестовых данных.
        var result = new ArrayList<ContactData>();

        /*for (var firstname : List.of("", "first_name")) {
            for (var lastname : List.of("", "last_name")) {
                for (var nickname : List.of("", "nickname")) {
                    result.add(new ContactData()
                            .withFirstName(firstname)
                            .withLastName(lastname)
                            .withNickname(nickname));
                }
            }
        }*/

        // Создаём JSON-маппер для чтения файла.
        var mapper = new ObjectMapper();

        /*for (int i = 1; i < 3; i++) {
            result.add(new ContactData()
                    .withFirstName(CommonFunctions.randomString(i * 10))
                    .withLastName(CommonFunctions.randomString(i * 10))
                    .withNickname(CommonFunctions.randomString(i * 10)));
        }*/
        // Читаем JSON-файл contacts.json и превращаем его в List<ContactData>.
        // TypeReference сохраняет информацию о генерическом типе при десериализации.
        var value = mapper.readValue(new File("contacts.json"),
                new TypeReference<List<ContactData>>() {
        });
        // Добавляем все прочитанные контакты в общий набор.
        result.addAll(value);
        // Возвращаем набор для параметризованного теста.
        return result;
    }

    /*public static List<ContactData> negativeContactProvider() {
        var result = new ArrayList<ContactData>(List.of(
                new ContactData("", "contact ' lastname", "", "")));
        return result;
    }

    // очищаем список после тестов
    @AfterEach
    public void tearDown() {
        app.contacts().removeAllContacts();
    }*/

    // Тест на создание контакта, не включенного в группу
    @Test
    void canCreateContact() {
        // Готовим данные: случайные имя/фамилия и случайное фото из каталога картинок.
        var contact = new ContactData()
                .withFirstName(CommonFunctions.randomString(10)) // Имя — 10 случайных букв.
                .withLastName(CommonFunctions.randomString(10)) // Фамилия — 10 случайных букв.
                .withPhoto(randomFile("src/test/resources/images/")); // Фото — путь к случайному файлу изображения.
        // Создаём контакт через UI. Проверка результата здесь не выполняется (smoke-сценарий).
        app.contacts().createContact(contact);
    }

    // Тест на создание контакта, включенного в группу
    @Test
    void canCreateContactInGroup() {
        var contact = new ContactData()
                // Тестовые данные контакта: случайные имя, фамилия, случайное фото.
                .withFirstName(CommonFunctions.randomString(10))
                .withLastName(CommonFunctions.randomString(10))
                .withPhoto(randomFile("src/test/resources/images/"));
        // для выбора группы если ее нет, то ее нужно создать
        // Считаем группы напрямую в БД, чтобы не зависеть от UI.
        if (app.hbm().getGroupCount() == 0) {
            app.hbm().createGroup(new GroupData("", "group name", "group header", "group footer"));
        }
        // получаем список групп и выбираем первую из них
        var group = app.hbm().getGroupList().get(0);
        // смотрим какие контакты уже включены в эту группу
        var oldRelated = app.hbm().getContactsInGroup(group);
        // создаем контакт
        app.contacts().createContact(contact, group);
        // загружаем новый список
        var newRelated = app.hbm().getContactsInGroup(group);
        // здесь надо будет сделать полноценную сверку списков по фио, а не только по изменившемуся количеству
        Assertions.assertEquals(oldRelated.size() + 1, newRelated.size());// проверка, что новый список содержит на 1 элемент больше, чем старый
    }



    /*@ParameterizedTest
    // Создаём контакт с заполненными полями
    @ValueSource(strings = {"Ivan", "Petr"})
    public void canCreateContact(String firstname) {
        app.contacts().createContact(new ContactData(firstname, "last_name", "nickname"));
        int before = app.contacts().getCount();
        app.contacts().createContact(new ContactData(firstname, "last_name", "nickname"));
        Assertions.assertEquals(before + 1, app.contacts().getCount());
    }*/

    /*@Test
    // Создаём пустой контакт (конструктор по умолчанию)
    public void canCreateContactWithEmptyName() {
        app.contacts().createContact(new ContactData());
    }*/

    /*@Test
    // Контакт только с именем
    public void canCreateContactWithNameOnly() {
        app.contacts().createContact(new ContactData().withFirstName("first_name"));
    }*/

    // Параметризованный тест: создаём контакт с данными из contactProvider (contacts.json).
    @ParameterizedTest
    @MethodSource("contactProvider") // Источник данных — метод contactProvider.
    public void canCreateMultipleContacts(ContactData contact) {
        // получаем старый список контактов (id + имя + фамилия)
        var oldContacts = app.contacts().getList();
        // Создаём новый контакт через UI с переданными данными.
        app.contacts().createContact(contact);
        // получаем новый список (id + имя + фамилия)
        var newContacts = app.contacts().getList();
        /*int newContactCount = app.contacts().getCount();*/

        // Компаратор: сравниваем контакты по числовому значению id.
        Comparator<ContactData> compareById = (o1, o2) -> {
            // Integer.compare возвращает -1/0/1; id в модели — строка, поэтому парсим в int.
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        // упорядочиваем список контактов после создания нового контакта
        newContacts.sort(compareById);
        // Копируем старый список в «ожидаемый» — он будет изменён.
        var expectedList = new ArrayList<>(oldContacts);
        // Добавляем новый контакт: берём максимальный id (последний после сортировки) и обнуляем nickname.
        expectedList.add(contact.withId(newContacts.get(newContacts.size() - 1).id()).withNickname(""));
        // берем последний максимальный идентификатор, очищаем nickname перед сравнением, чтобы он не участвовал в сравнении (сравниваем только имя и фамилию)
        newContacts.replaceAll(c -> c.withPhoto(null)); // исключаем фото из сравнения
        // В ожидаемом списке также обнуляем photo — чтобы сравнение было корректным.
        expectedList.replaceAll(c -> c.withPhoto(null));
        expectedList.sort(compareById); // сортируем expectedList
        // Сравниваем фактический список с ожидаемым (порядок уже одинаковый).
        Assertions.assertEquals(newContacts, expectedList); // сравниваем старый список, в который добавлен элемент, и новый список
    }
}
