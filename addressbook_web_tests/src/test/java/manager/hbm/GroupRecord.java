package manager.hbm;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

//import java.sql.Date;


// Класс, который описывает объекты, соответствующие записям в БД
// объявим только свойства, которые соответствуют столбцам в БД

@Entity
@Table(name = "group_list")
public class GroupRecord {


    @Id
    @Column(name = "group_id")
    public int id;
    @Column(name = "group_name")
    public String name;
    @Column(name = "group_header")
    public String header;
    @Column(name = "group_footer")
    public String footer;

    public Date deprecated = new Date();

    @ManyToMany //(fetch = FetchType.EAGER) загружать информацию о связях как можно раньше // многие ко многим
    @JoinTable(name = "address_in_groups",
                        joinColumns =  @JoinColumn(name = "group_id"),
                        inverseJoinColumns = @JoinColumn(name = "id"))
    public List<ContactRecord> contacts; // это свойство описывает связь с объектами типа ContactRecord


    public GroupRecord() {
    }

    public GroupRecord(int id, String name, String header, String footer) {
        this.id = id;
        this.name = name;
        this.header = header;
        this.footer = footer;
    }
}
