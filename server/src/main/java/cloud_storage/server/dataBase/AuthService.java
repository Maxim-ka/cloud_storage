package cloud_storage.server.dataBase;

import java.sql.*;

public class AuthService {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                                                "login STRING NOT NULL," +
                                                "password STRING NOT NULL," +
                                                "catalog STRING NOT NULL);";
    private static final String REQUEST_CATALOG = "SELECT catalog FROM users WHERE login = ? AND password = ? ;";
    private static final String REQUEST_VERIFICATION = "SELECT %s FROM users WHERE %s = ? ;";
    private static final String REQUEST_REGISTRATION = "INSERT INTO users (login, password, catalog) VALUES (?, ?, ?);";
    private static final String CATALOG = "catalog";
    private static final String LOGIN = "login";
    private Connection connection;

    public synchronized void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:dataBaseServer.db");
        createTable();
    }

    private synchronized void createTable(){
        try (Statement statement = connection.createStatement()){
            statement.executeUpdate(CREATE_TABLE);
        }catch (SQLException e){
            System.out.println("Не удалось создать таблицу");
            e.printStackTrace();
        }
    }

    public synchronized void  disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getCatalogUser(String login, String password){
        try (PreparedStatement preRequestNickname = connection.prepareStatement(REQUEST_CATALOG)){
            preRequestNickname.setString(1, login.toLowerCase());
            preRequestNickname.setString(2, password.toLowerCase());
            ResultSet resultSet = preRequestNickname.executeQuery();
            if (resultSet.next()) return resultSet.getString(CATALOG);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public synchronized String registrationOfUsers(String login, String password, String nickname){
//        String answer;
//        answer = check(LOGIN, login);
//        if (answer != null) return login;
//        answer = check(NICKNAME, nickname);
//        if (answer != null) return nickname;
//        try (PreparedStatement preRequestForRegistration = connection.prepareStatement(REQUEST_REGISTRATION)){
//            preRequestForRegistration.setCommand(1, login.toLowerCase());
//            preRequestForRegistration.setCommand(2, password.toLowerCase());
//            preRequestForRegistration.setCommand(3, nickname);
//            if (preRequestForRegistration.executeUpdate() == 1) return null;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return password;
//    }

//    public synchronized String change(String oldNick, String newNick){
//        String answer = check(NICKNAME, newNick);
//        if (answer != null) return newNick;
//        try (PreparedStatement preRequestChangeNickname = connection.prepareStatement(REQUEST_CHANGE_NICKNAME)){
//            preRequestChangeNickname.setCommand(1, newNick);
//            preRequestChangeNickname.setCommand(2, oldNick);
//            if (preRequestChangeNickname.executeUpdate() == 1) return null;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return  oldNick;
//    }

    private synchronized String check(String nameCol, String string){
        try (PreparedStatement preRequestVerification = connection.prepareStatement(String.format(REQUEST_VERIFICATION, nameCol, nameCol))){
            preRequestVerification.setString(1, string.toLowerCase());
            ResultSet resultSet = preRequestVerification.executeQuery();
            if (resultSet.next()) return resultSet.getString(nameCol);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
