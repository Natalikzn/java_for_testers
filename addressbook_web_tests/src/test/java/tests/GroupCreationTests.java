package tests;

import model.GroupData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//import static tests.TestBase.randomString;

public class GroupCreationTests extends TestBase {


    public static List<GroupData> groupProvider() {
        var result = new ArrayList<GroupData>();

        for (var name : List.of("", "group name")) {
            for (var header : List.of("", "group header")) {
                for (var footer : List.of("", "group footer")) {
                    result.add(new GroupData()
                            .withName(name)
                            .withHeader(header)
                            .withFooter(footer));
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            result.add(new GroupData()
                    .withName(randomString(i * 10))
                    .withHeader(randomString(i * 10))
                    .withFooter(randomString(i * 10)));
        }
        return result;
    }

    public static List<GroupData> negativeGroupProvider() {
        var result = new ArrayList<GroupData>(List.of(
                new GroupData("", "group ' name", "", "")));
        return result;
    }

    @ParameterizedTest
    @MethodSource("groupProvider")
    public void canCreateMultipleGroups(GroupData group) {
        var oldGroups = app.groups().getList(); // получаем старый список групп
        app.groups().createGroup(group);
        var newGroups = app.groups().getList(); // получаем новый список
        Comparator<GroupData> compareById = (o1, o2) -> {
            return Integer.compare(Integer.parseInt(o1.id()), Integer.parseInt(o2.id()));
        };
        newGroups.sort(compareById); // упорядочиваем список групп после создания новой группы
        var expectedList = new ArrayList<>(oldGroups);
        expectedList.add(group.withId(newGroups.get(newGroups.size() - 1).id()).withHeader("").withFooter("")); // берем последний максимальный идентификатор
        // очищаем header и footer перед сортировкой, чтобы они не участвовали в сравнении
        expectedList.sort(compareById); // сортируем expectedList
        Assertions.assertEquals(newGroups, expectedList); // сравниваем старый список, в который добавлен элемент, и новый список
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
