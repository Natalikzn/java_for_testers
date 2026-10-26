package tests;

import model.ContactData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;


// Наследуем базовую настройку
public class ContactCreationTests extends TestBase {

    public static List<ContactData> contactProvider() {
        var result = new ArrayList<ContactData>();

        for (var firstname : List.of("", "first_name")) {
            for (var lastname : List.of("", "last_name")) {
                for (var nickname : List.of("", "nickname")) {
                    result.add(new ContactData()
                            .withFirstName(firstname)
                            .withLastName(lastname)
                            .withNickname(nickname));
                }
            }
        }
        for (int i = 1; i < 5; i++) {
            result.add(new ContactData()
                    .withFirstName(randomString(i * 10))
                    .withLastName(randomString(i * 10))
                    .withNickname(randomString(i * 10)));
        }
        return result;
    }

    @AfterEach
    public void tearDown() {
        app.contacts().removeAllContacts();
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

    @Test
    // Создаём пустой контакт (конструктор по умолчанию)
    public void canCreateContactWithEmptyName() {
        app.contacts().createContact(new ContactData());
    }

    @Test
    // Контакт только с именем
    public void canCreateContactWithNameOnly() {
        app.contacts().createContact(new ContactData().withFirstName("first_name"));
    }

    @ParameterizedTest
    @MethodSource("contactProvider")
    public void canCreateMultipleContacts(ContactData contact) {
        int contactCount = app.contacts().getCount();

        app.contacts().createContact(contact);

        int newContactCount = app.contacts().getCount();
        Assertions.assertEquals(contactCount + 1, newContactCount);
    }
}
