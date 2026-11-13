// Пакет model — модели данных, используемые в тестах и генераторе.
package model;


import java.util.Objects; // Objects — утилита для вычисления hashCode (Objects.hash).

// ContactData — неизменяемая запись с данными контакта.
// Компоненты: id, firstname, lastname, nickname, address, photo.
// Компилятор создаёт геттеры id(), firstname(), ... и toString().
public record ContactData(String id, String firstname, String lastname, String nickname, String address, String photo) {
    // Дополнительный конструктор без параметров: создаёт объект, где все поля — пустые строки.
    public ContactData() {
        // Делегируем каноническому конструктору записи (this(...) должен быть первым оператором).
        this("", "", "", "", "", "");
    }

    // «Билдер»: возвращает НОВЫЙ объект, отличающийся только id (записи неизменяемы).
    public ContactData withId(String id) {
        // Передаём новый id, остальные поля берём из текущего объекта.
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

    // Переопределение equals: контакты сравниваются ТОЛЬКО по id.
    public ContactData withAddressName(String address) {
        return new ContactData(this.id, this.firstname, this.lastname, this.nickname, address, this.photo);
    }

    // Метод-билдер: создать новый объект с изменённым фото
    public ContactData withPhoto(String photo) {
        return new ContactData(this.id, this.firstname, this.lastname, this.nickname, this.address, photo);
    }

    // Переопределение equals: контакты сравниваются ТОЛЬКО по id.
    @Override
    public boolean equals(Object other) {
        // Если это тот же самый объект (одна ссылка) — сразу равны (быстрая проверка).
        if (this == other) {
            return true;
        }
        // pattern matching instanceof: проверяем тип и сразу приводим к ContactData в переменную contact.
        if (other instanceof ContactData contact) {
            // Равны, если id не null и совпадает с id другого объекта.
            // Сравнение по firstname/lastname закомментировано — в тестах достаточно id.
            return id != null && id.equals(contact.id);
                    //&& firstname.equals(contact.firstname)
                    //&& lastname.equals(contact.lastname);
        }
        // Аргумент другого типа — объекты не равны.
        return false;
    }

    // Переопределение hashCode в соответствии с equals (только по id).
    @Override
    public int hashCode() {
        // Objects.hash корректно обрабатывает null (id может быть пустым/null).
        return Objects.hash(id);
    }

}
