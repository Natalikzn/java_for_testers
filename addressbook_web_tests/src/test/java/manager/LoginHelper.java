package manager;

import org.openqa.selenium.By;

public class LoginHelper extends HelperBase {

    // Передаём manager в родительский конструктор
    public LoginHelper(ApplicationManager manager) {
        super(manager);
    }// Передаём manager в родительский конструктор

    // Метод авторизации
    void login(String user, String password) {
        type(By.name("user"), user);
        type(By.name("pass"), password);
        click(By.xpath("//input[@value=\'Login\']"));
    }
}
