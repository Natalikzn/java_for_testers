package manager;

import model.ContactData;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class ContactHelper extends HelperBase {

    public ContactHelper(ApplicationManager manager) {

        super(manager);
    }

    // открываем страницу добавления контакта
    public void openCreateContactPage() {
        if (!manager.isElementPresent(By.name("Last name"))) {
            click(By.linkText("add new")); // кликаем на "Add new"
        }
    }

    // убедиться, что мы на главной странице
    public void homePage() {
        if (!manager.isElementPresent(By.name("last"))) {
            click(By.linkText("home")); // кликаем на "home"
        }
    }

    // проверить, есть ли хотя бы один контакт
    public boolean isContactPresent() {
        homePage();// Сначала убедимся, что мы на главной странице
        return manager.isElementPresent(By.name("selected[]")); // Проверяем наличие чекбокса для выбора контакта
    }

    // Создание контакта
    public void createContact(ContactData contact) {
        openCreateContactPage();// Сначала убедимся, что мы на странице добавления контакта
        fillContactForm(contact); // заполняем форму
        submitContactCreation(); // нажать кнопку "Enter"
        homePage(); // перейти на главную страницу
    }

    public void removeContact() { // Удаление контакта
        homePage(); // перейти на главную страницу
        selectContact();
        removeSelectedContact();
        homePage(); // перейти на главную страницу
    }

    // Кнопка «Enter»
    private void submitContactCreation() {
        click(By.name("submit"));
    }

    // Кнопка создания контакта
    private void initContactCreation() {
        click(By.name("add new"));
    }


    private void removeSelectedContact() {
        click(By.name("delete"));
    }

    private void fillContactForm(ContactData contact) {
        type(By.name("firstname"), contact.firstname());
        type(By.name("lastname"), contact.lastname());
        type(By.name("nickname"), contact.nickname());
    }


    private void selectContact() {
        click(By.name("selected[]"));
    }

    public int getCount() {
        homePage();
        return manager.driver.findElements(By.name("selected[]")).size();
    }

    private void selectAllContacts() {
        var checkboxes = manager.driver.findElements(By.name("selected[]"));
        for (var checkbox : checkboxes) {
            checkbox.click();
        }
    }

    public void removeAllContacts() {
        homePage();
        selectAllContacts();
        removeSelectedContact();
        homePage();
    }

    public List<ContactData> getList() {
        homePage();
        var contacts = new ArrayList<ContactData>();
        var rows = manager.driver.findElements(By.cssSelector("table#maintable tr td.center:nth-child(2)"));
        for (var row : rows) {
            var id = row.findElement(By.name("selected[]")).getAttribute("value");
            // извлечь lastname/firstname из ячеек строки
            contacts.add(new ContactData().withId(id));
        }
        return contacts;
    }

}
