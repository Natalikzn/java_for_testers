// Пакет manager.hbm — Hibernate-сущности, описывающие таблицы БД
package manager.hbm;

import jakarta.persistence.*; // Импортируем все аннотации JPA (Entity, Table, Id, Column, ManyToMany, JoinTable, JoinColumn)

import java.util.Date; // Импорт java.util.Date — используется для поля deprecated
import java.util.List; // Импорт List — тип коллекции связанных контактов

/*import java.sql.Date;*/

// Сущность «группа»
@Entity
@Table(name = "group_list") // Таблица group_list (представление групп в приложении)

public class GroupRecord {

    @Id // @Id — первичный ключ.
    @Column(name = "group_id") // Явно указываем, что поле связано с колонкой group_id.
    public int id;

    @Column(name = "group_name") // Поле name ↔ колонка group_name.
    public String name;

    @Column(name = "group_header")
    public String header;

    @Column(name = "group_footer")
    public String footer;

    public Date deprecated = new Date(); // Техническое поле, имитирующее устаревшую колонку; инициализируется текущей датой.

    // @ManyToMany — связь «многие ко многим»: в группе много контактов, контакт может быть в многих группах.
    @ManyToMany //(fetch = FetchType.EAGER) загружать информацию о связях как можно раньше - не используем
    @JoinTable(name = "address_in_groups",
            joinColumns = @JoinColumn(name = "group_id"), // joinColumns — колонка этой сущности (group_id) в промежуточной таблице.
            inverseJoinColumns = @JoinColumn(name = "id"))
    // inverseJoinColumns — колонка «другой» сущности (id контакта).

    public List<ContactRecord> contacts; // это свойство описывает связь с объектами типа ContactRecord

    // Пустой конструктор обязателен для Hibernate.
    public GroupRecord() {
    }

    // Конструктор для создания сущности из модели GroupData (используется в конвертерах).
    public GroupRecord(int id, String name, String header, String footer) {
        this.id = id;
        this.name = name;
        this.header = header;
        this.footer = footer;
    }
}
