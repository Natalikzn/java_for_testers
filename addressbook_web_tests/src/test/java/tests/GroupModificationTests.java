// Пакет tests — тестовые классы.
package tests;

import model.GroupData; // Модель группы.
import org.junit.jupiter.api.Assertions; // Методы-проверки JUnit.
import org.junit.jupiter.api.Test; // Аннотация обычного теста.

import java.util.ArrayList; // ArrayList — для построения ожидаемого списка.
import java.util.Comparator; // Comparator — сортировка по id.
import java.util.Random; // Random — случайный выбор группы.

// Тесты модификации групп.
public class GroupModificationTests extends TestBase {

    // Изменение случайно выбранной группы.
    @Test
    void canModifyGroup() {
        // Если в БД нет ни одной группы…
        if (app.hbm().getGroupCount() == 0) {
            // …создаём пустую группу напрямую через Hibernate, чтобы было что менять
            app.hbm().createGroup(new GroupData("", "", "", ""));
        }
        // Снимок списка групп до изменения.
        var oldGroups = app.hbm().getGroupList();
        // Генератор случайных чисел.
        var rnd = new Random();
        // Случайный индекс группы в списке.
        var index = rnd.nextInt(oldGroups.size());
        // Новые данные: только имя изменяется, остальные поля останутся пустыми.
        var testData = new GroupData().withName("modified name");
        // Изменяем группу через UI: какую меняем и на что.
        app.groups().modifyGroup(oldGroups.get(index), testData);
        // Снимок списка групп после изменения.
        var newGroups = app.hbm().getGroupList();
        // Копия старого списка — основа ожидаемого результата.
        var expectedList = new ArrayList<>(oldGroups);
        // В ожидаемом списке заменяем элемент по тому же индексу на testData с прежним id.
        expectedList.set(index, testData.withId((oldGroups.get(index).id())));
        // Компаратор по числовому id: сортировка нужна, чтобы сравнение не зависело от порядка выдачи.
        Comparator<GroupData> compareById = (o1, o2) -> {
            // id в модели — строка, поэтому приводим к int.
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        // Сортируем фактический список.
        newGroups.sort(compareById);
        // Сортируем ожидаемый список.
        expectedList.sort(compareById);
        // Сравниваем списки: assertEquals у record сравнивает все компоненты (id, name, header, footer).
        Assertions.assertEquals(newGroups, expectedList);
    }
}