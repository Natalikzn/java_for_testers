// Пакет tests — тестовые классы.
package tests;

// Модель контакта.
import model.ContactData;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация обычного теста.
import org.junit.jupiter.api.Test;

// ArrayList — для построения ожидаемого списка.
import java.util.ArrayList;
// Random — случайный выбор контакта для удаления.
import java.util.Random;

// Тесты удаления контактов.
public class ContactRemovalTests extends TestBase {

    // Удаление одного (случайно выбранного) контакта.
    @Test
    public void canRemoveContact() {
        app.contacts().homePage(); // Гарантированно переходим на страницу home
        // Если нет контакта, создаём один, чтобы было что удалять
        if (!app.contacts().isContactPresent()) {
            // Создаём полностью «пустой» контакт (все поля пустые строки).
            app.contacts().createContact(new ContactData("", "", "", "", "", ""));
        }
        // Удаляем контакт
        var oldContacts = app.contacts().getList(); // Снимок списка до удаления.
        /*Assertions.assertFalse(oldContacts.isEmpty(), //"Нет ни одного контакта для удаления");*/
        var rnd = new Random(); // Генератор случайных чисел.
        var index = rnd.nextInt(oldContacts.size()); // Случайный индекс в пределах текущего списка.
        app.contacts().removeContact(oldContacts.get(index)); // Удаляем контакт, стоящий по выбранному индексу.
        var newContacts = app.contacts().getList(); // после удаления получаем новый список
        var expectedList = new ArrayList<>(oldContacts); // Копия старого списка — станет ожидаемым результатом.
        expectedList.remove(index); // Убираем из ожидаемого списка удалённый элемент.
        // сравниваем новый список со старым без удалённого элемента
        // ContactData.equals сравнивает по id, поэтому сравнение корректно.
        Assertions.assertEquals(newContacts, expectedList);
    }

    // Удаление всех контактов одной операцией.
    @Test
    void canRemoveAllContactsAtOnce() {
        // Если контактов нет — создаём один пустой контакт, чтобы удаление было не «вхолостую».
        if (app.contacts().getCount() == 0) {
            app.contacts().createContact(new ContactData("", "", "", "", "", ""));
        }
        app.contacts().removeAllContacts(); // Отмечаем все контакты и удаляем одним нажатием.
        Assertions.assertEquals(0, app.contacts().getCount()); // После удаления на странице не должно остаться ни одного контакта
    }
}
