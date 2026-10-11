package tests;

import model.ContactData;
import org.junit.jupiter.api.Test;

// Наследуем базовую настройку
public class ContactCreationTests extends TestBase {


    @Test
    public void canCreateContact() {
        // Создаём контакт с заполненными полями
        app.contacts().createContact(new ContactData("first name", "last name"));

    }

    @Test
    // Создаём пустой контакт (конструктор по умолчанию)
    public void canCreateContactWithEmptyName() {
        app.contacts().createContact(new ContactData());
    }

    @Test
    // Контакт только с именем
    public void canCreateContactWithNameOnly() {
        app.contacts().createContact(new ContactData().withFirstName("first name"));
    }
}
