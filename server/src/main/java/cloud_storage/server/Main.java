package cloud_storage.server;

import cloud_storage.common.Rule;

import java.sql.SQLException;

class Main {

    public static void main(String[] args) {
        try {
            new Server(Rule.PORT).run();
        } catch (InterruptedException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
