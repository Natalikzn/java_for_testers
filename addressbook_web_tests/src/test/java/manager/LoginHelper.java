// Пакет manager — хелперы для работы с приложением.
package manager;

import org.openqa.selenium.By; // By — локаторы Selenium.

// LoginHelper — отвечает только за авторизацию в приложении.
// Наследует click/type из HelperBase.
public class LoginHelper extends HelperBase {

    // Передаём manager в родительский конструктор
    // Конструктор создаёт объект-наследник с доступом к драйверу через базовый класс.
    public LoginHelper(ApplicationManager manager) {
        super(manager);
    }

    // Метод авторизации. user — имя пользователя, password — пароль (берутся из настроек).
    void login(String user, String password) {
        // Вводим логин в поле name="user".
        type(By.name("user"), user);
        // Вводим пароль в поле name="pass".
        type(By.name("pass"), password);
        // Нажимаем кнопку Login (находим по значению value='Login').
        click(By.xpath("//input[@value=\'Login\']"));
    }
}
