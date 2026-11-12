package model;


import java.util.Objects;

public record ContactData(String id, String firstname, String lastname, String nickname, String address, String photo) {
    // Конструктор по умолчанию (пустой контакт)
    public ContactData() {
        this("", "", "", "", "", "");
    }


    public ContactData withId(String id) {
        return new ContactData(id, this.firstname, this.lastname, this.nickname, this.address, this.photo);
    }

    // Метод-билдер: создать новый объект с изменённым именем
    public ContactData withFirstName(String firstname) {
        return new ContactData(this.id, firstname, this.lastname, this.nickname, this.address, this.photo);
    }

    // Метод-билдер: создать новый объект с изменённой фамилией
    public ContactData withLastName(String lastname) {
        return new ContactData(this.id, this.firstname, lastname, this.nickname, this.address, this.photo);
    }

    // Метод-билдер: создать новый объект с изменённым никнеймом
    public ContactData withNickname(String nickname) {
        return new ContactData(this.id, this.firstname, this.lastname, nickname, this.address, this.photo);
    }

    public ContactData withAddressName(String address) {
        return new ContactData(this.id, this.firstname, this.lastname, this.nickname, address, this.photo);
    }

    // Метод-билдер: создать новый объект с изменённым фото
    public ContactData withPhoto(String photo) {
        return new ContactData(this.id, this.firstname, this.lastname, this.nickname, this.address, photo);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ContactData contact) {
            return id != null && id.equals(contact.id);
                    //&& firstname.equals(contact.firstname)
                    //&& lastname.equals(contact.lastname);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
