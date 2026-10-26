package tests;

import model.ContactData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContactRemovalTests extends TestBase {


    @Test
    public void canRemoveContact() {
        app.contacts().homePage(); // Гарантированно переходим на страницу home
        // Если нет контакта, создаём один, чтобы было что удалять
        if (!app.contacts().isContactPresent()) {
            app.contacts().createContact(new ContactData("", "", "", ""));
        }
        // Удаляем контакт
        app.contacts().removeContact();
    }

    @Test
    void canRemoveAllContactsAtOnce() {
        if (app.contacts().getCount() == 0) {
            app.contacts().createContact(new ContactData("", "", "", ""));
        }
        app.contacts().removeAllContacts();
        Assertions.assertEquals(0, app.contacts().getCount());
    }
}
