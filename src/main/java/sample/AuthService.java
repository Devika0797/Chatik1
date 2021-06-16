package sample;

import java.sql.SQLException;

public interface AuthService {
    //запустить сервис
    void start();

    String getNickByLoginPass(String login, String password);

    //Остановить сервис
    void stop();

    // Получить никнейм
    // исправила по 2 заданию - добавила SQLException
    String getNickByLoginAndPass(String login, String pass) throws SQLException;
}
