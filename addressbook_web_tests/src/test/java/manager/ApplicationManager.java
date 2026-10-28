package manager;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.time.Duration;

public class ApplicationManager {

    protected WebDriver driver; // Ссылка на браузер (доступна наследникам)
    private LoginHelper session; // Хелпер для авторизации
    private GroupHelper groups; // Хелпер для работы с группами
    private ContactHelper contacts; // Хелпер для работы с контактами


    public void init(String browser) { // Инициализация браузера и запуск приложения
        if (driver == null) {
            if ("firefox".equals(browser)) {
                driver = new FirefoxDriver();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            } else if ("chrome".equals(browser)) {
                driver = new ChromeDriver();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            } else {
                throw new IllegalArgumentException(String.format("Unknown browser %s", browser));
            }
            Runtime.getRuntime().addShutdownHook(new Thread(driver::quit));
            driver.get("http://localhost/addressbook/");
            driver.manage().window().setSize(new Dimension(1076, 640));
            session().login("admin", "secret");
        }
    }

    public LoginHelper session() { // Ленивая инициализация LoginHelper
        if (session == null) {
            session = new LoginHelper(this);
        }
        return session;
    }

    public GroupHelper groups() { // Ленивая инициализация GroupHelper
        if (groups == null) {
            groups = new GroupHelper(this);
        }
        return groups;
    }

    public ContactHelper contacts() {// Ленивая инициализация ContactHelper
        if (contacts == null) {
            contacts = new ContactHelper(this);
        }
        return contacts;
    }

    protected boolean isElementPresent(By locator) { // Проверка наличия элемента на странице (без выброса исключения)
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException exception) {
            return false;
        }

    }

}
