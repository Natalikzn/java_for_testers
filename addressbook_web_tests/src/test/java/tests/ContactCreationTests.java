// Пакет tests — тесты, работающие через UI и БД.
package tests;

// Утилита генерации случайных строк (нужна для тестовых данных).
import common.CommonFunctions;
// Модели контакта и группы.
import model.ContactData;
import model.GroupData;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// @Test — обычный тест; @ParameterizedTest — тест с набором параметров; @MethodSource — поставщик параметров.
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
// Jackson 3: TypeReference сохраняет генерический тип List<ContactData>, ObjectMapper читает JSON.
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File; // File — источник данных (contacts.json).
import java.io.IOException; // IOException — проверяемое исключение чтения файла.
import java.util.ArrayList; // ArrayList — изменяемый список.
import java.util.List; // List — интерфейс списка.

// Тесты создания контактов.
//
// Задание 14: во всех проверках списки читаются ИЗ БАЗЫ ДАННЫХ (app.hbm().getContactList()
// через contactListFromDb() в TestBase), а не из таблицы контактов в интерфейсе.
// Благодаря этому сравниваются и те атрибуты, которых в списке UI вообще нет:
// отчество, должность, компания, все телефоны, факс, три e-mail и домашняя страница.
// Фото не сравнивается — это blob.
// В таблице addressbook нет колонок address2, phone2 и notes, поэтому эти поля в тестах
// и сравнении не участвуют.
public class ContactCreationTests extends TestBase {
    // Поставщик данных для параметризованного теста: читает контакты из contacts.json.
    public static List<ContactData> contactProvider() throws IOException {
        // Аккумулятор набора тестовых данных.
        var result = new ArrayList<ContactData>();
        // Создаём JSON-маппер для чтения файла.
        var mapper = new ObjectMapper();
        // Читаем JSON-файл contacts.json и превращаем его в List<ContactData>.
        // TypeReference сохраняет информацию о генерическом типе при десериализации.
        var value = mapper.readValue(new File("contacts.json"), new TypeReference<List<ContactData>>() {
        });
        // Добавляем все прочитанные контакты в общий набор.
        result.addAll(value);
        // Возвращаем набор для параметризованного теста.
        return result;
    }

    // Поставщик «богатого» контакта: заполнены ВСЕ атрибуты, которые мы умеем сравнивать.
    // Такой контакт позволяет проверить, что в БД действительно сохраняются поля,
    // невидимые в списке контактов (отчество, компания, телефоны, e-mail).
    public static List<ContactData> singleRichContact() {
        return List.of(new ContactData()
                // Имя, отчество, фамилия, никнейм.
                .withFirstName("Ivan")
                .withMiddleName("Ivanovich")
                .withLastName("Petrov")
                .withNickname("vanya")
                // Должность и компания.
                .withTitle("QA engineer")
                .withCompany("MOEX")
                // Адрес (в таблице addressbook только одна колонка address).
                .withAddress("Moscow, Bolshoy Kislovsky 13")
                // Телефоны и факс (отдельной колонки phone2 в таблице нет).
                .withHome("+7 495 111-22-33")
                .withMobile("+7 916 444-55-66")
                .withWork("+7 495 777-88-99")
                //.withFax("+7 495 000-11-22")
                // Три e-mail и домашняя страница.
                .withEmail("ivan.petrov@example.com")
                .withEmail2("i.petrov@example.com")
                .withEmail3("petrov@example.com")
                .withHomepage("https://example.com/ivan")
                // Фото тоже прикладываем, но в сравнение оно не попадёт.
                .withPhoto(randomFile("src/test/resources/images/"))
        );
    }

    // Тест на создание контакта, не включенного в группу.
    @Test
    void canCreateContact() {
        // Готовим данные: случайные имя/фамилия и случайное фото из каталога картинок.
        var contact = new ContactData()
                .withFirstName(CommonFunctions.randomString(10)) // Имя — 10 случайных букв.
                .withLastName(CommonFunctions.randomString(10)) // Фамилия — 10 случайных букв.
                .withPhoto(randomFile("src/test/resources/images/")); // Фото — путь к случайному файлу изображения.

        // Снимок списка контактов ИЗ БД до создания (упорядочен по id).
        var oldContacts = contactListFromDb();
        // Создаём контакт через UI.
        app.contacts().createContact(contact);
        // Снимок списка контактов ИЗ БД после создания.
        var newContacts = contactListFromDb();

        // Новых контактов должно стать ровно на один больше — иначе непонятно, какой id брать.
        Assertions.assertEquals(oldContacts.size() + 1, newContacts.size(),
                "После создания контакта в БД должен появиться ровно один новый контакт");
        // Находим добавленный контакт как разность по id: он единственный, кого не было раньше.
        var added = addedContacts(oldContacts, newContacts);
        Assertions.assertEquals(1, added.size(),
                "Должен быть найден ровно один новый контакт");

        // Ожидаемый список: прежние контакты + созданный контакт с id, который присвоила БД.
        var expectedList = sortedById(oldContacts);
        expectedList.add(contact.withId(added.get(0).id()));

        // Полная сверка по ВСЕМ сравнимым атрибутам, а не только по количеству:
        // сравниваются имя, отчество, фамилия, никнейм, должность, компания, адрес,
        // телефоны, факс, e-mail и домашняя страница (фото — нет).
        assertContactListEquals("после создания контакта", expectedList, newContacts);
    }

    // Тест на создание контакта, включенного в группу.
    @Test
    void canCreateContactInGroup() {
        // Контакт для добавления в группу: заполняем несколько «невидимых» в UI атрибутов,
        // чтобы проверка из БД ловила их потерю.
        var contact = new ContactData()
                .withFirstName(CommonFunctions.randomString(10))
                .withMiddleName(CommonFunctions.randomString(10))
                .withLastName(CommonFunctions.randomString(10))
                .withCompany(CommonFunctions.randomString(10))
                .withMobile(CommonFunctions.randomString(10))
                .withEmail(CommonFunctions.randomString(5) + "@example.com")
                .withPhoto(randomFile("src/test/resources/images/"));
        // для выбора группы если ее нет, то ее нужно создать
        // Считаем группы напрямую в БД, чтобы не зависеть от UI.
        if (app.hbm().getGroupCount() == 0) {
            app.hbm().createGroup(new GroupData("", "group name", "group header", "group footer"));
        }
        // получаем список групп и выбираем первую из них
        var group = app.hbm().getGroupList().get(0);
        // Снимок состава группы ИЗ БД до создания контакта.
        var oldRelated = app.hbm().getContactsInGroup(group);
        // создаем контакт через UI сразу с привязкой к группе
        app.contacts().createContact(contact, group);
        // Снимок состава группы ИЗ БД после создания контакта.
        var newRelated = app.hbm().getContactsInGroup(group);

        // id контактов, которые были в группе до создания нового.
        var oldIds = oldRelated.stream().map(ContactData::id).toList();
        // Контакты, которых раньше не было: ожидаем ровно один — только что созданный.
        var added = newRelated.stream().filter(c -> !oldIds.contains(c.id())).toList();
        Assertions.assertEquals(1, added.size(),
                "В группе должен появиться ровно один новый контакт");

        // Ожидаемый состав группы: прежние контакты + созданный контакт с id из БД.
        var expectedList = sortedById(oldRelated);
        expectedList.add(contact.withId(added.get(0).id()));

        // Полная сверка состава группы по всем сравнимым атрибутам (количество, id, ФИО,
        // телефоны, компания, e-mail…) — а не только по числу элементов, как было раньше.
        assertContactListEquals("после создания контакта в группе", expectedList, newRelated);
    }

    // Параметризованный тест: создаём контакт с данными из contactProvider (contacts.json).
    // Проверка списка контактов также выполняется по данным БД.
    @ParameterizedTest
    @MethodSource("contactProvider")
    public void canCreateMultipleContacts(ContactData contact) {
        // Снимок списка контактов из БД до создания.
        var oldContacts = contactListFromDb();
        // Создаём новый контакт через UI с переданными данными.
        app.contacts().createContact(contact);
        // Снимок списка контактов из БД после создания.
        var newContacts = contactListFromDb();

        // Ровно один новый контакт.
        Assertions.assertEquals(oldContacts.size() + 1, newContacts.size(),
                "После создания контакта в БД должен появиться ровно один новый контакт");
        // Находим добавленный контакт по разности id.
        var added = addedContacts(oldContacts, newContacts);
        Assertions.assertEquals(1, added.size(),
                "Должен быть найден ровно один новый контакт");

        // Ожидаемый список: старый список + созданный контакт с id из БД.
        var expectedList = sortedById(oldContacts);
        expectedList.add(contact.withId(added.get(0).id()));

        // Сверка по всем сравнимым атрибутам.
        assertContactListEquals("после создания контакта из файла данных", expectedList, newContacts);
    }

    // Параметризованный тест на «богатый» контакт: заполнены все сравниваемые атрибуты.
    // Проверяем, что в БД сохранились даже те поля, которые не видны в списке контактов UI.
    @ParameterizedTest
    @MethodSource("singleRichContact")
    public void canCreateContactWithAllAttributes(ContactData contact) {
        // Снимок списка контактов из БД до создания.
        var oldContacts = contactListFromDb();
        // Создаём контакт со всеми заполненными полями.
        app.contacts().createContact(contact);
        // Снимок списка контактов из БД после создания.
        var newContacts = contactListFromDb();

        // Находим добавленный контакт по разности id.
        var added = addedContacts(oldContacts, newContacts);
        Assertions.assertEquals(1, added.size(),
                "Должен быть найден ровно один новый контакт");

        // Ожидаемый список: старый список + созданный контакт с id из БД.
        var expectedList = sortedById(oldContacts);
        expectedList.add(contact.withId(added.get(0).id()));

        // Полная сверка по всем атрибутам: именно она подтверждает, что отчество, должность,
        // компания, адрес, телефоны, три e-mail и домашняя страница действительно дошли до БД,
        // хотя в списке контактов UI их не видно.
        assertContactListEquals("после создания контакта со всеми атрибутами", expectedList, newContacts);
    }

    // Контакты, которые есть в newContacts, но отсутствовали в oldContacts (разность по id).
    private static List<ContactData> addedContacts(List<ContactData> oldContacts, List<ContactData> newContacts) {
        // Собираем id «старых» контактов в набор для быстрой проверки вхождения.
        var oldIds = oldContacts.stream().map(ContactData::id).toList();
        // Оставляем в новом списке только те контакты, чьего id не было в старом.
        return newContacts.stream().filter(contact -> !oldIds.contains(contact.id())).toList();
    }
}
