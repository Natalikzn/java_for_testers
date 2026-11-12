package manager.hbm;

// В данный класс Hibernate будет извлекать информацию о контактах из БД

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "addressbook")

public class ContactRecord {

    @Id // помечает поле как первичный ключ (primary key)
    public int id;
    public String firstname;
    public String lastname;
    public String address;

    public ContactRecord() {
    }

    public ContactRecord(int id, String firstname, String lastname, String address) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
    }

}
