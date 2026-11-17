// Пакет tests — тестовые классы.
package tests;

// Модель контакта.
import model.ContactData;
// Утилита случайных строк для подготовки данных и модификации.
import common.CommonFunctions;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация обычного теста.
import org.junit.jupiter.api.Test;

import java.util.ArrayList; // ArrayList — копия списка под ожидаемый результат.
import java.util.Random; // Random — случайный выбор контакта для модификации.

// Тесты модификации контактов.
//
// Задание 14: все проверки делаются по спискам, загруженным ИЗ БАЗЫ ДАННЫХ
// (contactListFromDb() в TestBase). Поэтому после изменения контакта сравниваются
// не только количество и порядок, но и все сравнимые атрибуты: изменённый контакт
// должен получить новые значения, а все остальные контакты — остаться нетронутыми.
// Атрибуты, невидимые в списке контактов UI (отчество, должность, компания, телефоны,
// e-mail), тоже участвуют в сравнении. Фото — не участвует (blob).
// В таблице addressbook нет колонок address2, phone2 и notes, поэтому такие поля
// в данных и сравнении не участвуют.
public class ContactModificationTests extends TestBase {

    // Проверка предусловия: в БД должно быть не меньше 5 контактов.
    @Test
    void canPrepareContactsForModification() {
        // Если контактов меньше пяти — готовим данные напрямую в БД.
        if (app.hbm().getContactCount() < 5) {
            // Сколько контактов не хватает до пяти.
            var missing = 5 - (int) app.hbm().getContactCount();
            for (int i = 0; i < missing; i++) {
                // Каждый контакт — с заполненными «видимыми» и «невидимыми» в UI полями.
                app.hbm().createContact(new ContactData()
                        .withFirstName(CommonFunctions.randomString(10))
                        .withMiddleName(CommonFunctions.randomString(10))
                        .withLastName(CommonFunctions.randomString(10))
                        .withNickname(CommonFunctions.randomString(10))
                        .withCompany(CommonFunctions.randomString(10))
                        .withMobile(CommonFunctions.randomString(10)));
            }
        }
        // Проверяем по данным БД, что контактов действительно не меньше пяти.
        Assertions.assertTrue(app.hbm().getContactCount() >= 5,
                "В БД должно быть не меньше 5 контактов для теста модификации");
    }

    // Модификация не должна менять количество контактов; изменяется только выбранный контакт.
    @Test
    void canModifyContact() {
        // Предусловие: в БД есть хотя бы один контакт.
        if (app.hbm().getContactCount() == 0) {
            // Создаём контакт напрямую в БД.
            app.hbm().createContact(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10)));
        }

        // Снимок списка контактов ИЗ БД до изменения (упорядочен по id).
        var oldContacts = contactListFromDb();
        // Генератор случайных чисел.
        var rnd = new Random();
        // Случайный индекс контакта, который будем менять.
        var index = rnd.nextInt(oldContacts.size());
        // Контакт, который изменяем (id нужен для поиска в UI и для ожидаемого списка).
        var modified = oldContacts.get(index);

        // Новые данные контакта: заполняем и «видимые», и «невидимые» в списке UI атрибуты,
        // чтобы проверка из БД действительно ловила изменение каждого поля.
        // Заполняются только колонки, которые реально есть в таблице addressbook
        // (без address2, phone2 и notes).
        var testData = new ContactData()
                .withFirstName("modified firstname")
                .withMiddleName("modified middlename")
                .withLastName("modified lastname")
                .withNickname("modified nickname")
                .withTitle("modified title")
                .withCompany("modified company")
                .withAddress("modified address")
                .withHome("111-11-11")
                .withMobile("222-22-22")
                .withWork("333-33-33")
                .withFax("444-44-44")
                .withEmail("modified1@example.com")
                .withEmail2("modified2@example.com")
                .withEmail3("modified3@example.com")
                .withHomepage("https://modified.example.com");

        // Изменяем контакт через UI: какой меняем и на что.
        app.contacts().modifyContact(modified, testData);

        // Снимок списка контактов ИЗ БД после изменения.
        var newContacts = contactListFromDb();

        // Количество контактов измениться не должно.
        Assertions.assertEquals(oldContacts.size(), newContacts.size(),
                "Модификация контакта не должна менять количество контактов в БД");

        // Ожидаемый список: копия старого, где выбранный контакт заменён новыми данными.
        var expectedList = new ArrayList<>(sortedById(oldContacts));
        // Находим позицию изменяемого контакта в упорядоченном списке по id
        // (индекс из исходного списка после сортировки может быть другим).
        var position = indexOfId(expectedList, modified.id());
        // Заменяем элемент, сохраняя его id: id при модификации не меняется.
        expectedList.set(position, testData.withId(modified.id()));

        // Полная сверка по всем сравнимым атрибутам:
        //   * у изменённого контакта должны появиться новые значения всех полей;
        //   * у остальных контактов не должно измениться ничего.
        assertContactListEquals("после модификации контакта", expectedList, newContacts);
    }

    // Модификация должна менять только выбранный контакт, остальные остаются прежними.
    // Тест проверяет именно «соседей» изменённой записи — частая ошибка, когда
    // приложение затирает данные другого контакта.
    @Test
    void modificationDoesNotAffectOtherContacts() {
        // Предусловие: нужно минимум два контакта, чтобы был «другой» контакт.
        while (app.hbm().getContactCount() < 2) {
            app.hbm().createContact(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10)));
        }

        // Снимок списка ИЗ БД до изменения.
        var oldContacts = contactListFromDb();
        // Берём первый контакт для изменения.
        var modified = oldContacts.get(0);
        // Новые данные только для него.
        var testData = new ContactData()
                .withFirstName("only this contact changed")
                .withLastName("only this contact changed")
                .withEmail("changed@example.com");

        // Изменяем один контакт через UI.
        app.contacts().modifyContact(modified, testData);

        // Снимок списка ИЗ БД после изменения.
        var newContacts = contactListFromDb();

        // Все контакты, кроме изменённого, должны остаться точно такими же.
        var otherOld = oldContacts.stream().filter(c -> !c.id().equals(modified.id())).toList();
        var otherNew = newContacts.stream().filter(c -> !c.id().equals(modified.id())).toList();
        // Сверка «соседей» по всем сравнимым атрибутам.
        assertContactListEquals("остальные контакты после модификации", otherOld, otherNew);
    }

    // Индекс контакта с заданным id в списке (списки небольшие, линейный поиск достаточен).
    private static int indexOfId(java.util.List<ContactData> contacts, String id) {
        for (int i = 0; i < contacts.size(); i++) {
            // Сравниваем id как строки: в модели id хранится строкой.
            if (contacts.get(i).id().equals(id)) {
                return i;
            }
        }
        // Контакт обязан быть в списке; если его нет — это ошибка теста, а не приложения.
        throw new IllegalStateException("Контакт с id=" + id + " не найден в списке из БД");
    }
}
