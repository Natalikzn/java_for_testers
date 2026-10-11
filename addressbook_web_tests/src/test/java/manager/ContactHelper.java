package manager;

import model.ContactData;
import org.openqa.selenium.By;

public class ContactHelper extends HelperBase {

    public ContactHelper(ApplicationManager manager) {

        super(manager);
    }

    // открываем страницу добавления контакта
    public void openCreateContactPage() {
        if (!manager.isElementPresent(By.name("new"))) {
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
    }


    private void selectContact() {
        click(By.name("selected[]"));
    }

}
