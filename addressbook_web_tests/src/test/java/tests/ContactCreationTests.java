package tests;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import common.CommonFunctions;
import model.ContactData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator; // Comparator нужен для упорядочивания списков контактов по id перед сравнением
import java.util.List;

import static tests.TestBase.app;


// Наследуем базовую настройку
public class ContactCreationTests extends TestBase {

    public static List<ContactData> contactProvider() throws IOException {
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
        var mapper = new ObjectMapper();

        /*for (int i = 1; i < 3; i++) {
            result.add(new ContactData()
                    .withFirstName(CommonFunctions.randomString(i * 10))
                    .withLastName(CommonFunctions.randomString(i * 10))
                    .withNickname(CommonFunctions.randomString(i * 10)));
        }*/

        var value = mapper.readValue(new File("contacts.json"),
                new TypeReference<List<ContactData>>() {
        });
        result.addAll(value);
        return result;
    }

    /*public static List<ContactData> negativeContactProvider() {
        var result = new ArrayList<ContactData>(List.of(
                new ContactData("", "contact ' lastname", "", "")));
        return result;
    }*/

    // очищаем список после тестов
    /*@AfterEach
    public void tearDown() {
        app.contacts().removeAllContacts();
    }*/

    /*@Test
    void canCreateContact() {
        var contact = new ContactData()
                .withFirstName(CommonFunctions.randomString(10))
                .withLastName(CommonFunctions.randomString(10))
                .withPhoto(randomFile("src/test/resources/images/"));
        app.contacts().createContact(contact);
    }*/

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

    @ParameterizedTest
    @MethodSource("contactProvider")
    public void canCreateMultipleContacts(ContactData contact) {
        var oldContacts = app.contacts().getList(); // получаем старый список контактов (id + имя + фамилия)
        app.contacts().createContact(contact);
        var newContacts = app.contacts().getList(); // получаем новый список (id + имя + фамилия)
        //int newContactCount = app.contacts().getCount();
        Comparator<ContactData> compareById = (o1, o2) -> {
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        newContacts.sort(compareById); // упорядочиваем список контактов после создания нового контакта
        var expectedList = new ArrayList<>(oldContacts);
        expectedList.add(contact.withId(newContacts.get(newContacts.size() - 1).id()).withNickname(""));
        // берем последний максимальный идентификатор, очищаем nickname перед сравнением, чтобы он не участвовал в сравнении (сравниваем только имя и фамилию)
        newContacts.replaceAll(c -> c.withPhoto(null)); // исключаем фото из сравнения
        expectedList.replaceAll(c -> c.withPhoto(null));
        expectedList.sort(compareById); // сортируем expectedList

        Assertions.assertEquals(newContacts, expectedList); // сравниваем старый список, в который добавлен элемент, и новый список
    }
}
