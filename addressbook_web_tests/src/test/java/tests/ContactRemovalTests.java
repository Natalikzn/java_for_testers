package tests;

import model.ContactData;
import org.junit.jupiter.api.Test;

public class ContactRemovalTests extends TestBase {


    @Test
    public void canRemoveContact() {
        app.contacts().homePage(); // Гарантированно переходим на страницу home
        // Если нет контакта, создаём один, чтобы было что удалять
        if (!app.contacts().isContactPresent()) {
            app.contacts().createContact(new ContactData("", ""));
        }
        // Удаляем контакт
        app.contacts().removeContact();
    }
}
