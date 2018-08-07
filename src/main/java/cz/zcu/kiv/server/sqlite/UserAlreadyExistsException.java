package cz.zcu.kiv.server.sqlite;

public class UserAlreadyExistsException extends Throwable {
    public UserAlreadyExistsException(String email){
        super(email);
    }
}
