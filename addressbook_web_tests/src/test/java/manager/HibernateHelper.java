package manager;

import manager.hbm.ContactRecord;
import manager.hbm.GroupRecord;
import model.ContactData;
import model.GroupData;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class HibernateHelper extends HelperBase {

    public SessionFactory sessionFactory;

    public HibernateHelper(ApplicationManager manager) {
        super(manager);
        sessionFactory = new Configuration()
                .addAnnotatedClass(ContactRecord.class) // добавили инфу, что этот класс есть
                .addAnnotatedClass(GroupRecord.class)
                .setProperty(AvailableSettings.URL, "jdbc:mysql://localhost/addressbook?zeroDateTimeBehavior=convertToNull")
                .setProperty(AvailableSettings.USER, "root")
                .setProperty(AvailableSettings.PASS, "")
                .buildSessionFactory();
    }


    // Преобразователь - функция, которая из объектов типов GroupRecord строит объекты типа GroupData
    static List<GroupData> convertList(List<GroupRecord> records) {
        List<GroupData> result = new ArrayList<>();
        for (var record : records) {
            result.add(convert(record));
        }
        return result;
    }

    // Метод для конвертаци списка
    static List<ContactData> convertContactList(List<ContactRecord> records) {
        List<ContactData> result = new ArrayList<>();
        for (var record : records) {
            result.add(convert(record));
        }
        return result;
    }

    // Метод, который конвертирует 1 запись из БД в объект типа GroupData
    private static @NonNull GroupData convert(GroupRecord record) {
        return new GroupData("" + record.id, record.name, record.header, record.footer);
    }

    // Метод, который конвертирует объект типа GroupData в объект типа GroupRecord. Вызывает конструктор и передает туда параметры
    private static GroupRecord convert(GroupData data) {
        var id = data.id();
        if ("".equals(id)) {
            id = "0";
        }
        System.out.println("convert: id=" + id + " name=" + data.name());
         return new GroupRecord(Integer.parseInt(id), data.name(), data.header(), data.footer());
    }

    // Методы для отдельного конверта контакта в одну и в другую сторону
    private static ContactData convert(ContactRecord record) {
        return new ContactData().withId("" + record.id)
                .withFirstName(record.firstname)
                .withLastName(record.lastname)
                .withAddressName(record.address);
    }

    private static ContactRecord convert(ContactData data) {
        var id = data.id();
        if ("".equals(id)) {
            id = "0";
        }
        return new ContactRecord(Integer.parseInt(id), data.firstname(), data.lastname(), data.address());
    }


    public List<GroupData> getGroupList() {
        return convertList(sessionFactory.fromSession(session -> {
            return session.createQuery("from GroupRecord", GroupRecord.class).list();
        }));
    }

    // Метод для подсчета количества групп из БД
    public long getGroupCount() {
        return sessionFactory.fromSession(session -> {
            return session.createQuery("select count(*) from GroupRecord", Long.class).getSingleResult();
        });
    }

    public void createGroup(GroupData groupData) {
        sessionFactory.inSession(session -> {
            session.getTransaction().begin();
            session.persist(convert(groupData));
            session.getTransaction().commit();
        });
    }

    // по id группы находит все контакты, которые в эту группу включены
    public List<ContactData> getContactsInGroup(GroupData group) {
        return sessionFactory.fromSession(session -> {
         return convertContactList(session.get(GroupRecord.class, group.id()).contacts); //Зная id группы получаем объект типа GroupRecord, а у него свойство contacts
        });
    }
}
