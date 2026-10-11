package model;


public record ContactData(String firstname, String lastname) {
    // Конструктор по умолчанию (пустой контакт)
    public ContactData() {
        this("", "");
    }

    // Метод-билдер: создать новый объект с изменённым именем
    public ContactData withFirstName(String firstname) {
        return new ContactData(firstname, this.lastname);
    }

    // Метод-билдер: создать новый объект с изменённой фамилией
    public ContactData withLastName(String lastname) {
        return new ContactData(this.firstname, lastname);
    }

}
