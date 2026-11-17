// Пакет manager — слой доступа к приложению (UI и БД).
package manager;

// ContactRecord — Hibernate-сущность таблицы addressbook (контакты в БД).
import manager.hbm.ContactRecord;
// GroupRecord — Hibernate-сущность таблицы group_list (группы в БД).
import manager.hbm.GroupRecord;
import model.ContactData;
import model.GroupData;
// SessionFactory — фабрика сессий Hibernate (создаётся один раз на приложение).
import org.hibernate.SessionFactory;
// AvailableSettings — константы имён настроек Hibernate (URL, USER, PASS…)
import org.hibernate.cfg.AvailableSettings;
// Configuration — конфигурация Hibernate (сущности + параметры подключения).
import org.hibernate.cfg.Configuration;
// @NonNull — аннотация «результат не null» (используется в сигнатуре метода).
import org.jspecify.annotations.NonNull;
// ArrayList — изменяемый список результатов конвертации.
import java.util.ArrayList;
// Comparator — сортировка списка по id.
import java.util.Comparator;
// List — интерфейс списка.
import java.util.List;

// HibernateHelper — доступ к БД через ORM: чтение групп/контактов и их связей.
//
// Задание 14: тесты проверяют списки, загруженные ИЗ БД, а не из интерфейса.
// Именно этот класс — точка входа для таких проверок: getContactList() отдаёт полный
// список контактов со всеми «плоскими» атрибутами (включая те, что не видны в UI).
//
// Состав колонок таблицы addressbook ограничивает переносимые атрибуты: в ней есть
// address, но НЕТ address2; есть home/mobile/work/fax, но НЕТ phone2; НЕТ колонки notes.
// Конвертация переносит только реально существующие в таблице поля.
public class HibernateHelper extends HelperBase {
    // Публичное поле с фабрикой сессий: через него выполняются все запросы.
    public SessionFactory sessionFactory;

    // Конструктор: настраивает и создаёт SessionFactory.
    public HibernateHelper(ApplicationManager manager) {
        super(manager);
        sessionFactory = new Configuration() // Создаём конфигурацию Hibernate в «программном» стиле (без hibernate.cfg.xml).
                .addAnnotatedClass(ContactRecord.class) // Регистрируем сущность контактов, чтобы Hibernate знал о её таблице.
                .addAnnotatedClass(GroupRecord.class) // Регистрируем сущность групп.
                // URL подключения к MySQL; zeroDateTimeBehavior=convertToNull — «нулевые» даты читаются как null.
                .setProperty(AvailableSettings.URL, "jdbc:mysql://localhost/addressbook?zeroDateTimeBehavior=convertToNull")
                // Имя/пароль пользователя БД.
                .setProperty(AvailableSettings.USER, "root")
                .setProperty(AvailableSettings.PASS, "")
                // Строим фабрику сессий — после этого можно открывать сессии.
                .buildSessionFactory();
    }

    // Преобразователь - функция, которая из объектов типов GroupRecord строит объекты типа GroupData
    static List<GroupData> convertList(List<GroupRecord> records) {
        List<GroupData> result = new ArrayList<>(); // Создаём пустой результирующий список моделей.
        // Проходим по всем записям, полученным из БД.
        for (var record : records) {
            // Конвертируем одну запись и накапливаем в списке
            result.add(convert(record));
        }
        return result; // Возвращаем список GroupData.
    }

    // Метод для конвертации списка. Аналогично convertList, но для контактов.
    static List<ContactData> convertContactList(List<ContactRecord> records) {
        List<ContactData> result = new ArrayList<>();
        for (var record : records) {
            result.add(convert(record));
        }
        return result;
    }

    // Метод, который конвертирует 1 запись из БД в объект типа GroupData. @NonNull — гарантия, что метод не вернёт null.
    private static @NonNull GroupData convert(GroupRecord record) {
        // id в модели — строка, поэтому число приводим конкатенацией с пустой строкой ("" + id).
        // Остальные поля передаются как есть: name, header, footer.
        return new GroupData("" + record.id, record.name, record.header, record.footer);
    }

    // Метод, который конвертирует объект типа GroupData в объект типа GroupRecord. Вызывает конструктор и передает туда параметры
    private static GroupRecord convert(GroupData data) {
        // Берём id из модели (строка).
        var id = data.id();
        // Пустой id означает «новая запись» — Hibernate требует число, используем 0.
        if ("".equals(id)) {
            id = "0";
        }
        // Отладочный вывод: видно, что именно уйдёт в БД (полезно при отладке тестов).
        System.out.println("convert: id=" + id + " name=" + data.name());
        // Создаём сущность; Integer.parseInt переводит строковый id в int.
        return new GroupRecord(Integer.parseInt(id), data.name(), data.header(), data.footer());
    }

    // ===================== Конвертация контактов =====================

    // DB → модель: ContactRecord превращаем в ContactData.
    // Переносим ВСЕ «плоские» текстовые атрибуты, которые есть в маппинге и реально
    // присутствуют в таблице addressbook, кроме фото (фото в БД — blob, его мы
    // сознательно не сравниваем). Колонки address2, phone2 и notes в таблице отсутствуют,
    // поэтому их значения взять неоткуда и в конвертации они не участвуют.
    // null-значения не обрабатываем здесь: компактный конструктор ContactData сам
    // приводит null к пустой строке.
    private static ContactData convert(ContactRecord record) {
        return new ContactData()
                .withId("" + record.id)          // числовой id из БД → строка в модели
                .withFirstName(record.firstname)
                .withMiddleName(record.middlename)
                .withLastName(record.lastname)
                .withNickname(record.nickname)
                .withTitle(record.title)
                .withCompany(record.company)
                .withAddressName(record.address)
                .withHome(record.home)
                .withMobile(record.mobile)
                .withWork(record.work)
                .withFax(record.fax)
                .withEmail(record.email)
                .withEmail2(record.email2)
                .withEmail3(record.email3)
                .withHomepage(record.homepage);
    }

    // Модель → DB: ContactData превращаем в ContactRecord.
    private static ContactRecord convert(ContactData data) {
        // Берём id из модели.
        var id = data.id();
        // Для нового объекта id пустой — заменяем на "0".
        if ("".equals(id)) {
            id = "0";
        }
        // Создаём сущность контакта: числовой id + все текстовые атрибуты таблицы addressbook.
        return new ContactRecord(
                Integer.parseInt(id),
                data.firstname(),
                data.middlename(),
                data.lastname(),
                data.nickname(),
                data.title(),
                data.company(),
                data.address(),
                data.home(),
                data.mobile(),
                data.work(),
                data.fax(),
                data.email(),
                data.email2(),
                data.email3(),
                data.homepage());
    }

    // ===================== Группы =====================

    // Получить список всех групп из БД.
    public List<GroupData> getGroupList() {
        // fromSession открывает сессию, выполняет лямбду и закрывает сессию автоматически.
        return convertList(sessionFactory.fromSession(session -> {
            // HQL-запрос "from GroupRecord" = SELECT * FROM group_list; list() возвращает записи.
            return session.createQuery("from GroupRecord", GroupRecord.class).list();
        }));
    }

    // Метод для подсчета количества групп из БД
    public long getGroupCount() {
        // Открываем сессию и выполняем запрос.
        return sessionFactory.fromSession(session -> {
            // count(*) возвращает Long; getSingleResult() — единственное значение.
            return session.createQuery("select count(*) from GroupRecord", Long.class).getSingleResult();
        });
    }

    // Создать группу напрямую в БД (без UI) — удобно для подготовки данных.
    public void createGroup(GroupData groupData) {
        // inSession — сессия с автоматическим управлением жизненным циклом.
        sessionFactory.inSession(session -> {
            // Начинаем транзакцию: изменения должны быть атомарными.
            session.getTransaction().begin();
            // persist сохраняет новый объект в БД.
            session.persist(convert(groupData));
            // Фиксируем транзакцию — данные записаны.
            session.getTransaction().commit();
        });
    }

    // по id группы находит все контакты, которые в эту группу включены
    public List<ContactData> getContactsInGroup(GroupData group) {
        // Открываем сессию с БД.
        return sessionFactory.fromSession(session -> {
            // Зная id группы получаем объект типа GroupRecord, а у него свойство contacts
            // session.get загружает сущность по первичному ключу; .contacts — связь many-to-many из GroupRecord.
            return convertContactList(session.get(GroupRecord.class, group.id()).contacts);
        });
    }

    // ===================== Контакты =====================

    // Получить полный список контактов ИЗ БД со всеми сравнимыми атрибутами.
    //
    // Это ключевой метод задания 14: тесты создания/удаления/модификации контактов
    // строят ожидаемый список и читают фактический именно здесь, а не через
    // ContactHelper#getList() (тот видит только фамилию и имя из таблицы UI).
    //
    // Список принудительно сортируется по id: без ORDER BY СУБД не гарантирует порядок
    // строк, и сравнение «до/после» могло бы падать из-за перестановки элементов,
    // хотя данные не менялись.
    public List<ContactData> getContactList() {
        var contacts = sessionFactory.fromSession(session -> {
            // HQL "from ContactRecord" = SELECT * FROM addressbook.
            return session.createQuery("from ContactRecord", ContactRecord.class).list();
        });
        // Преобразуем записи БД в модели и сортируем по числовому значению id.
        var result = convertContactList(contacts);
        result.sort(Comparator.comparingInt(contact -> Integer.parseInt(contact.id())));
        return result;
    }

    // Количество контактов в БД (дешевле, чем читать весь список).
    public long getContactCount() {
        return sessionFactory.fromSession(session -> {
            // count(*) по таблице контактов.
            return session.createQuery("select count(*) from ContactRecord", Long.class).getSingleResult();
        });
    }

    // Создать контакт напрямую в БД (без UI) — для подготовки данных в предусловиях тестов.
    // Возвращаем сохранённый ContactData: первичный ключ присваивает БД, и он нужен вызывающему коду.
    public ContactData createContact(ContactData contactData) {
        // Переменная для сгенерированного БД первичного ключа.
        var id = 0;
        // inSession — сессия с автоматическим управлением жизненным циклом (закрывается сама).
        var session = sessionFactory.openSession();
        try {
            // Начинаем транзакцию: запись должна быть атомарной.
            session.getTransaction().begin();
            // Готовим сущность из модели (новый контакт приходит с пустым id → он станет 0).
            var record = convert(contactData);
            // persist сохраняет объект; после flush/commit Hibernate проставит record.id.
            session.persist(record);
            // Фиксируем транзакцию.
            session.getTransaction().commit();
            // Запоминаем присвоенный id.
            id = record.id;
        } finally {
            // Сессию закрываем в любом случае, даже если запись упала.
            session.close();
        }
        // Возвращаем модель с настоящим id из БД — такую запись можно сравнивать со списком.
        return contactData.withId("" + id);
    }
}
