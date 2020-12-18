import java.sql.*;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class Library {
    Librarian librarian;
    Connection mySqlConnection;

    public Library() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/library?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";   //database
        // specific url.
        Properties properties = new Properties();
        properties.put("user", "root");
        properties.put("password", "root");
        mySqlConnection = DriverManager.getConnection(url, properties);
    }

    //SELECT name, title FROM rental join library.user on library.rental.user_userID = library.user.userID join library.book on book_bookID = bookID where endDate is null;
    void showUsersWithRentedBooks() throws SQLException {
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT name, startDate, title FROM rental JOIN library.user ON library.rental.user_userID = library.user.userID " +
                "JOIN library.book ON book_bookID = bookID WHERE endDate IS NULL;");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String userName = rs.getString("name");
            Date date = rs.getDate("startDate"); // ez nem tudom, mit fog eredményezni
            String bookTitle = rs.getString("title");
            System.out.println(userName + " " + date + "-n kivette a " + bookTitle + " című könyvet, és még nála van.");
        }
    }

    //SELECT name, title FROM rental join library.user on library.rental.user_userID = library.user.userID join library.book on book_bookID = bookID where endDate is null and name = ?;
    void showRentedBooksOfUser() throws SQLException {
        Scanner s = new Scanner(System.in);
        System.out.print("Melyik felhasználónál lévő könyvekre keressek? ");
        String userName = s.nextLine();
        PreparedStatement ps = mySqlConnection.prepareStatement(
                "SELECT name, title FROM rental JOIN library.user ON library.rental.user_userID = library.user.userID JOIN library.book ON book_bookID = bookID WHERE endDate IS NULL AND name = ?;");
        ps.setString(1, userName);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            String bookTitle = rs.getString("title");
            System.out.println(name + " kivette a " + bookTitle + " című könyvet, és még nála van.");
        }
    }

    // SELECT title, startDate, endDate FROM rental join library.book on book_bookID = bookID where title = ?
    void showRentHistoryOfBook() throws SQLException {
        Scanner s = new Scanner(System.in);
        System.out.print("Melyik könyv előzményére keressek? ");
        String book = s.next();
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT title, startDate, endDate FROM rental JOIN library.book ON book_bookID = bookID WHERE title = ?;");
        ps.setString(1, book);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String title = rs.getString("title");
            Date startDate = rs.getDate("startDate");
            Date endDate = rs.getDate("endDate");
            System.out.println("A(z) " + title + " című könyvet " + startDate + "-n kivették, " + endDate + "-n visszahozták.");
        }
    }

    // select title, user.name, startDate from rental join library.book on book_bookID = bookID join library.user on user_userID = userID where startDate = ?
    void showEveryRentOfADay() throws SQLException {
        Scanner s = new Scanner(System.in);
        System.out.print("Melyik nap eseményeire keressek? ");
//        Pattern date = new Pattern("\\d{4}-\\d{2}-\\d{2}");
        String startDate = s.next();
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT title, user.name, startDate, endDate FROM rental JOIN library.book ON book_bookID = bookID JOIN library" +
                ".user ON user_userID = userID WHERE startDate = ? OR endDate = ?;");
        ps.setString(1, startDate);
        ps.setString(2, startDate);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String title = rs.getString("title");
            String user = rs.getString("name");
            Date sDate = rs.getDate("startDate");
            Date eDate = rs.getDate("endDate");
            System.out.println("A(z) " + title + " című könyvet " + user + " kivette " + sDate + "-n, és visszahozta " + eDate + "-n");
        }
    }

    //select title, count(*) from rental join library.book on book_bookID = bookID group by book_bookID having count(*) = (select count(*) from rental GROUP BY book_bookID order by book_bookID desc limit 1);
    void showMostPopularBook() throws SQLException {
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT title, COUNT(*) FROM rental JOIN library.book ON book_bookID = bookID GROUP BY book_bookID HAVING COUNT" +
                "(*) = (SELECT COUNT(*) FROM rental GROUP BY book_bookID ORDER BY book_bookID DESC LIMIT 1);");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String title = rs.getString("title");
            String number = rs.getString("count(*)");
            System.out.println("A legnépszerűbb könyv a(z) " + title + ", " + number + " alkalommal vették ki.");
        }
    }

    //select (datediff(endDate, startDate)/(select count(*) from rental where endDate is not null)) from rental where endDate is not null;
    void showAverageRentTimeOfRentedBooks() throws SQLException {
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT CEIL(DATEDIFF(endDate, startDate)/(SELECT COUNT(*) FROM rental WHERE endDate IS NOT NULL)) AS result FROM" +
                " rental WHERE endDate IS NOT NULL;");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String result = rs.getString("result");
            System.out.println("A könyvek kölcsönzése átlagosan " + result + " napig tart.");
        }
    }

    //SELECT name, title FROM rental join library.user on library.rental.user_userID = library.user.userID join library.book on book_bookID = bookID where name = ?
    void showRentHistoryOfUser() throws SQLException {
        Scanner s = new Scanner(System.in);
        System.out.print("Melyik felhasználó előzményére keressek? ");
        String user = s.next();
        PreparedStatement ps = mySqlConnection.prepareStatement("SELECT name, startDate, endDate, title FROM rental JOIN library.user ON library.rental.user_userID = library" +
                ".user.userID JOIN library.book ON book_bookID = bookID WHERE name = ?;");
        ps.setString(1, user);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            Date startDate = rs.getDate("startDate");
            Date endDate = rs.getDate("endDate");
            String title = rs.getString("title");
            System.out.println("A(z) " + name + " nevű felhasználó " + startDate + "-n kivette, majd " + endDate + "-n visszahozta a " + title + " című könyvet.");
        }
    }

    void closeMySQLConnection () throws SQLException {
        mySqlConnection.close();
    }
}
