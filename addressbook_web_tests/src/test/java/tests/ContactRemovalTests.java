package tests;

import model.ContactData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

public class ContactRemovalTests extends TestBase {


    @Test
    public void canRemoveContact() {
        app.contacts().homePage(); // Гарантированно переходим на страницу home
        // Если нет контакта, создаём один, чтобы было что удалять
        if (!app.contacts().isContactPresent()) {
            app.contacts().createContact(new ContactData("", "", "", ""));
        }
        // Удаляем контакт
        var oldContacts = app.contacts().getList(); // получаем старый список
        var rnd = new Random();
        var index = rnd.nextInt(oldContacts.size());
        app.contacts().removeContact(oldContacts.get(index)); // удаляем контакт
        var newContacts = app.contacts().getList(); // после удаления получаем новый список
        var expectesList = new ArrayList<>(oldContacts);
        expectesList.remove(index);
        Assertions.assertEquals(newContacts, expectesList); // сравниваем новый список со старым без удалённого элемента
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
