// Пакет tests — тестовые классы.
package tests;

// Модель группы.
import model.GroupData;
// Методы-проверки JUnit.
import org.junit.jupiter.api.Assertions;
// Аннотация обычного теста.
import org.junit.jupiter.api.Test;
// ArrayList — для построения ожидаемого списка.
import java.util.ArrayList;
// Random — случайный выбор группы.
import java.util.Random;

// Тесты удаления групп.
public class GroupRemovalTests extends TestBase {

    // Удаление одной (случайно выбранной) группы.
    @Test
    public void canRemoveGroup() {
        // Открываем страницу групп в UI.
        app.groups().openGroupsPage();
        /*if (app.groups().getCount() == 0) {
            app.groups().createGroup(new GroupData("id", "group name", "group header", "group footer"));
        }*/
        // Если в БД нет групп — создаём одну напрямую, чтобы было что удалять.
        if (app.hbm().getGroupCount() == 0) {
            // Группа с именем/заголовком/подвалом по умолчанию.
            app.hbm().createGroup(new GroupData("", "group name", "group header", "group footer"));
        }
        /*var oldGroups = app.groups().getList();*/
        // Снимок списка групп из БД до удаления.
        var oldGroups = app.hbm().getGroupList();
        // Генератор случайных чисел.
        var rnd = new Random();
        // Случайный индекс группы для удаления.
        var index = rnd.nextInt(oldGroups.size());
        // Удаляем выбранную группу через UI
        app.groups().removeGroup(oldGroups.get(index));
        /*/var newGroups = app.groups().getList();*/
        // Список групп из БД после удаления.
        var newGroups = app.hbm().getGroupList();
        // Копия старого списка — ожидаемый результат.
        var expectedList = new ArrayList<>(oldGroups);
        // Убираем удалённый элемент по индексу.
        expectedList.remove(index);
        // Сравниваем фактический и ожидаемый списки.
        Assertions.assertEquals(newGroups, expectedList);
    }

    // Удаление всех групп сразу.
    @Test
    void canRemoveAllGroupsAtOnce() {
        // Если групп нет — создаём одну, чтобы проверить именно удаление.
        if (app.hbm().getGroupCount() == 0) {
            // Пустая группа (все поля — пустые строки).
            app.hbm().createGroup(new GroupData("", "", "", ""));
        }
        // Отмечаем и удаляем все группы через UI.
        app.groups().removeAllGroups();
        // После удаления в БД не должно остаться групп.
        Assertions.assertEquals(0, app.hbm().getGroupCount());
    }
}


