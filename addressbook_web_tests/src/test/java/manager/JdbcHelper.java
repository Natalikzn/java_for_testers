// Пакет manager — слой доступа к приложению и данным.
package manager;
// Модель группы, в которую складываем строки результата SQL-запроса.
import model.GroupData;
// DriverManager — менеджер JDBC-драйверов, через него открывается соединение.
import java.sql.DriverManager;
// SQLException — проверяемое исключение работы с БД.
import java.sql.SQLException;
// ArrayList — изменяемый список результатов.
import java.util.ArrayList;
// List — интерфейс списка.
import java.util.List;

// JdbcHelper — прямой доступ к БД через JDBC (в обход UI и ORM).
// Наследует HelperBase, чтобы иметь ссылку на менеджер.


// Конструктор: передаём менеджер в родительский класс.
public class JdbcHelper extends HelperBase {
    public JdbcHelper(ApplicationManager manager) {
        super(manager);
    }

    // Читает список групп напрямую из БД.
    public List<GroupData> getGroupList() {
        var groups = new ArrayList<GroupData>(); // Аккумулятор результата.

        // try-with-resources: соединение, statement и resultSet закроются автоматически.
        // Открываем соединение с локальной MySQL-базой addressbook под root без пароля.
        try (var conn = DriverManager.getConnection("jdbc:mysql://localhost/addressbook", "root", "");
             // Создаём Statement для выполнения SQL.
             var statement = conn.createStatement();
             // Выполняем запрос к view group_list (представление с группами) и получаем ResultSet.
             var result = statement.executeQuery("SELECT group_id, group_name, group_header, group_footer FROM group_list")) {
            // пока результаты не кончилиись
            // result.next() переходит к следующей строке; false означает конец данных.
            while (result.next()) {
                // Для каждой строки собираем GroupData через билдеры,
                // читая значения колонок по их именам.
                groups.add(new GroupData()
                        // group_id → id.
                        .withId(result.getString("group_id"))
                        .withName(result.getString("group_name"))
                        .withHeader(result.getString("group_header"))
                        .withFooter(result.getString("group_footer")));
            }
        } catch (SQLException e) {
            // Оборачиваем проверяемое SQLException в непроверяемое RuntimeException,
            // чтобы не загромождать сигнатуры тестов.
            throw new RuntimeException(e);
        }
        return groups; // Возвращаем прочитанный список групп.
    }

    // Проверка целостности БД: нет «висячих» связей контактов и групп.
    public void checkConsistency() {
        // Снова try-with-resources: автоматическое закрытие ресурсов.
        try (var conn = DriverManager.getConnection("jdbc:mysql://localhost/addressbook", "root", "");
             // Statement для выполнения запроса.
             var statement = conn.createStatement();
             // LEFT JOIN ищем строки address_in_groups, для которых нет контакта в addressbook:
             // такие «осиротевшие» связи означают повреждённые данные.
             var result = statement.executeQuery("SELECT * FROM `address_in_groups` left join addressbook on addressbook.id = address_in_groups.id where addressbook.id is null")) {
            // если результат не пустой, то нужно выбросить исключение
            if (result.next()) {
                // Нашли хотя бы одну повреждённую запись — сигнализируем об ошибке.
                throw new IllegalStateException("DB is corrupted");
            }
        } catch (SQLException e) {
            // Ошибку JDBC превращаем в RuntimeException, чтобы тест упал с понятной причиной.
            throw new RuntimeException(e);
        }
    }
}
