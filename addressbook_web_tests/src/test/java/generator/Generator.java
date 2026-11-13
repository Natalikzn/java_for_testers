// Пакет generator — утилита командной строки для генерации тестовых данных
package generator;

import com.beust.jcommander.JCommander; // JCommander — библиотека разбора аргументов командной строки
import com.beust.jcommander.Parameter; // @Parameter — аннотация JCommander, описывающая параметр командной строки
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper — основной класс Jackson для чтения/записи JSON
import com.fasterxml.jackson.databind.SerializationFeature; // SerializationFeature — набор настроек сериализации (например, форматирование с отступами)
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper; // YAMLMapper — «маппер», который пишет/читает YAML вместо JSON
import common.CommonFunctions; // Наша утилита генерации случайных строк
import model.ContactData; // Модель контакта (record) — в неё складываются сгенерированные данные
import model.GroupData; // Модель группы (record)
import tools.jackson.dataformat.xml.XmlMapper; // XmlMapper из Jackson 3 — сериализация в XML

import java.io.File; // File — абстракция файла/каталога
import java.io.FileWriter; // FileWriter — поток записи символов в файл
import java.io.IOException; // IOException — проверяемое исключение ввода-вывода, объявляется в throws
import java.util.ArrayList; // ArrayList — изменяемый список для накопления сгенерированных объекто

import static tests.TestBase.randomFile; // Статический импорт: даёт возможность вызывать randomFile(...) без имени класса TestBase

// Главный класс утилиты генерации данных
public class Generator {

    // Параметр --type (-t): что генерировать — "groups" или "contacts"
    @Parameter(names = {"--type", "-t"})
    String type;

    // Параметр --output (-o): путь к файлу, куда сохранить результат
    @Parameter(names = {"--output", "-o"})
    String output;

    // Параметр --format (-f): формат файла — "json", "yaml" или "xml"
    @Parameter(names = {"--format", "-f"})
    String format;

    // Параметр --format (-f): формат файла — "json", "yaml" или "xml"
    @Parameter(names = {"--count", "-n"})
    int count;

    // main — точка входа утилиты; throws IOException т.к. дальше идёт запись в файл
    public static void main(String[] args) throws IOException {
        // Создаём экземпляр генератора — в его поля JCommander запишет аргументы
        var generator = new Generator();
        // Собираем парсер аргументов: связываем его с объектом generator и разбираем массив args
        JCommander.newBuilder()
                .addObject(generator) // Добавляем объект, поля которого аннотированы @Parameter
                .build()// Создаём готовый JCommander
                .parse(args); // Разбираем аргументы командной строки (при ошибке JCommander бросит исключение)
        // Запускаем основную логику: сгенерировать данные и сохранить их
        generator.run();
    }

    // Основной сценарий: генерация + сохранение
    private void run() throws IOException {
        var data = generate(); // generate() возвращает Object: это либо List<GroupData>, либо List<ContactData>
        save(data); // Сохраняем полученные данные в файл в выбранном формате
    }

    // Диспетчер: выбирает генератор по значению --type
    private Object generate() {
        if ("groups".equals(type)) {
            return generateGroups(); // Если тип "groups" — генерируем группы
        } else if ("contacts".equals(type)) {
            return generateContacts(); // Если тип "contacts" — генерируем контакты
        } else {
            throw new IllegalArgumentException("Неизвестный тип данных " + type); // Неизвестный тип — сообщаем об ошибке и прерываем выполнение
        }
    }

    // Генерация списка групп
    private Object generateGroups() {
        var result = new ArrayList<GroupData>();
        for (int i = 0; i < count; i++) {
            result.add(new GroupData()
                    // Создаём GroupData через «билдеры» with*: каждое поле заполняется случайной строкой.
                    // Длина строки зависит от i (i*10): 0, 10, 20 … — так получаются разные по длине значения
                    .withName(CommonFunctions.randomString(i * 10))
                    .withHeader(CommonFunctions.randomString(i * 10))
                    .withFooter(CommonFunctions.randomString(i * 10)));
        }
        return result;
    }

    // Генерация списка контактов
    private Object generateContacts() {
        var result = new ArrayList<ContactData>();
        for (int i = 0; i < count; i++) {
            result.add(new ContactData()
                    .withFirstName(CommonFunctions.randomString(10))
                    .withLastName(CommonFunctions.randomString(10))
                    .withPhoto(randomFile("src/test/resources/images/"))); // Фото: случайный файл из каталога с картинками (randomFile импортирован статически)
        }
        return result;
    }

    /* private void save(Object data) {}*/

    // Сохранение данных в файл в выбранном формате
    private void save(Object data) throws IOException {
        // Ветка JSON
        if ("json".equals(format)) {
            ObjectMapper mapper = new ObjectMapper(); // Создаём JSON-маппер Jackson
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // Включаем «красивый» вывод с отступами и переносами строк
            var json = mapper.writeValueAsString(data); // Превращаем объект (список) в строку JSON
            // try-with-resources: writer закроется автоматически после блока
            try (var writer = new FileWriter(output)) {
                writer.write(json); // Записываем JSON в файл
            }
        }
        /* writer.close(); // данные запишем на диск*/
        // Ветка YAML: YAMLMapper сам записывает данные в файл по указанному пути
        else if ("yaml".equals(format)) {
            var mapper = new YAMLMapper(); // Создаём YAML-маппер
            mapper.writeValue(new File(output), data); // Сериализуем объект сразу в файл
        }
        // Ветка XML: XmlMapper аналогично пишет список в XML-файл
        else if ("xml".equals(format)) {
            var mapper = new XmlMapper(); // Создаём XML-маппер
            mapper.writeValue(new File(output), data); // Записываем объект в XML-файл
        } else {
            throw new IllegalArgumentException("Неизвестный формат данных " + format);
        }
        /*if (data instanceof ArrayList<?> list) {
            System.out.println("Сгенерировано групп: " + list.size());
            list.forEach(System.out::println);
        } else {
            System.out.println("Данные: " + data);
        }*/
    }
}
