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
            click(By.linkText("add new"));
        }
    }

    // убедиться, что мы на главной странице
    public void homePage() {
        if (!manager.isElementPresent(By.name("last"))) {
            click(By.linkText("home"));
        }
    }

    // проверить, есть ли хотя бы один контакт
    public boolean isContactPresent() {
        homePage(); // Сначала убедимся, что мы на главной странице
        return manager.isElementPresent(By.name("selected[]")); // Проверяем наличие чекбокса для выбора контакта
    }

    // Создание контакта
    public void createContact(ContactData contact) {
        openCreateContactPage();// Сначала убедимся, что мы на странице добавления контакта
        fillContactForm(contact); // заполняем форму
        submitContactCreation(); // нажать кнопку "Enter"
        homePage();
    }

    // Удаление контакта
    public void removeContact(ContactData contact) { // Удаление контакта
        homePage();
        selectContact(contact);
        removeSelectedContact();
        homePage();
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
        click(By.name("update"));
    }

    private void selectContact(ContactData contact) {
        click(By.cssSelector(String.format("input[value='%s']", contact.id())));
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
        var rows = manager.driver.findElements(By.name("entry")); // собирает все строки контактов
        for (var row : rows) {
            var cells = row.findElements(By.tagName("td")); // ячейки строки: 0 — чекбокс, 1 — Last name, 2 — First name;
            var id = row.findElement(By.name("selected[]")).getAttribute("value");
            // извлечь lastname/firstname из ячеек строки
            var lastname = cells.get(1).getText();
            var firstname = cells.get(2).getText();
            contacts.add(new ContactData().withId(id).withFirstName(firstname).withLastName(lastname));
            //contacts.add(new ContactData().withId(id));
        }
        return contacts;
    }

    public void modifyContact(ContactData contact, ContactData modifiedContact) {
        homePage();
        selectContactById(contact); // выбрать нужный контакт чекбоксом по его id
        initContactModification(); // нажать кнопку модификации "Edit"
        fillContactForm(modifiedContact); // нажать кнопку "Update"
        homePage();
    }

    //Выбор конкретного контакта чекбоксом со значением, равным его id.
    private void selectContactById(ContactData contact) {
        click(By.cssSelector((String.format("input[value = '%s']", contact.id()))));
    }

    // Кнопка «Edit» в списке контактов
    private void initContactModification() {
        click(By.cssSelector("img[title='Edit']"));
    }


}
