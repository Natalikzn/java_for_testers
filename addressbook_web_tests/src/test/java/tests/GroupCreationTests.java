package tests;

//import com.fasterxml.jackson.core.type.TypeReference;

import common.CommonFunctions;
import model.GroupData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//import static tests.TestBase.randomString;

public class GroupCreationTests extends TestBase {


    public static List<GroupData> groupProvider() throws IOException {
        var result = new ArrayList<GroupData>();

        /*for (var name : List.of("", "group name")) {
            for (var header : List.of("", "group header")) {
                for (var footer : List.of("", "group footer")) {
                    result.add(new GroupData()
                            .withName(name)
                            .withHeader(header)
                            .withFooter(footer));
                }
            }
        }*/
        /*var json = "";
        try (var reader = new FileReader("groups.json");
            var breader = new BufferedReader(reader)) {
            var line = breader.readLine();
            while (line != null) {
                json = json + line;
                line = breader.readLine();
            }
        }*/
        //var json = Files.readString(Paths.get("groups.json"));

        var mapper = new XmlMapper();

        //var value = mapper.readValue(json, new TypeReference<List<GroupData>>() {});

        var value = mapper.readValue(new File("groups.xml"), new TypeReference<List<GroupData>>() {
        });
        result.addAll(value);
        return result;
    }

    public static List<GroupData> singleRandomGroup() {
        return List.of(new GroupData()
                .withName(CommonFunctions.randomString(10))
                .withHeader(CommonFunctions.randomString(20))
                .withFooter(CommonFunctions.randomString(30)));
    }

    public static List<GroupData> negativeGroupProvider() {
        var result = new ArrayList<GroupData>(List.of(
                new GroupData("", "group ' name", "", "")));
        return result;
    }

    @ParameterizedTest
    //@MethodSource("groupProvider")
    @MethodSource("singleRandomGroup")
    //public void canCreateMultipleGroups(GroupData group) {
    public void canCreateGroup(GroupData group) {
        var oldGroups = app.jdbc().getGroupList(); // получаем старый список групп
        app.groups().createGroup(group);
        var newGroups = app.jdbc().getGroupList(); // получаем новый список
        Comparator<GroupData> compareById = (o1, o2) -> {
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        newGroups.sort(compareById); // упорядочиваем список групп после создания новой группы

        var maxId = newGroups.get(newGroups.size() - 1).id(); // максимальный из существующих id, то есть id новой созданной группы

        var expectedList = new ArrayList<>(oldGroups);
        expectedList.add(group.withId(maxId)); // берем последний максимальный идентификатор
        // очищаем header и footer перед сортировкой, чтобы они не участвовали в сравнении
        expectedList.sort(compareById); // сортируем expectedList
        Assertions.assertEquals(newGroups, expectedList); // сравниваем старый список, в который добавлен элемент, и новый список

        //var newUiGroups = app.groups().getList(); // создаем список групп с UI  и далее сравнивать с newGroups, но опять только по id и name
    }

    @ParameterizedTest
    @MethodSource("negativeGroupProvider")
    public void canNotCreateGroups(GroupData group) {
        var oldGroups = app.groups().getList(); // получаем старый список
        app.groups().createGroup(group);
        var newGroups = app.groups().getList(); // после того, как группа не создана, получаем новый список
        Assertions.assertEquals(newGroups, oldGroups); // сравниваем старый список с новым
    }

}
