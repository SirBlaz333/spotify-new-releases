package com.arsenii;

import java.io.IOException;
import java.util.Properties;

public class Keys {
    public static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    public static final String CLIENT_ID = "Client_id";
    public static final String CLIENT_SECRET = "Client_secret";
    public static String loadSpotifyProperty(final String key) throws IOException {
        Properties properties = new Properties();
        properties.load(Keys.class.getClassLoader().getResourceAsStream("private/spotify-credentials.properties"));
        return properties.getProperty(key);
    }
}
