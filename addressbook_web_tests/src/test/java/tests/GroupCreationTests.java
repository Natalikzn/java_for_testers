// Пакет tests — тестовые классы.
package tests;

//import com.fasterxml.jackson.core.type.TypeReference;

import common.CommonFunctions; // Утилита генерации случайных строк.
import model.GroupData; // Модель группы.
import org.junit.jupiter.api.Assertions; // Методы-проверки JUnit.
import org.junit.jupiter.params.ParameterizedTest; // @ParameterizedTest — тест с набором параметров.
import org.junit.jupiter.params.provider.MethodSource; // @MethodSource — метод-поставщик параметров.
import tools.jackson.core.type.TypeReference; // TypeReference — сохранение генерического типа List<GroupData> при чтении XML.
import tools.jackson.dataformat.xml.XmlMapper; // XmlMapper — маппер Jackson 3 для чтения XML.

import java.io.File; // File — источник данных.
import java.io.IOException; // IOException — проверяемое исключение чтения файла.
import java.util.ArrayList; // ArrayList — изменяемый список.
import java.util.Comparator; // Comparator — сортировка по id.
import java.util.List; // List — интерфейс списка.

//import static tests.TestBase.randomString;

// Тесты создания групп.
public class GroupCreationTests extends TestBase {
    // Поставщик данных: читает группы из groups.xml.
    public static List<GroupData> groupProvider() throws IOException {
        var result = new ArrayList<GroupData>(); // Аккумулятор тестовых данных.

        /*for (var name : List.of("", "group name")) {
            for (var header : List.of("", "group header")) {
                for (var footer : List.of("", "group footer")) {
                    result.add(new GroupData()
                            .withName(name)
                            .withHeader(header)
                            .withFooter(footer));
                }
            }
        }
        var json = "";
        try (var reader = new FileReader("groups.json");
            var breader = new BufferedReader(reader)) {
            var line = breader.readLine();
            while (line != null) {
                json = json + line;
                line = breader.readLine();
            }

        //var json = Files.readString(Paths.get("groups.json"));*/

        // Создаём XML-маппер.
        var mapper = new XmlMapper();

        /*var value = mapper.readValue(json, new TypeReference<List<GroupData>>() {});*/

        // Читаем groups.xml и получаем список GroupData (TypeReference задаёт тип списка).
        var value = mapper.readValue(new File("groups.xml"), new TypeReference<List<GroupData>>() {
        });
        // Добавляем прочитанные группы к результату.
        result.addAll(value);
        // Возвращаем набор параметров.
        return result;
    }

    // Поставщик одной случайной группы — используется по умолчанию.
    public static List<GroupData> singleRandomGroup() {
        // Возвращаем список из одной группы со случайными name (10), header (20), footer (30) символами.
        return List.of(new GroupData()
                .withName(CommonFunctions.randomString(10)) // Название — 10 случайных букв.
                .withHeader(CommonFunctions.randomString(20)) // Header — 20 случайных букв.
                .withFooter(CommonFunctions.randomString(30))); // Footer — 30 случайных букв.
    }

    // Негативный набор: имя группы содержит апостроф — ожидаем, что группа не создастся.
    public static List<GroupData> negativeGroupProvider() {
        // Список из одной «плохой» группы.
        var result = new ArrayList<GroupData>(List.of(
                // name = "group ' name" — спецсимвол, ломающий сценарий создания.
                new GroupData("", "group ' name", "", "")));
        return result;
    }

    // Параметризованный тест создания группы.
    @ParameterizedTest
    /*@MethodSource("groupProvider")*/
    @MethodSource("singleRandomGroup") // Данные берём из singleRandomGroup (по одной случайной группе на прогон).
    /*public void canCreateMultipleGroups(GroupData group) {*/
    public void canCreateGroup(GroupData group) {
        // получаем старый список групп
        // Читаем список напрямую из БД (JDBC), чтобы сравнивать состояние «до/после».
        var oldGroups = app.jdbc().getGroupList();
        // Создаём группу через UI.
        app.groups().createGroup(group);
        // получаем новый список
        var newGroups = app.jdbc().getGroupList();
        // Компаратор по числовому id (в модели id — строка).
        Comparator<GroupData> compareById = (o1, o2) -> {
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        // упорядочиваем список групп после создания новой группы
        newGroups.sort(compareById);
        // максимальный из существующих id, то есть id новой созданной группы
        // После сортировки последний элемент имеет максимальный id.
        var maxId = newGroups.get(newGroups.size() - 1).id();
        // Копия старого списка — основа ожидаемого результата.
        var expectedList = new ArrayList<>(oldGroups);
        // берем последний максимальный идентификатор
        // очищаем header и footer перед сортировкой, чтобы они не участвовали в сравнении
        // фактически поля не очищаются — сравниваются все поля записи)
        expectedList.add(group.withId(maxId));
        expectedList.sort(compareById); // сортируем expectedList
        // сравниваем старый список, в который добавлен элемент, и новый список
        Assertions.assertEquals(newGroups, expectedList);
        /*var newUiGroups = app.groups().getList(); // создаем список групп с UI  и далее сравнивать с newGroups, но опять только по id и name*/
    }

    // Негативный параметризованный тест: группа не должна создаться.
    @ParameterizedTest
    @MethodSource("negativeGroupProvider") // Данные — из negativeGroupProvider.
    public void canNotCreateGroups(GroupData group) {
        // получаем старый список групп через UI до попытки создания.
        var oldGroups = app.groups().getList();
        // Пытаемся создать заведомо некорректную группу.
        app.groups().createGroup(group);
        // после того, как группа не создана, получаем новый список
        var newGroups = app.groups().getList();
        // сравниваем старый список с новым
        // Списки должны совпасть — значит, создания не произошло.
        Assertions.assertEquals(newGroups, oldGroups);
    }

}
