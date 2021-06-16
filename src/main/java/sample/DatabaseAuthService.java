package sample;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAuthService implements AuthService{

    private static final String DATABASE_URL = "jdbc:sqlite:javadb.db";
    private static Connection connection;
    private static Statement statement;
    private static List<Entry> entries;

    private static class Entry {
        private final String nickname;
        private final String login;
        private final String password;


        public Entry( String nickname, String login, String password) {
            this.nickname = nickname;
            this.login = login;
            this.password = password;

        }
    }

    public DatabaseAuthService() {
        entries = new ArrayList<>();
    }



    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        insertNewClientPS(entries);
        System.out.println(this.getClass().getName() + " server started");
    }

    @Override
    public String getNickByLoginPass(String login, String password) {
        for (Entry entry : entries) {
            if (entry.login.equals(login) && entry.password.equals(password)) return entry.nickname;
        }
        return null;
    }

    @Override
    public void stop() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(this.getClass().getName() + " server stopped");
    }

    private String getNick(String login, String pass) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("select nick from users where login is "
                + login + "' and pass is " + pass + " ")) {
            return resultSet.getString(1);
        }
    }
    @Override
    public String getNickByLoginAndPass(String login, String pass) throws SQLException {
        return getNick(login, pass);
    }

    public void insertNewClientPS(List<Entry> entries) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into clients (nick, login, password) values (?, ?, ?)")) {
            for (int i = 1; i <= entries.size(); i++) {
                preparedStatement.setString(1, entries.get(i - 1).nickname);
                preparedStatement.setString(2, entries.get(i - 1).login);
                preparedStatement.setString(3, entries.get(i - 1).password);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//    private static void createTable(String nick) throws SQLException {
//        String createTable = "create table (" +
//                "id integer not null primary key, " +
//                "nick varchar(30) not null, " +
//                "login varchar(30) not null, " +
//                "pass varchar(30))";
//        statement.execute(createTable);
//
//
//    }
//
//    private static void dropTable() throws SQLException {
//        String dropSql = "drop table";
//        statement.execute(dropSql);
//    }
//
//
//    private static void search() throws SQLException {
//        String sql = "select * from login";
//        ResultSet resultSet = statement.executeQuery(sql);
//        entries = new ArrayList<>();
//        while (resultSet.next()) {
//            entries.add(new Entry(
//                    resultSet.getString("login"),
//                    resultSet.getString("password"),
//                    resultSet.getString("nickname"))
//            );
//
//            System.out.println(
//                    resultSet.getInt(1) + " " +
//                            resultSet.getString("nickname") + " " +
//                            resultSet.getString("login") + " " +
//                            resultSet.getString("password"));
//        }
//    }




}
