// Пакет manager.hbm — Hibernate-сущности (mapping), описывающие таблицы БД.
package manager.hbm;

// В данный класс Hibernate будет извлекать информацию о контактах из БД.

import jakarta.persistence.Column; // @Column — явное указание имени колонки в таблице.
import jakarta.persistence.Entity; // @Entity — помечает класс как сущность JPA/Hibernate (таблица в БД).
import jakarta.persistence.Id; // @Id — первичный ключ сущности.
import jakarta.persistence.Table; // @Table — имя таблицы, с которой связана сущность.

// Сущность «контакт»: соответствует таблице addressbook.
//
// Задание 14: для проверок «по данным из БД» нам нужны «плоские» текстовые атрибуты
// контакта, а не только те, что видны в списке контактов в интерфейсе. Поэтому в маппинг
// добавлены middlename, title, company, home, mobile, work, fax, email/email2/email3,
// homepage. Это «плоские» текстовые поля: их легко прочитать и сравнить.
//
// Состав колонок таблицы addressbook:
//   id, firstname, middlename, lastname, nickname, company, title, address,
//   home, mobile, work, fax, email, email2, email3, homepage,
//   bday, bmonth, byear, aday, amonth, ayear, photo, created, modified, deprecated.
// В таблице НЕТ колонок address2, phone2 и notes, поэтому такие поля в сущности
// отсутствуют (ранее ошибочно добавляемые колонки удалены из маппинга).
//
// Сознательно НЕ маппим:
//   * photo — BLOB с двоичными данными картинки (сравнение потребовало бы отдельной логики);
//   * bday/bmonth/byear, aday/amonth/ayear — дни рождения и годовщины: в приложении это
//     списки с текстовыми месяцами, в БД — числа, из-за чего сравнение даёт ложные расхождения;
//   * created/modified/deprecated — служебные колонки приложения, к данным контакта
//     отношения не имеют.
// Hibernate игнорирует колонки, которые не описаны в сущности, — на чтение остальных полей
// это не влияет.
@Entity
@Table(name = "addressbook")
public class ContactRecord {

    @Id // помечает поле как первичный ключ (primary key)
    @Column(name = "id") // колонка id в таблице addressbook
    public int id;

    // --- идентификация и имя ---
    @Column(name = "firstname")
    public String firstname; // имя
    @Column(name = "middlename")
    public String middlename; // отчество
    @Column(name = "lastname")
    public String lastname; // фамилия
    @Column(name = "nickname")
    public String nickname; // никнейм

    // --- работа ---
    @Column(name = "title")
    public String title; // должность
    @Column(name = "company")
    public String company; // компания

    // --- адрес (отдельной колонки address2 в таблице нет) ---
    @Column(name = "address")
    public String address; // адрес

    // --- телефоны (отдельной колонки phone2 в таблице нет) ---
    @Column(name = "home")
    public String home; // домашний телефон
    @Column(name = "mobile")
    public String mobile; // мобильный телефон
    @Column(name = "work")
    public String work; // рабочий телефон
    @Column(name = "fax")
    public String fax; // факс

    // --- интернет ---
    @Column(name = "email")
    public String email; // первый e-mail
    @Column(name = "email2")
    public String email2; // второй e-mail
    @Column(name = "email3")
    public String email3; // третий e-mail
    @Column(name = "homepage")
    public String homepage; // домашняя страница

    // Пустой конструктор обязателен для Hibernate: он создаёт объект перед заполнением полей.
    public ContactRecord() {
    }

    // Конструктор для удобного создания сущности из кода.
    // Порядок параметров совпадает с порядком компонентов ContactData (кроме фото),
    // чтобы конвертация в HibernateHelper была наглядной и без путаницы.
    public ContactRecord(int id,
                         String firstname,
                         String middlename,
                         String lastname,
                         String nickname,
                         String title,
                         String company,
                         String address,
                         String home,
                         String mobile,
                         String work,
                         String fax,
                         String email,
                         String email2,
                         String email3,
                         String homepage) {
        this.id = id; // Присваиваем переданный id полю объекта.
        this.firstname = firstname;
        this.middlename = middlename;
        this.lastname = lastname;
        this.nickname = nickname;
        this.title = title;
        this.company = company;
        this.address = address;
        this.home = home;
        this.mobile = mobile;
        this.work = work;
        this.fax = fax;
        this.email = email;
        this.email2 = email2;
        this.email3 = email3;
        this.homepage = homepage;
    }
}
