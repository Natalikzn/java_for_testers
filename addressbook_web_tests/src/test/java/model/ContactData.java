// Пакет model — модели данных, используемые в тестах, генераторе и хелперах.
package model;

import java.util.LinkedHashMap; // LinkedHashMap — карта «атрибут → значение» с предсказуемым порядком ключей.
import java.util.Map; // Map — интерфейс карты, которую возвращает comparableAttributes().
import java.util.Objects; // Objects — утилита для вычисления hashCode (Objects.hash).

// ContactData — неизменяемая запись с данными контакта.
//
// ВАЖНО (задание 14): раньше запись содержала только те поля, которые видны в списке
// контактов в веб-интерфейсе (id, firstname, lastname, nickname, address, photo).
// Теперь проверки списков делаются по данным из БД, поэтому модель расширена «плоскими»
// атрибутами контакта, которые реально есть в таблице addressbook.
// Это позволяет сравнивать то, что в интерфейсе в списке контактов вообще не показывается:
// отчество, должность, компанию, все телефоны, факс, три e-mail и домашнюю страницу.
//
// Состав полей строго соответствует колонкам таблицы addressbook:
//   id, firstname, middlename, lastname, nickname, company, title, address,
//   home, mobile, work, fax, email, email2, email3, homepage,
//   bday, bmonth, byear, aday, amonth, ayear, photo, created, modified, deprecated.
// НЕ сравниваем «сложные» атрибуты:
//   * photo — двоичные данные картинки: сравнение требует чтения blob и побайтового
//     сопоставления, а приложение может перекодировать изображение при загрузке;
//   * дни/месяцы/годы рождения и годовщины (bday/bmonth/byear, aday/amonth/ayear) —
//     в приложении это выпадающие списки с нечисловыми месяцами, а в БД числа,
//     что даёт лишние расхождения без пользы для проверки;
//   * created/modified/deprecated — служебные колонки приложения, к данным контакта
//     отношения не имеют.
// Поэтому таких компонентов в сравнении просто нет: то, чего нет в сравнении,
// физически не может случайно попасть в проверку.
//
// Компилятор сам создаёт геттеры id(), firstname(), ... и toString().
public record ContactData(
        // --- идентификация ---
        String id,          // первичный ключ из БД (в модели хранится строкой, как в UI)
        // --- имя ---
        String firstname,   // имя
        String middlename,  // отчество (в списке контактов UI не показывается)
        String lastname,    // фамилия
        String nickname,    // никнейм
        // --- работа ---
        String title,       // должность
        String company,     // компания
        // --- адрес (единственный: отдельной колонки address2 в таблице нет) ---
        String address,     // адрес
        // --- телефоны (отдельной колонки phone2 в таблице нет) ---
        String home,        // домашний телефон
        String mobile,      // мобильный телефон
        String work,        // рабочий телефон
        String fax,         // факс
        // --- интернет ---
        String email,       // e-mail (первый)
        String email2,      // e-mail (второй)
        String email3,      // e-mail (третий)
        String homepage,    // домашняя страница
        // --- «сложный» атрибут, который хранится, но НЕ участвует в сравнении ---
        String photo        // путь к файлу при создании контакта; в БД это blob
) {
    // Компактный конструктор записи: он выполняется перед присваиванием компонентов.
    // Приводим null к пустой строке, потому что:
    //   * в БД незаполненные текстовые колонки могут быть NULL, а Hibernate вернёт null;
    //   * JSON/XML с неполным набором полей тоже даёт null для отсутствующих компонентов.
    // Без такой нормализации сравнение «null против ""» падало бы на пустых полях,
    // хотя фактически данные совпадают.
    public ContactData {
        id = normalize(id);
        firstname = normalize(firstname);
        middlename = normalize(middlename);
        lastname = normalize(lastname);
        nickname = normalize(nickname);
        title = normalize(title);
        company = normalize(company);
        address = normalize(address);
        home = normalize(home);
        mobile = normalize(mobile);
        work = normalize(work);
        fax = normalize(fax);
        email = normalize(email);
        email2 = normalize(email2);
        email3 = normalize(email3);
        homepage = normalize(homepage);
        photo = normalize(photo);
    }

    // Вспомогательный метод нормализации: null → "", любое другое значение без изменений.
    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    // Конструктор без параметров: «пустой» контакт, все поля — пустые строки.
    // Нужен для удобного старта цепочки билдеров: new ContactData().withFirstName(...).
    public ContactData() {
        // Делегируем каноническому конструктору записи; this(...) должен быть первым оператором.
        this("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    // Совместимый конструктор на 6 параметров (id, имя, фамилия, никнейм, адрес, фото).
    // Оставлен, чтобы старый код вида new ContactData("", "", "", "", "", "") продолжал
    // компилироваться: остальные (новые) атрибуты получают пустые значения.
    public ContactData(String id, String firstname, String lastname, String nickname, String address, String photo) {
        this(id, firstname, "", lastname, nickname, "", "", address, "", "", "", "", "", "", "", "", photo);
    }

    // ============================ Билдеры (with*) ============================
    // Записи неизменяемы, поэтому каждый with* возвращает НОВЫЙ объект,
    // у которого изменён только один атрибут, а все остальные скопированы из текущего.

    // Новый объект с другим id (используется, когда БД присвоила контакту идентификатор).
    public ContactData withId(String id) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим именем.
    public ContactData withFirstName(String firstname) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим отчеством.
    public ContactData withMiddleName(String middlename) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другой фамилией.
    public ContactData withLastName(String lastname) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим никнеймом.
    public ContactData withNickname(String nickname) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другой должностью.
    public ContactData withTitle(String title) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другой компанией.
    public ContactData withCompany(String company) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим адресом.
    // Имя withAddressName сохранено исторически (так метод называется в проекте),
    // рядом добавлен короткий псевдоним withAddress — оба делают одно и то же.
    public ContactData withAddressName(String address) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Псевдоним withAddressName для читаемости в новых тестах.
    public ContactData withAddress(String address) {
        return withAddressName(address);
    }

    // Новый объект с другим домашним телефоном.
    public ContactData withHome(String home) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим мобильным телефоном.
    public ContactData withMobile(String mobile) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим рабочим телефоном.
    public ContactData withWork(String work) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим факсом.
    public ContactData withFax(String fax) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим первым e-mail.
    public ContactData withEmail(String email) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект со вторым e-mail.
    public ContactData withEmail2(String email2) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с третьим e-mail.
    public ContactData withEmail3(String email3) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другой домашней страницей.
    public ContactData withHomepage(String homepage) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // Новый объект с другим фото (путь к файлу при загрузке через UI).
    public ContactData withPhoto(String photo) {
        return new ContactData(id, firstname, middlename, lastname, nickname, title, company, address,
                home, mobile, work, fax, email, email2, email3, homepage, photo);
    }

    // ===================== Сравнение «по всем атрибутам» =====================
    // Возвращает карту «имя атрибута → значение» для всех атрибутов, которые имеют смысл
    // при сверке с БД. Фото сюда сознательно НЕ входит (см. комментарий в начале класса).
    //
    // Зачем карта, а не прямое сравнение record-объектов:
    //   1) equals ниже сравнивает контакты ТОЛЬКО по id, поэтому assertEquals(list1, list2)
    //      проверил бы лишь идентификаторы, но не имена/телефоны/адреса;
    //   2) карта даёт понятный diff при падении: JUnit печатает, какие именно атрибуты
    //      не совпали, а не «два разных объекта ContactData».
    public Map<String, String> comparableAttributes() {
        // LinkedHashMap сохраняет порядок вставки — сообщения об ошибках читаются предсказуемо.
        var attributes = new LinkedHashMap<String, String>();
        attributes.put("id", id);                 // идентификатор тоже сверяем: он ловит подмену контакта
        attributes.put("firstname", firstname);
        attributes.put("middlename", middlename);
        attributes.put("lastname", lastname);
        attributes.put("nickname", nickname);
        attributes.put("title", title);
        attributes.put("company", company);
        attributes.put("address", address);
        attributes.put("home", home);
        attributes.put("mobile", mobile);
        attributes.put("work", work);
        //attributes.put("fax", fax);
        attributes.put("email", email);
        attributes.put("email2", email2);
        attributes.put("email3", email3);
        attributes.put("homepage", homepage);
        // photo не добавляем: бинарные данные картинки сравнивать не будем.
        return attributes;
    }

    // Переопределение equals: контакты сравниваются ТОЛЬКО по id.
    // Важно помнить: полноценная сверка списков делается через comparableAttributes()
    // (см. tests/TestBase#assertContactListEquals), а не через equals.
    @Override
    public boolean equals(Object other) {
        // Если это тот же самый объект (одна ссылка) — сразу равны (быстрая проверка).
        if (this == other) {
            return true;
        }
        // pattern matching instanceof: проверяем тип и сразу приводим к ContactData в переменную contact.
        if (other instanceof ContactData contact) {
            // Равны, если id не null и совпадает с id другого объекта.
            return id != null && id.equals(contact.id);
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
