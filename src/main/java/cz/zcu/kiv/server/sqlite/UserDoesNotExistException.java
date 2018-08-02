package cz.zcu.kiv.server.sqlite;

public class UserDoesNotExistException extends Throwable {
    public UserDoesNotExistException(String email) {
        super(email);
    }
}
