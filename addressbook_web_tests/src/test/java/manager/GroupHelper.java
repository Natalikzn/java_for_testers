// Пакет manager — слой, инкапсулирующий работу с приложением.
package manager;


import model.GroupData; // Модель группы — данные для создания/изменения/сравнения.
import org.openqa.selenium.By; // By — локаторы элементов Selenium.

import java.util.ArrayList; // ArrayList — изменяемый список для сбора групп.
import java.util.List; // List — интерфейс списка.

// GroupHelper — операции над группами контактов.
// Наследует вспомогательные методы (click/type) из HelperBase.
public class GroupHelper extends HelperBase {
    // Конструктор: принимает менеджер приложения.
    public GroupHelper(ApplicationManager manager) {
        super(manager); // Передаём ссылку на менеджер в базовый класс.
    }

    // Открыть страницу со списком групп.
    public void openGroupsPage() {
        if (!manager.isElementPresent(By.name("new"))) { // Если на странице нет кнопки name="new", значит страница групп не открыта…
            click(By.linkText("groups")); // …переходим по ссылке "groups".
        }
    }

    // Создание группы
    public void createGroup(GroupData group) {
        openGroupsPage(); // открыть страницу с группами
        initGroupCreation(); // нажать кнопку создания группы "New group"
        /*selectGroup();*/
        fillGroupForm(group); // заполняем форму
        submitGroupCreation(); // нажать кнопку "Enter information"
        /*group.setId(id);*/
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    // Удаление группы
    public void removeGroup(GroupData group) {
        openGroupsPage();
        selectGroup(group); // Отметить чекбокс нужной группы (по её id).
        removeSelectedGroups(); // Нажать кнопку удаления.
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    // Изменение группы
    public void modifyGroup(GroupData group, GroupData modifiedGroup) {
        openGroupsPage();
        selectGroup(group);
        initGroupModification(); // нажать кнопку модификации "Edit group"
        fillGroupForm(modifiedGroup); // заполняем форму
        submitGroupModification(); // нажать кнопку Update
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    // Подтвердить создание группы (кнопка submit).
    private void submitGroupCreation() {
        click(By.name("submit"));
    }

    // Открыть форму создания новой группы.
    private void initGroupCreation() {
        click(By.name("new")); // Клик по кнопке name="new" («New group»).
    }

    // Удалить отмеченные группы.
    private void removeSelectedGroups() {
        click(By.name("delete"));
    }

    // Вернуться к странице со списком групп.
    private void returnToGroupsPage() {
        click(By.linkText("group page"));
    }

    // Сохранить изменения группы (кнопка update).
    private void submitGroupModification() {
        click(By.name("update"));
    }

    // Заполнить форму группы значениями из модели.
    private void fillGroupForm(GroupData group) {
        type(By.name("group_name"), group.name());
        type(By.name("group_header"), group.header());
        type(By.name("group_footer"), group.footer());
    }


    // Нажать кнопку редактирования выбранной группы
    private void initGroupModification() {
        click(By.name("edit"));
    }

    // Отметить группу чекбоксом по её id.
    private void selectGroup(GroupData group) {
        click(By.cssSelector(String.format("input[value='%s']", group.id()))); // CSS-локатор: input, у которого value = id группы.
    }

    // Количество групп на странице.
    public int getCount() {
        openGroupsPage();
        return manager.driver.findElements(By.name("selected[]")).size(); // Количество чекбоксов selected[] = количество групп.
    }

    // Удалить все группы сразу.
    public void removeAllGroups() {
        openGroupsPage();
        selectAllGroups();
        removeSelectedGroups();
    }

    // Отметить все группы чекбоксами.
    private void selectAllGroups() {
        var checkboxes = manager.driver.findElements(By.name("selected[]")); // Получаем все чекбоксы групп.
        for (var checkbox : checkboxes) { // Для каждого чекбокса…
            checkbox.click(); // …выполняем клик, отмечая группу.
        }
    }

    // Получить список групп (id + название) со страницы.
    public List<GroupData> getList() {
        openGroupsPage();
        var groups = new ArrayList<GroupData>(); // Аккумулятор результата.
        var spans = manager.driver.findElements(By.cssSelector("span.group")); // Находим все элементы span с классом group — по одному на группу.
        for (var span : spans) { // Обходим найденные элементы.
            var name = span.getText(); // Текст span — название группы.
            var checkbox = span.findElement(By.name("selected[]")); // Внутри span находим чекбокс выбора группы.
            var id = checkbox.getAttribute("value"); // id группы хранится в атрибуте value этого чекбокса.
            groups.add(new GroupData().withId(id).withName(name)); // Собираем GroupData (id + name) и добавляем в список.
        }
        return groups; // Возвращаем список групп.
    }
}
