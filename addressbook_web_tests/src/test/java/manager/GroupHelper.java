package manager;

import model.GroupData;
import org.openqa.selenium.By;

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
        returnToGroupsPage(); // вернуться на страницу со списком групп
    }

    public void removeGroup() { // Удаление группы
        openGroupsPage(); // открыть страницу с группами
        selectGroup();
        removeSelectedGroups();
        returnToGroupsPage();
    }

    public void modifyGroup(GroupData modifiedGroup) { // Изменение группы
        openGroupsPage(); // открыть страницу с группами
        selectGroup(); // выбрать группу
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

    private void selectGroup() {
        click(By.name("selected[]"));
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
}
