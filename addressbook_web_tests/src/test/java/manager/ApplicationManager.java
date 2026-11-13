// Пакет manager содержит «менеджеры» и хелперы — слой взаимодействия с приложением.
package manager;

import org.openqa.selenium.By; // By — механизм поиска элементов (локаторы) в Selenium.
import org.openqa.selenium.Dimension; // Dimension — размеры окна браузера.
import org.openqa.selenium.NoSuchElementException; // NoSuchElementException — исключение Selenium, когда элемент не найден.
import org.openqa.selenium.WebDriver; // WebDriver — основной интерфейс управления браузером.
import org.openqa.selenium.chrome.ChromeDriver; // ChromeDriver — реализация WebDriver для Google Chrome.
import org.openqa.selenium.firefox.FirefoxDriver;

import java.time.Duration; // Duration — длительность (для неявных ожиданий).
import java.util.Properties; // Properties — набор пар «ключ=значение» (читается из local.properties)

// ApplicationManager — центральный объект: хранит драйвер и предоставляет хелперы.
public class ApplicationManager {

    protected WebDriver driver; // Ссылка на браузер (доступна наследникам), protected — поле видно классам-наследникам и классам того же пакета (например, хелперам).
    private LoginHelper session; // Хелпер для авторизации, private — снаружи доступен только через метод session().
    private GroupHelper groups; // Хелпер для работы с группами
    private ContactHelper contacts; // Хелпер для работы с контактами
    private Properties properties; // Настройки приложения (URL, логин, пароль) из local.properties.
    private JdbcHelper jdbc; // Хелпер прямых SQL-запросов к БД.
    private HibernateHelper hbm; // Хелпер работы с БД через Hibernate (ORM).

    // Инициализация браузера и запуск приложения, browser — имя браузера ("chrome"/"firefox"), properties — настройки.
    public void init(String browser, Properties properties) {
        this.properties = properties; // Сохраняем настройки в поле, чтобы использовать их дальше
        if (driver == null) { // Инициализируем браузер только один раз (если драйвер ещё не создан).
            if ("firefox".equals(browser)) {
                driver = new FirefoxDriver(); // Запускаем Firefox через Selenium.
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2)); // Неявное ожидание: при поиске элемента драйвер ждёт до 2 секунд.
            } else if ("chrome".equals(browser)) {
                driver = new ChromeDriver();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            } else {
                throw new IllegalArgumentException(String.format("Unknown browser %s", browser));
            }
            Runtime.getRuntime().addShutdownHook(new Thread(driver::quit)); // Регистрируем shutdown hook: при завершении JVM браузер будет закрыт (driver.quit).
            driver.get(properties.getProperty("web.baseUrl")); // Открываем базовый URL приложения из настроек (ключ web.baseUrl).
            driver.manage().window().setSize(new Dimension(1076, 640)); // Задаём фиксированный размер окна — стабильность вёрстки и скриншотов.
            session().login(properties.getProperty("web.username"), properties.getProperty("web.password")); // Выполняем вход, используя логин и пароль из настроек.
        }
    }

    // Ленивая инициализация LoginHelper. «Ленивая» = объект создаётся при первом обращении, затем переиспользуется.
    public LoginHelper session() {
        if (session == null) { // Если хелпер ещё не создан…
            session = new LoginHelper(this); // …создаём его, передавая ссылку на этот менеджер.
        }
        return session; // Возвращаем готовый хелпер.
    }

    // Ленивая инициализация GroupHelper
    public GroupHelper groups() {
        if (groups == null) {
            groups = new GroupHelper(this);
        }
        return groups;
    }

    //Ленивая инициализация ContactHelper
    public ContactHelper contacts() {
        if (contacts == null) {
            contacts = new ContactHelper(this);
        }
        return contacts;
    }

    public JdbcHelper jdbc() {
        if (jdbc == null) {
            jdbc = new JdbcHelper(this);
        }
        return jdbc;
    }

    public HibernateHelper hbm() {
        if (hbm == null) {
            hbm = new HibernateHelper(this);
        }
        return hbm;
    }


    // Проверка наличия элемента на странице (без выброса исключения)
    // protected — доступно хелперам (они в том же пакете) и наследникам.
    protected boolean isElementPresent(By locator) {
        // Пытаемся найти элемент по локатору.
        // findElement бросит NoSuchElementException, если элемента нет.
        try {
            driver.findElement(locator);
            return true; // Элемент найден — возвращаем true.
        } catch (NoSuchElementException exception) {
            return false; // Элемент не найден — это ожидаемая ситуация, возвращаем false.
        }

    }

}
