import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Librarian {
    public static void main(String[] args) throws SQLException {
        addBookToLibrary();
//        createNewUser();
    }

    public static void addBookToLibrary() throws SQLException {
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        Scanner sc = new Scanner(System.in);
        System.out.println("Add meg a könyv címét!");
        String bookTitle = sc.nextLine().toLowerCase();
        System.out.println("Add meg,hányadik kiadás!");
        String bookRealese = sc.nextLine();
        System.out.println("Add meg a könyv állapotát!(pl. GOOD)");
        String bookCondition = sc.nextLine();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `library`.`book` (`title`, `release`, `condition`) VALUES (?,?, ?)");
        preparedStatement.setString(1, bookTitle);
        preparedStatement.setString(2, bookRealese);
        preparedStatement.setString(3, bookCondition);
        int rows = preparedStatement.executeUpdate();
        System.out.println("kész ");


        System.out.println("Add meg az író nevét!");
        String authorName = sc.nextLine().toLowerCase();
//        System.out.println("Add meg az író születési évét");
//        String yearOfBirth = sc.nextLine();
//        preparedStatement = connection.prepareStatement("INSERT INTO `library`.`author` (`name`) VALUES (?)");
        preparedStatement = connection.prepareStatement("INSERT INTO `library`.`author` (`name`) \n" +
                "SELECT * FROM (SELECT ?) AS tmp\n" +
                "WHERE NOT EXISTS (\n" +
                "    SELECT name FROM author WHERE name = ?\n" +
                ") LIMIT 1;");
        preparedStatement.setString(1, authorName);
        preparedStatement.setString(2, authorName);

        rows = preparedStatement.executeUpdate();
        System.out.println("kész ");


        System.out.println("Most a rendszer összekapcsolja az író nevét a mű címével!");
        preparedStatement = connection.prepareStatement("INSERT INTO `library`.`book_has_author`" +
                " (`book_bookID`, `author_authorID`) " +
                "VALUES ((SELECT max(`bookID`) FROM `library`.`book`),'6' );\n");
//        SELECT max(`library`.`bookID`) FROM `library`.`book` where `title`=booktitle
//        preparedStatement.setString(1, bookTitle);
//        preparedStatement.setString(2, authorID);


        rows = preparedStatement.executeUpdate();
        System.out.println("kész ");
        connection.close();

//        kiíratás teszteléshez

//        Librarian me2 = new Librarian();
//        Connection connection2 = me2.getConnection();
//        PreparedStatement preparedStatement2 = connection2.prepareStatement("select title from book");
//        ResultSet resultSet2 = preparedStatement2.executeQuery();
//       while (resultSet2.next()){
//             String title =resultSet2.getString("title");
//           System.out.println(title);
//       }
//        connection2.close();

    }

    void discardBook() throws SQLException {
        System.out.println("Add meg a könyv Id számát,amit selejtezni szeretnél !");
        Librarian me2 = new Librarian();
        Connection connection2 = me2.getConnection();
        PreparedStatement preparedStatement2 = connection2.prepareStatement("select title from book");
        ResultSet resultSet2 = preparedStatement2.executeQuery();
       while (resultSet2.next()){
             String title =resultSet2.getString("title");
           System.out.println(title);
       }
        connection2.close();

    }

    //
//    void rentABook() {
//
//    }
//
//    void takeBackABook() {
//
//    }
//
    public static void createNewUser() throws SQLException {
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        Scanner sc = new Scanner(System.in);
        System.out.println("Add meg a teljes nevedet!");
        String fullName = sc.nextLine();
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `library`.`user` (`name`) VALUES (?)");
        preparedStatement.setString(1, fullName);

        int resultSet = preparedStatement.executeUpdate();
        System.out.println("kész ");
        connection.close();
    }

//       }
//   }

//    }

    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/library";
        Properties properties = new Properties();
        properties.put("user", "root");
        properties.put("password", "password");
        Connection connection =
                DriverManager.getConnection(url, properties);
        return connection;
    }
}