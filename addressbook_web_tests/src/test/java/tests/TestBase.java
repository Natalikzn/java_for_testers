package tests;

import manager.ApplicationManager;
import org.junit.jupiter.api.BeforeEach;

public class TestBase {

    protected static ApplicationManager app;


    @BeforeEach
    public void setUp() {
        if (app == null) {
            // Создаём менеджер, если его ещё нет
            app = new ApplicationManager();
        }
// Получаем имя браузера из системных свойств (или по умолчанию — firefox)
        app.init(System.getProperty("browser", "firefox"));

    }
}
