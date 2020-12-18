import java.sql.SQLException;

public class Book {
    String title;
    int release; // Máté, ez az adatbázisban eredetileg LocalDate és releaseDate a neve, írd át!
    Condition condition;

    public Book(String title, int release, Condition condition) {
        this.title = title;
        this.release = release;
        this.condition = condition;
    }
}
