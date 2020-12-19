import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class Librarian {
    public static void main(String[] args) throws SQLException {
//        addBookToLibrary();
//        createNewUser();
//        discardBook();
//        rentABook();
        takeBackABook();
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
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `library`.`book` (`title`," +
                " `release`, `condition`) VALUES (?, ?, ?);");
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
                " (`book_bookID`, `author_authorID`) VALUES ((SELECT max(`bookID`) FROM `library`.`book`), (SELECT authorID FROM library.author where name = ?));\n");
        preparedStatement.setString(1, authorName);
//        SELECT max(`bookID`) FROM `library`.`book`
//        preparedStatement.setString(2, authorID);
        rows = preparedStatement.executeUpdate();
        System.out.println("kész ");

        System.out.println("Add meg a könyv műfajait, vesszővel elválasztva!");
        String bookThemes = sc.nextLine().toLowerCase();
        String[] parts = bookThemes.split(",");
        for (int i = 0; i < parts.length; i++) {
            String oneTheme = parts[i].trim();
            preparedStatement = connection.prepareStatement("INSERT INTO `library`.`theme` (`theme`) \n" +
                    "SELECT * FROM (SELECT ?) AS tmp\n" +
                    "WHERE NOT EXISTS (\n" +
                    "    SELECT theme FROM theme WHERE theme = ?\n" +
                    ") LIMIT 1;");
            preparedStatement.setString(1, oneTheme);
            rows = preparedStatement.executeUpdate();
            System.out.println("kész ");

            System.out.println("Most a rendszer összekapcsolja az adott műfajokat a mű címével!");
            preparedStatement = connection.prepareStatement("INSERT INTO `library`.`book_has_theme`" +
                    " (`book_bookID`, `theme_themeID`) VALUES ((SELECT max(`bookID`) FROM `library`.`book`), (SELECT themeID FROM library.theme where theme = ?));\n");
            preparedStatement.setString(1, oneTheme);
            rows = preparedStatement.executeUpdate();
            System.out.println("kész ");

        }
        connection.close();

//        kiíratás teszteléshez
//
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

    public static void discardBook() throws SQLException {
        System.out.println("Add meg a könyv Id számát,amit selejtezni szeretnél !");
        Scanner sc = new Scanner(System.in);
        int bookId = Integer.parseInt(sc.nextLine());
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select title,book.release,book.condition from book where bookID=? ");
        preparedStatement.setInt(1, bookId);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String title = resultSet.getString("title");
            String release = resultSet.getString("book.release");
            String condition = resultSet.getString("book.condition");
            System.out.println(title + "" + release + "" + condition);
        }
        System.out.println("Selejtezni szeretnéd ezt a könyvet? (i/n)");
//        a nem válasz nincs lekezelve
        sc = new Scanner(System.in);
        String yesOrNo = sc.nextLine();
        if (yesOrNo.equals("i")) {
//            SET FOREIGN_KEY_CHECKS=0; -- to disable them
//            SET FOREIGN_KEY_CHECKS=1; -- to re-enable them

            preparedStatement = connection.prepareStatement(" UPDATE `book` \n" +
                    "SET \n" +
                    "    `condition` = 'DISCARDED'\n" +
                    "WHERE\n" +
                    "    `bookID` = ?;");
//                     "REPLACE INTO `book`(`bookID`,`condition` )\n" +
//                     "            VALUES(2,'DISCARDED');\n" +
//                    "");
            preparedStatement.setInt(1, bookId);
            int row = preparedStatement.executeUpdate();
        }
        connection.close();


    }

    //
    public static void rentABook() throws SQLException {
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        Scanner sc = new Scanner(System.in);
        System.out.println("Add meg a nevedet!");
        String userName = sc.nextLine().toLowerCase();
        System.out.println("Add meg a könyv kódját!");
        int realBookID = Integer.parseInt(sc.nextLine());
// ez itt nem fontos, csak ellenőrzi, hogy jó könyv van- e a kódhoz csatolva
        System.out.println("Add meg a könyv címét!");
        String bookTitle = sc.nextLine().toLowerCase();
        System.out.println("Add meg,hogy hányadik kiadás!");
        int release = Integer.parseInt(sc.nextLine());
//        idáig
        System.out.println("Add meg a mai dátumot (pl. 2000-02-22)");
        String startingLocalDate = sc.nextLine();

        PreparedStatement allowToSetForeignKeys = connection.prepareStatement("SET GLOBAL FOREIGN_KEY_CHECKS=0;");
        int rows1 = allowToSetForeignKeys.executeUpdate();
// fontos, hogy egy user csak egyszer szerepeljen az adatbázisban
        PreparedStatement getUserIdFromName = connection.prepareStatement("select userId from user where name= ? ");
        getUserIdFromName.setString(1, userName);

        ResultSet resultSet = getUserIdFromName.executeQuery();
        resultSet.next();
        int userId = resultSet.getInt("userId");
        System.out.println(userId + " " + userName);


        PreparedStatement getBookIdFromTitle = connection.prepareStatement("select bookID from book where book.title= ? and book.release=? ");
        getBookIdFromTitle.setString(1, bookTitle);
        getBookIdFromTitle.setInt(2, release);
        ResultSet resultSet2 = getBookIdFromTitle.executeQuery();
//        ez is csak ellenőrzés
        while (
                resultSet2.next()) {
            int bookId = resultSet2.getInt("bookID");
            System.out.println(bookId + " " + bookTitle);
        }
//        idáig
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `library`.`rental` (`startdate`," +
                " `user_userID`, `book_bookID`) VALUES (?, ?, ?);");
        preparedStatement.setString(1, startingLocalDate);
        preparedStatement.setInt(2, userId);
        preparedStatement.setInt(3, realBookID);
        int rows = preparedStatement.executeUpdate();
        System.out.println("kész ");

        PreparedStatement allowToSetForeignKeys2 = connection.prepareStatement("SET GLOBAL FOREIGN_KEY_CHECKS=1;");
        int rows2 = allowToSetForeignKeys2.executeUpdate();
    }

//  :(  engedélyezi , hogy ugyanazt az id-jű könyvet kivegyük,mielőtt vissza lenne hozva :(

    //
    public static void takeBackABook() throws SQLException {
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        Scanner sc = new Scanner(System.in);
        System.out.println("Add meg a kölcsönző teljes nevét !");
        String userName = sc.nextLine().toLowerCase();
        System.out.println("Add meg a könyv kódját!");
        int realBookID = Integer.parseInt(sc.nextLine());
//    szintén csak ellenőrzésend
        System.out.println("Add meg a könyv címét  !");
        String bookTitle = sc.nextLine().toLowerCase();
        System.out.println("Add meg,hogy hányadik kiadás!");
        int release = Integer.parseInt(sc.nextLine());
//       idáig

        PreparedStatement allowToSetForeignKeys = connection.prepareStatement("SET GLOBAL FOREIGN_KEY_CHECKS=0;");
        int rows1 = allowToSetForeignKeys.executeUpdate();

        PreparedStatement getUserIdFromName = connection.prepareStatement("select userID from user where name= ? ");
        getUserIdFromName.setString(1, userName);
        ResultSet resultSet = getUserIdFromName.executeQuery();
        resultSet.next();
        int userId = resultSet.getInt("userID");
        System.out.println(userId + " " + userName);

        PreparedStatement getBookIdFromTitle = connection.prepareStatement("select bookID from book where book.title= ? and book.release=?");
        getBookIdFromTitle.setString(1, bookTitle);
        getBookIdFromTitle.setInt(2, release);
        ResultSet resultSet2 = getBookIdFromTitle.executeQuery();
        while (resultSet2.next()) {
            int bookId = resultSet2.getInt("bookID");
            System.out.println(bookId + " " + bookTitle);
        }
//        ez teszteli, hogy az enddate null
        PreparedStatement endingDateTest = connection.prepareStatement("select idrental from rental where book_bookID=? and " +
                "user_userID=? and enddate is null;  ");
        getBookIdFromTitle.setInt(1, realBookID);
        getBookIdFromTitle.setInt(2, userId);
        ResultSet resultSet3 = getBookIdFromTitle.executeQuery();
        while (resultSet3.next()) {
            int idrental = resultSet3.getInt("idrental");
            System.out.println(idrental + " " + bookTitle);
        }
        System.out.println("Eddig minden rendben? (i/n)");
// a nem válasz nincs lekezelve itt s még egy helyen asszem

        if (sc.nextLine().equals("i")) {
            System.out.println("Add meg a mai dátumot!");
            String endDateLocalDate = sc.nextLine();
            PreparedStatement preparedStatement = connection.prepareStatement(" UPDATE `rental`" +
                    " SET `endDate`=? WHERE `user_userID`=? " +
                    "and `book_bookID`=? and `endDate` is null");
            preparedStatement.setString(1, endDateLocalDate);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, realBookID);
            int rows = preparedStatement.executeUpdate();
            System.out.println("kész ");

            PreparedStatement allowToSetForeignKeys2 = connection.prepareStatement("SET GLOBAL FOREIGN_KEY_CHECKS=1;");
            int rows2 = allowToSetForeignKeys2.executeUpdate();
        }
    }

    public static void createNewUser() throws SQLException {
        Librarian me = new Librarian();
        Connection connection = me.getConnection();
        Scanner sc = new Scanner(System.in);
        System.out.println("Add meg az új tag teljes nevét!");
        String fullName = sc.nextLine().toLowerCase();
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