package sample;

import java.util.ArrayList;
import java.util.List;

/**
 * Простейшая реализация сервиса аутентификации, которая работает на встроенном списке
 */

public abstract class BaseAuthService implements AuthService{

    private class Entry {
        private final String nick;
        private final String login;
        private final String pass;

        public Entry(String nick, String login, String pass) {
            this.nick = nick;
            this.login = login;
            this.pass = pass;
        }
    }

    private final List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("nick1", "login1", "pass1"));
        entries.add(new Entry("nick2", "login2", "pass2"));
        entries.add(new Entry("nick3", "login3", "pass3"));
    }



    @Override
    public void start() {
        System.out.println(this.getClass().getName() + " server started");
    }

    @Override
    public void stop() {
        System.out.println(this.getClass().getName() + " server stopped");
    }

    @Override
    public String getNickByLoginAndPass(String login, String pass) {
        for (Entry entry : entries) {
            if (entry.login.equals(login) && entry.pass.equals(pass))
                return entry.nick;
        }
        return null;
    }


}
