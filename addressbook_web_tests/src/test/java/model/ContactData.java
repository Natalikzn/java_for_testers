package model;


public record ContactData(String id, String firstname, String lastname, String nickname) {
    // Конструктор по умолчанию (пустой контакт)
    public ContactData() {
        this("", "", "", "");
    }
     /*public static ContactData empty() {
        return new ContactData("", "", "");
    }*/

    public ContactData withId(String id) {
        return new ContactData(id, this.firstname, this.lastname, this.nickname);
    }

    // Метод-билдер: создать новый объект с изменённым именем
    public ContactData withFirstName(String firstname) {
        return new ContactData(this.id, firstname, this.lastname, this.nickname);
    }

    // Метод-билдер: создать новый объект с изменённой фамилией
    public ContactData withLastName(String lastname) {
        return new ContactData(this.id, this.firstname, lastname, this.nickname);
    }

    // Метод-билдер: создать новый объект с изменённым никнеймом
    public ContactData withNickname(String nickname) {
        return new ContactData(this.id, this.firstname, this.lastname, nickname);
    }

}
