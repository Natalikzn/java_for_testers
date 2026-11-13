// Пакет manager — вспомогательный слой для работы с UI.
package manager;

import org.openqa.selenium.By; // By — тип локатора Selenium (name, xpath, css и т.д.).

import java.nio.file.Paths; // Paths — утилита для построения путей к файлам.

// Базовый класс для всех хелперов: хранит ссылку на менеджер и общие действия с элементами.
public class HelperBase {
    // Ссылка на главный менеджер
    // protected — доступна наследникам (GroupHelper, ContactHelper и др.);
    // final — ссылка не может быть переприсвоена после конструктора.
    protected final ApplicationManager manager;

    // Конструктор: каждый хелпер получает менеджер при создании.
    public HelperBase(ApplicationManager manager) {
        this.manager = manager; // Сохраняем ссылку при создании хелпера
    }

    // Клик по элементу. locator — стратегия поиска; метод находит элемент и кликает по нему.
    protected void click(By locator) {
        manager.driver.findElement(locator).click();
    }

    // Ввод текста в поле: клик, очистка, ввод
    /*protected void type(By locator, String text) {
        click(locator);
        manager.driver.findElement(locator).clear();
        manager.driver.findElement(locator).sendKeys(text);
    }*/

    // Улучшенная версия type: элемент ищется один раз, затем выполняются действия.
    protected void type(By locator, String text) {
        // Находим поле ввода и сохраняем ссылку на него в переменную element.
        var element = manager.driver.findElement(locator);
        // Кликаем, чтобы поставить фокус в поле.
        element.click();
        // Очищаем поле от предыдущего значения.
        element.clear();
        // Вводим новый текст (имитация набора с клавиатуры).
        element.sendKeys(text);
    }

    // Прикрепить файл к полю загрузки (например, photo).
    protected void attach(By locator, String file) {
        // Если путь не задан (null) или пустой — ничего не делаем, выходим
        if (file == null || file.isEmpty()) {
            return;
        }
        // Преобразуем относительный путь в абсолютный — Selenium требует полный путь к файлу.
        var path = Paths.get(file).toAbsolutePath();
        // В поле типа file отправляем строку пути: браузер «подставит» этот файл.
        manager.driver.findElement(locator).sendKeys(path.toString());
    }
}
