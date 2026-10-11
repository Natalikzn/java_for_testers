package manager;

import org.openqa.selenium.By;

public class HelperBase {// Ссылка на главный менеджер
    protected final ApplicationManager manager;

    public HelperBase(ApplicationManager manager) {
        this.manager = manager; // Сохраняем ссылку при создании хелпера
    }

    // Клик по элементу
    protected void click(By locator) {
        manager.driver.findElement(locator).click();
    }

    // Ввод текста в поле: клик, очистка, ввод
    protected void type(By locator, String text) {
        click(locator);
        manager.driver.findElement(locator).clear();
        manager.driver.findElement(locator).sendKeys(text);
    }
}
