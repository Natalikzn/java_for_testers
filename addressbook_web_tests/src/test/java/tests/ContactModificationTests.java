// Пакет tests — тестовые классы.
package tests;

// Утилита случайных строк для генерации тестовых данных.
import common.CommonFunctions;
// Модель контакта.
import model.ContactData;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация обычного теста.
import org.junit.jupiter.api.Test;

// ArrayList — копия списка для «ожидаемого» результата.
import java.util.ArrayList;
// Comparator — сортировка списков перед сравнением.
import java.util.Comparator;

// Тесты модификации контактов.
public class ContactModificationTests extends TestBase {
    // Проверка предусловия: в приложении должно быть не меньше 5 контактов.
    @Test
    void canModifyContact() {
        // Если контактов меньше пяти…
        if (app.contacts().getCount() < 5) {
            app.contacts().removeAllContacts(); // чистим список...
            for (int i = 0; i < 5; i++) {       // ...создаём 5 контактов
                app.contacts().createContact(new ContactData()
                        // Каждый контакт — с случайными именем, фамилией и никнеймом.
                        .withFirstName(CommonFunctions.randomString(10))
                        .withLastName(CommonFunctions.randomString(10))
                        .withNickname(CommonFunctions.randomString(10)));
            }
        }
        // Проверяем, что контактов действительно не меньше пяти.
        Assertions.assertTrue(app.contacts().getCount() >= 5);
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
        // Проверяем, что контакт появился.
        Assertions.assertTrue(app.contacts().isContactPresent());

        // Снимок списка до изменения.
        var oldContacts = app.contacts().getList();
        // Индекс контакта, который будем менять (первый в списке).
        var index = 0;
        // Данные для изменения: новые имя и фамилия.
        var testData = new ContactData()
                .withFirstName("modified firstname4")
                .withLastName("modified lastname4");

        // Открываем форму нужного контакта, заполняем новыми данными и сохраняем.
        app.contacts().modifyContact(oldContacts.get(index), testData);

        // Снимок списка после изменения.
        var newContacts = app.contacts().getList();
        // Копируем старый список — из него построим ожидаемый результат.
        var expectedList = new ArrayList<>(oldContacts);
        // В ожидаемом списке заменяем элемент по тому же индексу изменёнными данными (id сохраняем).
        expectedList.set(index, testData.withId(oldContacts.get(index).id()));
        // Сортируем ожидаемый список по id (id — строка, поэтому лексикографический порядок).
        expectedList.sort(Comparator.comparing(ContactData::id));
        // Сортируем фактический список тем же способом, чтобы сравнение не зависело от порядка.
        newContacts.sort(Comparator.comparing(ContactData::id));

        // Контакты равны по id (см. equals в ContactData), поэтому список должен совпасть.
        Assertions.assertEquals(expectedList, newContacts);
        /*Assertions.assertEquals(oldContacts.size(), newContacts.size()); */
        //количество контактов не изменилось
    }
}
