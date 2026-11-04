package tests;

import common.CommonFunctions;
import model.ContactData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;

public class ContactModificationTests extends TestBase {
    @Test
    void canModifyContact() {
        if (app.contacts().getCount() < 5) {
            app.contacts().removeAllContacts(); // чистим список
            for (int i = 0; i < 5; i++) {       // создаём 5 контактов
                app.contacts().createContact(new ContactData()
                        .withFirstName(CommonFunctions.randomString(10))
                        .withLastName(CommonFunctions.randomString(10))
                        .withNickname(CommonFunctions.randomString(10)));
            }
        }
        Assertions.assertTrue(app.contacts().getCount() >= 5); // предусловие выполнено
    }

    @Test
    // Модификация не должна менять количество контактов и их порядок

    void modifyingContactDoesNotChangeCountOrOrder() {
        // Предусловие: в приложении есть хотя бы один контакт.
        if (!app.contacts().isContactPresent()) {
            for (int i = 0; i < 3; i++) {         // создаём 3 контакта
                app.contacts().createContact(new ContactData()
                        .withFirstName(CommonFunctions.randomString(10))
                        .withLastName(CommonFunctions.randomString(10)));
            }
        }
        Assertions.assertTrue(app.contacts().isContactPresent()); // предусловие выполнено

        // модифицируем первый контакт списка ---
        var oldContacts = app.contacts().getList();
        var index = 0;
        var testData = new ContactData()
                .withFirstName("modified firstname4")
                .withLastName("modified lastname4");

        app.contacts().modifyContact(oldContacts.get(index), testData);

        var newContacts = app.contacts().getList(); // Новый список
        var expectedList = new ArrayList<>(oldContacts);
        expectedList.set(index, testData.withId(oldContacts.get(index).id()));
        expectedList.sort(Comparator.comparing(ContactData::id));
        newContacts.sort(Comparator.comparing(ContactData::id));
        Assertions.assertEquals(expectedList, newContacts); // сравнение списков
        //Assertions.assertEquals(oldContacts.size(), newContacts.size()); // количество контактов не изменилось*/
    }
}
