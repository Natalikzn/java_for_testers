// Пакет model — модели данных для тестов.
package model;

// GroupData — неизменяемая запись с данными группы: id, name, header, footer.
// Компилятор создаёт канонический конструктор, геттеры, equals/hashCode/toString по всем полям.
public record GroupData(String id, String name, String header, String footer) {
    // Конструктор без параметров: «пустая» группа, все поля — пустые строки.
    public GroupData() {
        // Делегирование каноническому конструктору записи.
        this("", "", "", "");
    }

    // «Билдер»: новый объект с другим id, остальные поля копируются.
    public GroupData withId(String id) {
        // Возвращаем новую запись: записи неизменяемы, поэтому нельзя просто присвоить поле.
        return new GroupData(id, this.name, this.header, this.footer);
    }

    // «Билдер»: новый объект с другим названием.
    public GroupData withName(String name) {
        // Копируем id, header, footer; подставляем новое name.
        return new GroupData(this.id, name, this.header, this.footer);
    }


    // «Билдер»: новый объект с другим header.
    public GroupData withHeader(String header) {
        return new GroupData(this.id, this.name, header, this.footer);
    }

    // «Билдер»: новый объект с другим footer.
    public GroupData withFooter(String footer) {
        return new GroupData(this.id, this.name, this.header, footer);
    }
}