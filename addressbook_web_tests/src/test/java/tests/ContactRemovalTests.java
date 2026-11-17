// Пакет tests — тестовые классы.
package tests;

// Модель контакта.
import model.ContactData;
// Класс с генераторами случайных строк.
import common.CommonFunctions;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация обычного теста.
import org.junit.jupiter.api.Test;

import java.util.Random; // Random — случайный выбор контакта.

// Тесты удаления контактов.
//
// Задание 14: до и после удаления списки контактов читаются ИЗ БАЗЫ ДАННЫХ
// (contactListFromDb() в TestBase), поэтому проверяется не только «стало на один меньше»,
// но и что удалён именно выбранный контакт, а остальные контакты не изменились
// ни в одном сравниваемом атрибуте (имя, отчество, компания, телефоны, e-mail…).
// Состав сравниваемых атрибутов ограничен колонками таблицы addressbook: колонок
// address2, phone2 и notes в ней нет, поэтому они не участвуют ни в модели, ни в сверке.
public class ContactRemovalTests extends TestBase {

    // Удаление одного (случайно выбранного) контакта.
    @Test
    public void canRemoveContact() {
        // Если в БД нет ни одного контакта, создавать «пустышку» через UI не нужно:
        // предусловие готовим напрямую в БД — это быстрее и не зависит от вёрстки.
        if (app.hbm().getContactCount() == 0) {
            // Контакт с осмысленными данными: при удалении видно, что удалился именно он.
            app.hbm().createContact(new ContactData()
                    .withFirstName("removable")
                    .withLastName("contact"));
        }

        // Снимок списка контактов ИЗ БД до удаления (упорядочен по id).
        var oldContacts = contactListFromDb();
        // Генератор случайных чисел.
        var rnd = new Random();
        // Случайный индекс контакта, который будем удалять.
        var index = rnd.nextInt(oldContacts.size());
        // Запоминаем удаляемый контакт: он понадобится для проверки, что удалился именно он.
        var removed = oldContacts.get(index);
        // Удаляем контакт через UI (список уже из БД, поэтому id настоящий).
        app.contacts().removeContact(removed);
        // Снимок списка контактов ИЗ БД после удаления.
        var newContacts = contactListFromDb();

        // Ожидаемый список: копия старого без удалённого элемента.
        var expectedList = sortedById(oldContacts);
        // Удаляем из ожидаемого списка тот же контакт (по id, а не по индексу:
        // после сортировки индексы могли измениться).
        expectedList.removeIf(contact -> contact.id().equals(removed.id()));

        // Полная сверка по всем сравнимым атрибутам: остальные контакты должны остаться
        // неизменными, а удалённого в списке быть не должно. Фото не сравнивается.
        assertContactListEquals("после удаления контакта", expectedList, newContacts);

        // Дополнительная точечная проверка: удалённого контакта действительно нет в БД.
        // Она отдельно фиксирует смысл теста, даже если списки случайно совпали по размеру.
        Assertions.assertTrue(
                newContacts.stream().noneMatch(contact -> contact.id().equals(removed.id())),
                "Удалённый контакт (id=" + removed.id() + ") не должен оставаться в БД");
    }

    // Удаление всех контактов одной операцией.
    @Test
    void canRemoveAllContactsAtOnce() {
        // Предусловие: в БД должен быть хотя бы один контакт, иначе удаление будет «вхолостую».
        if (app.hbm().getContactCount() == 0) {
            // Создаём контакт напрямую в БД.
            app.hbm().createContact(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10)));
        }
        // Снимок списка ИЗ БД до удаления — нужен, чтобы проверить, что удалилось всё.
        var oldContacts = contactListFromDb();
        // Отмечаем все контакты и удаляем одним нажатием через UI.
        app.contacts().removeAllContacts();
        // Снимок списка ИЗ БД после удаления.
        var newContacts = contactListFromDb();

        // В БД не должно остаться контактов: проверяем и длину списка, и счётчик в БД.
        Assertions.assertEquals(0, newContacts.size(),
                "После удаления всех контактов список из БД должен быть пуст, было: " + oldContacts.size());
        // count(*) в БД — независимая проверка того же факта (список мог быть отфильтрован запросом).
        Assertions.assertEquals(0, app.hbm().getContactCount(),
                "После удаления всех контактов таблица addressbook должна быть пуста");
    }
}
