package components;

public class UserLite {
    private final String username;
    private final String status;

    public UserLite(String username, String status){
        this.username = username;
        this.status = status;
    }

    public String getUsername(){
        return username;
    }

    public String getStatus(){
        return status;
    }
}
