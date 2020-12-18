import java.time.LocalDate;

public class Rental {
    User user;
    LocalDate startOfRent;
    LocalDate endOfRent;
    Book book;

    public Rental(User user, LocalDate startOfRent, LocalDate endOfRent, Book book) {
        this.user = user;
        this.startOfRent = startOfRent;
        this.endOfRent = endOfRent;
        this.book = book;
    }
}