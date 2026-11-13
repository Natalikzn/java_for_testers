// Пакет manager: хелперы для взаимодействия с приложением.
package manager;

import model.ContactData; // Модель контакта (record) — данные, которые вводим или читаем со страницы.
import model.GroupData; // Модель группы — нужна для привязки контакта к группе.
import org.openqa.selenium.By; // By — локаторы Selenium.
import org.openqa.selenium.support.ui.Select; // Select — обёртка для работы с выпадающими списками (<select>).

import java.util.ArrayList; // ArrayList — изменяемый список результатов.
import java.util.List; // List — интерфейс списка.

// ContactHelper — операции над контактами (создание, удаление, изменение, чтение списка).
// Наследуется от HelperBase и получает методы click/type/attach и ссылку на manager.
public class ContactHelper extends HelperBase {

    public ContactHelper(ApplicationManager manager) {
        super(manager);
    }

    // открываем страницу добавления контакта
    public void openCreateContactPage() {
        if (!manager.isElementPresent(By.name("lastname"))) { // Если поля lastname нет, значит страница добавления не открыта…
            click(By.linkText("add new")); // …кликаем по ссылке "add new", ведущей на форму создания контакта.
        }
    }
    // убедиться, что мы на главной странице
    public void homePage() {
        if (!manager.isElementPresent(By.xpath("//h1[.='Home']"))) { // Проверяем наличие заголовка h1 с текстом "Home" — признак главной страницы.
            click(By.linkText("home"));
        }
    }
    // проверить, есть ли хотя бы один контакт
    public boolean isContactPresent() {
        homePage(); // Сначала убедимся, что мы на главной странице
        return manager.isElementPresent(By.name("selected[]")); // Проверяем наличие чекбокса для выбора контакта/ Наличие хотя бы одного чекбокса selected[] означает, что в таблице есть контакт.
    }

    // Создание контакта
    public void createContact(ContactData contact) {
        openCreateContactPage();  // Сначала убедимся, что мы на странице добавления контакта
        fillContactForm(contact); // заполняем форму
        submitContactCreation(); // Отправляем форму
        homePage(); // Возвращаемся на главную страницу c списком контактов.
    }

    // Создание контакта и включение его в группу из выпадающего списка
    public void createContact(ContactData contact, GroupData group) {
        openCreateContactPage();
        fillContactForm(contact); // заполняем форму
        selectGroup(group); // выбирает группу из выпадающего списка
        submitContactCreation(); // Отправляем форму
        homePage();
    }

    // Приватный шаг: выбор группы в выпадающем списке формы.
    private void selectGroup(GroupData group) {
        new Select(manager.driver.findElement(By.name("new_group")))// Находим <select name="new_group"> и оборачиваем в Select;
                .selectByValue(group.id()); // selectByValue(group.id()) выбирает пункт, у которого value равен id группы.
    }

    // Удаление контакта
    public void removeContact(ContactData contact) {
        homePage();
        selectContact(contact);
        removeSelectedContact();
        homePage();
    }

    // Клик по кнопке name="submit" — отправка формы создания контакта.
    private void submitContactCreation() {
        click(By.name("submit"));
    }

    // Клик по кнопке name="add new" — открытие формы нового контакта.
    private void initContactCreation() {
        click(By.name("add new"));
    }

    // Кнопка удаления отмеченных контактов.
    private void removeSelectedContact() {
        click(By.name("delete"));
    }

    // Клик по кнопке name="update" — сохранение изменений контакта
    private void submitContactModification() {
        click(By.name("update"));
    }

    // Заполнение формы контакта значениями из модели.
    private void fillContactForm(ContactData contact) {
        type(By.name("firstname"), contact.firstname());
        type(By.name("lastname"), contact.lastname());
        type(By.name("nickname"), contact.nickname());
        attach(By.name("photo"), contact.photo()); // Прикрепляем фото: если путь пустой/null, attach просто ничего не делает.
        /*click(By.name("update"));*/
    }

    // Отметить контакт чекбоксом по его id.
    private void selectContact(ContactData contact) {
        click(By.cssSelector(String.format("input[value='%s']", contact.id()))); // CSS-локатор ищет input, у которого значение value равно id контакта, и кликает по нему.
    }

    // Количество контактов на странице.
    public int getCount() {
        homePage();
        return manager.driver.findElements(By.name("selected[]")).size(); // Находим все чекбоксы selected[] — их число равно количеству контактов в таблице.
    }

    // Отметить все контакты чекбоксами.
    private void selectAllContacts() {
        var checkboxes = manager.driver.findElements(By.name("selected[]")); // Получаем список всех чекбоксов контактов.
        for (var checkbox : checkboxes) { // Проходим по каждому чекбоксу…
            checkbox.click(); // …и кликаем, отмечая контакт.
        }
    }

    // Удалить сразу все контакты.
    public void removeAllContacts() {
        homePage();
        if (manager.driver.findElements(By.name("selected[]")).isEmpty()) {
            return; // Если чекбоксов нет, значит контактов нет — выходим, ничего не удаляя.
        }
        selectAllContacts();
        removeSelectedContact();
        homePage();
    }

    // Получить список контактов (id + имя + фамилия) со страницы.
    public List<ContactData> getList() {
        homePage();
        var contacts = new ArrayList<ContactData>(); // Аккумулятор результата.
        var rows = manager.driver.findElements(By.name("entry")); // собирает все строки контактов. У строк таблицы контактов атрибут name="entry".
        for (var row : rows) { // Обходим каждую строку.
            // ячейки строки: 0 — чекбокс, 1 — Last name, 2 — First name;
            var cells = row.findElements(By.tagName("td")); // Находим все td внутри строки.
            var id = row.findElement(By.name("selected[]")).getAttribute("value"); // Достаём id из атрибута value чекбокса в этой строке.
            // извлечь lastname/firstname из ячеек строки
            var lastname = cells.get(1).getText(); // Текст второй ячейки — фамилия.
            var firstname = cells.get(2).getText(); // Текст третьей ячейки — имя.
            contacts.add(new ContactData().withId(id).withFirstName(firstname).withLastName(lastname)); // Собираем объект ContactData через билдеры и добавляем в список.
            /*contacts.add(new ContactData().withId(id));*/
        }
        return contacts;
    }

    // Изменение контакта: contact — какой менять, modifiedContact — новые данные.
    public void modifyContact(ContactData contact, ContactData modifiedContact) {
        homePage();
        initContactModification(contact); // клик по «карандашику» в строке нужного контакта
        fillContactForm(modifiedContact); // заполняем форму новыми данными
        submitContactModification(); // нажать кнопку "Update"
        homePage();
    }

    //Выбор конкретного контакта чекбоксом со значением, равным его id.
    /*private void selectContactById(ContactData contact) {
        click(By.cssSelector((String.format("input[value = '%s']", contact.id()))));
    }*/

    // Открыть форму редактирования конкретного контакта.
    private void initContactModification(ContactData contact) {
        // XPath находит строку tr с name='entry', внутри которой есть чекбокс selected[] с нужным id.
        var row = manager.driver.findElement(By.xpath(
                String.format("//tr[@name='entry'][.//input[@name='selected[]' and @value='%s']]", contact.id())));
        // Кликаем по «карандашику» уже внутри найденной строки. Так открывается форма редактирования нужного контакта.
        row.findElement(By.cssSelector("img[title='Edit']")).click(); // Кликаем по «карандашику» уже внутри найденной строки. Так открывается форма редактирования нужного контакта.
    }
}
