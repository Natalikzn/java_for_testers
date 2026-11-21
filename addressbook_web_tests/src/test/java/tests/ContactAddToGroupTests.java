// Пакет tests — тесты, работающие через UI и проверяющие состояние по данным БД.
package tests;

// Утилита генерации случайных строк (нужна для тестовых данных контакта).

import common.CommonFunctions;
// Модель контакта.
import model.ContactData;
// Модель группы.
import model.GroupData;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация теста.
import org.junit.jupiter.api.Test;

// Тесты ДОБАВЛЕНИЯ контакта в группу (задание 15).
//
// Сценарий по заданию, полностью через пользовательский интерфейс:
//   1) на главной странице отметить контакт галочкой;
//   2) справа внизу из выпадающего списка выбрать группу;
//   3) нажать кнопку "Add to".
//
// Все проверки выполняются по информации, загружаемой ИЗ БАЗЫ ДАННЫХ напрямую
// (app.hbm().getContactsInGroup() — состав группы через связь many-to-many таблицы
// address_in_groups), а не по тому, что показывает интерфейс.
//
// Отдельно реализованы проверка и обеспечение предусловий:
//   * предусловие 1: в системе есть хотя бы одна группа;
//   * предусловие 2: есть контакт, который в эту группу ЕЩЁ НЕ входит.
// Если предусловие не выполняется, оно обеспечивается созданием данных (группа — через UI,
// контакт — напрямую в БД), после чего выполняется явная проверка, что предусловие достигнуто.
public class ContactAddToGroupTests extends TestBase {

    // Добавление уже существующего контакта в группу.
    @Test
    void canAddContactToGroup() {
        // ---------- ПРЕДУСЛОВИЕ 1: в системе есть группа ----------
        // ПРОВЕРКА: считаем количество групп напрямую из БД.
        if (app.hbm().getGroupCount() == 0) {
            // ОБЕСПЕЧЕНИЕ: группы нет — создаём её через UI, чтобы не «дорисовывать»
            // состояние, которого приложение не умеет.
            app.groups().createGroup(new GroupData("", "group for add test", "header", "footer"));
        }
        // Повторная ПРОВЕРКА после обеспечения: группа действительно есть в БД.
        var groupsFromDb = app.hbm().getGroupList();
        Assertions.assertFalse(groupsFromDb.isEmpty(),
                "Предусловие не выполнено: в БД должна быть хотя бы одна группа");
        // Целевая группа берётся из ПЕРЕСЕЧЕНИЯ списка БД и списка UI, а не get(0) из БД.
        // Выпадающий список to_group (кнопка "Add to") содержит только активные группы;
        // в БД могут лежать «невидимые» записи (soft-deprecated) — выбор get(0)
        // наткнулся бы на такую группу и selectByValue упал бы, не найдя опцию.
        var visibleGroupIds = app.groups().getList().stream()
                .map(GroupData::id).toList(); // id ровно те же, что в value чекбоксов
        // страницы groups и в опциях to_group
        var targetGroup = groupsFromDb.stream()
                .filter(g -> visibleGroupIds.contains(g.id()))
                .findFirst();
        // Если в БД группы есть, но в UI не видна ни одна (soft-deprecated записи),
        // создаём группу через UI: она заведомо активна и появится и в списке groups,
        // и в выпадающем списке to_group. Новый id определяем сравнением «до/после»,
        // чтобы не зависеть от совпадения названий.
        var group = targetGroup.orElseGet(() -> {
            var idsBefore = app.groups().getList().stream().map(GroupData::id).toList();
            app.groups().createGroup(new GroupData("", "visible group for add test", "header", "footer"));
            return app.groups().getList().stream()
                    .filter(g -> !idsBefore.contains(g.id()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Группа, созданная через UI, не появилась в списке групп"));
        });
        // Явная проверка выбранной группы по БД: она существует и доступна UI.
        //ПЕРЕД проверкой перечитать список групп из БД, чтобы снимок отражал актуальное
        // состояние — включая только что созданную группу. Перечитываем безусловно: в ветке, где
        // группа была найдена среди уже существующих, она никуда не исчезает и проверка остаётся
        // корректной, поэтому отдельного условия не требуется.
        groupsFromDb = app.hbm().getGroupList();
        Assertions.assertTrue(groupsFromDb.stream().anyMatch(g -> g.id().equals(group.id())),
                "Целевая группа должна присутствовать в списке групп БД");

        // ---------- ПРЕДУСЛОВИЕ 2: есть контакт, НЕ входящий в эту группу ----------
        // id контактов, которые УЖЕ состоят в группе (по данным БД).
        var idsInGroup = app.hbm().getContactsInGroup(group).stream().map(ContactData::id).toList();
        // Ищем среди всех контактов БД первого, кого нет в составе группы.
        var candidate = app.hbm().getContactList().stream()
                .filter(contact -> !idsInGroup.contains(contact.id()))
                .findFirst();
        // ПРОВЕРКА/ОБЕСПЕЧЕНИЕ: если такого контакта нет — создаём новый.
        // Создаём напрямую в БД: новый контакт заведомо не связан ни с одной группой,
        // поэтому предусловие гарантированно выполняется без лишней возни с UI.
        var contact = candidate.orElseGet(() -> app.hbm().createContact(new ContactData()
                .withFirstName(CommonFunctions.randomString(10))
                .withLastName(CommonFunctions.randomString(10))
                .withMobile(CommonFunctions.randomString(10))));
        // Итоговая ПРОВЕРКА предусловия: контакт существует и в группе не состоит.
        Assertions.assertFalse(app.hbm().getContactsInGroup(group).contains(contact),
                "Предусловие не выполнено: контакт не должен входить в целевую группу до теста");

        // ---------- ДЕЙСТВИЕ ЧЕРЕЗ UI ----------
        // Снимок состава группы ИЗ БД до действия.
        var oldRelated = app.hbm().getContactsInGroup(group);
        // Отметить контакт галочкой → выбрать группу в выпадающем списке справа внизу → "Add to".
        app.contacts().addContactToGroup(contact, group);
        // Снимок состава группы ИЗ БД после действия.
        var newRelated = app.hbm().getContactsInGroup(group);

        // ---------- ПРОВЕРКИ ПО БАЗЕ ДАННЫХ ----------
        // В группе стал ровно один новый контакт.
        Assertions.assertEquals(oldRelated.size() + 1, newRelated.size(),
                "После добавления в группе должен появиться ровно один контакт");
        // Добавился именно нужный контакт (совпадение по id, equals ContactData — по id).
        Assertions.assertTrue(newRelated.contains(contact),
                "В составе группы должен появиться выбранный контакт");

        // Полная сверка состава группы по всем сравнимым атрибутам (ФИО, компания, телефоны,
        // e-mail…), а не только по количеству: ожидаемый список = прежний состав + добавленный
        // контакт в том же порядке сортировки по id.
        var expectedList = sortedById(oldRelated);
        expectedList.add(contact);
        // Порядок «до/после» может отличаться из-за позиции новой связи, поэтому сверяем
        // отсортированные копии: sortedById внутри assertContactListEquals упорядочивает оба списка.
        assertContactListEquals("после добавления контакта в группу", expectedList, newRelated);

        // Дополнительная сверка с интерфейсом: после применения фильтра по группе тот же
        // контакт виден в таблице — UI и БД согласованы.
        app.contacts().filterByGroup(group);
        Assertions.assertTrue(app.contacts().getDisplayedContactIds().contains(contact.id()),
                "После добавления контакт должен отображаться в отфильтрованном списке группы");
        // Возвращаем список без фильтра, чтобы не влиять на другие тесты.
        app.contacts().showAllContactsOfAllGroups();
    }
}
