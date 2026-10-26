package manager;

import model.GroupData;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class GroupHelper extends HelperBase {

    public GroupHelper(ApplicationManager manager) {

        super(manager);
    }

    public void openGroupsPage() {
        if (!manager.isElementPresent(By.name("new"))) {
            click(By.linkText("groups"));
        }
    }

    public void createGroup(GroupData group) { // Создание группы
        openGroupsPage(); // открыть страницу с группами
        initGroupCreation(); // нажать кнопку создания группы "New group"
        //selectGroup();
        fillGroupForm(group); // заполняем форму
        submitGroupCreation(); // нажать кнопку "Enter information"
        //group.setId(id);
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    public void removeGroup(GroupData group) { // Удаление группы
        openGroupsPage(); // открыть страницу с группами
        selectGroup(group);
        removeSelectedGroups();
        returnToGroupsPage();
    }

    public void modifyGroup(GroupData group, GroupData modifiedGroup) { // Изменение группы
        openGroupsPage(); // открыть страницу с группами
        selectGroup(group); // выбрать группу
        initGroupModification(); // нажать кнопку модификации "Edit group"
        fillGroupForm(modifiedGroup); // заполняем форму
        submitGroupModification(); // нажать кнопку Update
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    private void submitGroupCreation() {
        click(By.name("submit"));
    }


    private void initGroupCreation() {
        click(By.name("new"));
    }


    private void removeSelectedGroups() {
        click(By.name("delete"));
    }

    private void returnToGroupsPage() {
        click(By.linkText("group page"));
    }

    private void submitGroupModification() {
        click(By.name("update"));
    }

    private void fillGroupForm(GroupData group) {
        type(By.name("group_name"), group.name());
        type(By.name("group_header"), group.header());
        type(By.name("group_footer"), group.footer());
    }


    private void initGroupModification() {
        click(By.name("edit"));
    }

    private void selectGroup(GroupData group) {
        click(By.cssSelector(String.format("input[value='%s']", group.id())));
    }

    public int getCount() {
        openGroupsPage();
        return manager.driver.findElements(By.name("selected[]")).size();
    }

    public void removeAllGroups() {
        openGroupsPage();
        selectAllGroups();
        removeSelectedGroups();
    }

    private void selectAllGroups() {
        var checkboxes = manager.driver.findElements(By.name("selected[]"));
        for (var checkbox : checkboxes) {
            checkbox.click();
        }
    }

    public List<GroupData> getList() {
        openGroupsPage(); // открыть страницу с группами
        var groups = new ArrayList<GroupData>();
        var spans = manager.driver.findElements(By.cssSelector("span.group"));
        for (var span : spans) {
            var name = span.getText();
            var checkbox = span.findElement(By.name("selected[]"));
            var id = checkbox.getAttribute("value");
            groups.add(new GroupData().withId(id).withName(name));
        }
        return groups;
    }
}
