// Пакет manager.hbm — Hibernate-сущности (mapping), описывающие таблицы БД.
package manager.hbm;

// В данный класс Hibernate будет извлекать информацию о контактах из БД

import jakarta.persistence.Entity; // @Entity — помечает класс как сущность JPA/Hibernate (таблица в БД).
import jakarta.persistence.Id; // @Id — первичный ключ сущности.
import jakarta.persistence.Table; // @Table — имя таблицы, с которой связана сущность.

// Сущность «контакт»: соответствует таблице addressbook.
@Entity
@Table(name = "addressbook")

// Класс-сущность контакта. Поля public — Hibernate обращается к ним напрямую.
public class ContactRecord {

    @Id // помечает поле как первичный ключ (primary key)
    public int id; // id контакта (int), столбец id в таблице.
    public String firstname; // Имя контакта (колонка firstname).
    public String lastname;
    public String address;

    // Пустой конструктор обязателен для Hibernate: он создаёт объект перед заполнением полей.
    public ContactRecord() {
    }

    // Конструктор для удобного создания сущности из кода (конвертеры в HibernateHelper)
    public ContactRecord(int id, String firstname, String lastname, String address) {
        this.id = id; // Присваиваем переданный id полю объекта.
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
    }

}
