// Пакет tests — тесты, работающие через UI и проверяющие состояние по данным БД.
package tests;

// Утилита генерации случайных строк (нужна для тестовых данных контакта).

import common.CommonFunctions;
import model.ContactData;
import model.GroupData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

// Модель контакта.
// Модель группы.
// Методы-проверки JUnit.
// Аннотация теста.

// Тесты УДАЛЕНИЯ контакта из группы (задание 15).
//
// Сценарий по заданию, полностью через пользовательский интерфейс:
//   1) на главной странице справа вверху выбрать группу — останутся только её контакты;
//   2) отметить нужный контакт галочкой;
//   3) нажать кнопку "Remove from".
//
// Проверки — по данным, загружаемым ИЗ БАЗЫ ДАННЫХ напрямую:
//   * состав группы — app.hbm().getContactsInGroup(group) (связь many-to-many address_in_groups);
//   * сам контакт по-прежнему присутствует в таблице addressbook — удаляется СВЯЗЬ, а не контакт.
//
// Обеспечение предусловия: чтобы удалять контакт из группы, контакт должен в эту группу входить.
// Связь создаётся напрямую в БД (app.hbm().assignContactToGroup), а не через UI-добавление,
// иначе предусловие опиралось бы на тот же UI-механизм, который проверяется рядом других тестов.
public class ContactRemoveFromGroupTests extends TestBase {

    // Удаление контакта из группы.
    @Test
    void canRemoveContactFromGroup() {
        // ---------- ПРЕДУСЛОВИЕ 1: в системе есть группа ----------
        // ПРОВЕРКА по БД.
        // Проверка наличия именно UI-видимой группы вместо getGroupCount() (считает и призрачные группы)
        if (app.hbm().getVisibleGroupList().isEmpty()) {
            // ОБЕСПЕЧЕНИЕ: создаём группу через UI.
            app.groups().createGroup(new GroupData("", "group for remove test", "header", "footer"));
        }
        /*var groupsFromDb = app.hbm().getGroupList();
        Assertions.assertFalse(groupsFromDb.isEmpty(),
                "Предусловие не выполнено: в БД должна быть хотя бы одна группа");
        var group = groupsFromDb.get(0);*/
        // Источник выбора группы = только UI-видимые, иначе INSERT-связь на призрачной группе не видна в getContactsInGroup и assert:83 падает
        var groupsFromDb = app.hbm().getVisibleGroupList();
        Assertions.assertFalse(groupsFromDb.isEmpty(),
                "Предусловие не выполнено: в БД должна быть хотя бы одна группа");
        // Целевая группа — первая непустая по составу из БД. get(0) вслепую ненадёжен:
        // в списке могут быть группы, невидимые в UI (soft-deprecated записи), и тогда
        // фильтр по группе не показал бы контакт и кнопка "Remove from" не сработала бы.
        var group = groupsFromDb.stream()
                .filter(g -> !app.hbm().getContactsInGroup(g).isEmpty())
                .findFirst()
                .orElse(groupsFromDb.get(0));
        //groupsFromDb.stream().filter(g -> !app.hbm().getContactsInGroup(g).isEmpty()).findFirst().orElse(groupsFromDb.get(0));
        /*Assertions.assertTrue(app.hbm().getContactsInGroup(group).contains(created),
                "Связь контакт-группа не создалась в БД при обеспечении предусловия");*/
        // ---------- ПРЕДУСЛОВИЕ 2: в группе есть хотя бы один контакт ----------
        // ПРОВЕРКА по БД.
        /*if (app.hbm().getContactsInGroup(group).isEmpty()) {
            // ОБЕСПЕЧЕНИЕ: создаём контакт напрямую в БД и включаем его в группу тоже напрямую.
            var created = app.hbm().createContact(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10))
                    .withCompany(CommonFunctions.randomString(10))
                    .withMobile(CommonFunctions.randomString(10))
                    .withEmail(CommonFunctions.randomString(5) + "@example.com"));
            app.hbm().assignContactToGroup(created, group);
        }*/
        if (app.hbm().getContactsInGroup(group).isEmpty()) {
            // ОБЕСПЕЧЕНИЕ: создаём контакт напрямую в БД и включаем его в группу тоже напрямую.
            var created = app.hbm().createContact(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10))
                    .withCompany(CommonFunctions.randomString(10))
                    .withMobile(CommonFunctions.randomString(10))
                    .withEmail(CommonFunctions.randomString(5) + "@example.com"));
            app.hbm().assignContactToGroup(created, group);
            // Немедленная проверка, что связь реально записалась в address_in_groups.
            // Без неё сбой записи связи был бы виден только ниже, как «предусловие
            // не выполнено», и его причина (сбой БД vs пустая группа) осталась бы неизвестной.
            Assertions.assertTrue(app.hbm().getContactsInGroup(group).contains(created),
                    "Связь контакт-группа не создалась в БД при обеспечении предусловия");
        }
        // Итоговая ПРОВЕРКА предусловия: группа непустая — есть что удалять.
        var membersBefore = app.hbm().getContactsInGroup(group);
        Assertions.assertFalse(membersBefore.isEmpty(),
                "Предусловие не выполнено: в группе должен быть хотя бы один контакт");
        // Удаляем первый контакт состава, полученного из БД.
        var contact = membersBefore.get(0);

        // ---------- ДЕЙСТВИЕ ЧЕРЕЗ UI ----------
        // Применяем фильтр по группе: в таблице остаются только контакты этой группы,
        // поэтому отмеченный контакт виден и доступен для удаления именно из неё.
        app.contacts().filterByGroup(group);
        // Отметить контакт галочкой → нажать кнопку "Remove from".
        try {
            app.contacts().removeContactFromGroup(contact);
        } catch (org.openqa.selenium.TimeoutException e) {
            app.contacts().homePage();
        }

        // ---------- ПРОВЕРКИ ПО БАЗЕ ДАННЫХ ----------
        // Состав группы ПОСЛЕ удаления.
        var membersAfter = app.hbm().getContactsInGroup(group);
        // Контакт больше не входит в группу.
        Assertions.assertFalse(membersAfter.contains(contact),
                "Контакт должен быть удалён из состава группы");
        // Ровно на одного участника меньше: лишние связи не удалились.
        Assertions.assertEquals(membersBefore.size() - 1, membersAfter.size(),
                "Из группы должен быть удалён ровно один контакт");
        // Удаление из группы НЕ удаляет сам контакт: он по-прежнему есть в таблице addressbook.
        Assertions.assertTrue(app.hbm().getContactList().contains(contact),
                "Сам контакт должен остаться в БД: из группы удаляется только связь");
        // Остальные участники группы не пострадали: сверяем их состав по всем атрибутам.
        var expectedRemaining = sortedById(membersBefore);
        expectedRemaining.remove(contact);
        assertContactListEquals("после удаления контакта из группы", expectedRemaining, membersAfter);

        // Дополнительная сверка с интерфейсом: в том же фильтре по группе контакт больше не виден.
        app.contacts().filterByGroup(group);
        Assertions.assertFalse(app.contacts().getDisplayedContactIds().contains(contact.id()),
                "Удалённый из группы контакт не должен отображаться в отфильтрованном списке");
        // Возвращаем список без фильтра, чтобы не влиять на другие тесты.
        app.contacts().showAllContactsOfAllGroups();
    }
}
