package Entites;

public class OAuthUser {

    private final String provider;
    private final String id;
    private final String name;
    private final String email;

    public OAuthUser(String provider, String id, String name, String email) {
        this.provider = provider;
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

