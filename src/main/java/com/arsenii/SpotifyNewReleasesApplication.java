package com.arsenii;

import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.Paging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class SpotifyNewReleasesApplication {
    private final Logger logger = LoggerFactory.getLogger(SpotifyNewReleasesApplication.class);

    private String token;
    private String expiresIn;

    public static void main(String[] args) {
        SpringApplication.run(SpotifyNewReleasesApplication.class, args);
    }

    @GetMapping("/search")
    public String hello(@RequestParam(value = "artist") String artistName) throws IOException, ParseException, SpotifyWebApiException {
        try {
            authorize();
            logger.info("Spotify authorization successfully completed");
        } catch (IOException e) {
            final String message = "Cannot authorize in spotify: " + e.getMessage();
            logger.error(message);
            return message;
        }
        SpotifyApi api = SpotifyApi.builder()
                .setAccessToken(token)
                .build();
		final Paging<Artist> requestResult = api.searchArtists(artistName)
				.build()
				.execute();
        StringBuilder result = new StringBuilder();
        for (Artist artist : requestResult.getItems()) {
			final String genres = Arrays.stream(artist.getGenres())
					.filter(genre -> genre.contains("metal") || genre.contains("core") || genre.contains("death"))
					.collect(Collectors.joining(", "));
			if (!genres.isEmpty()) {
				result.append(String.format("%s - [%s]<br>", artist.getName(), genres));
			}
        }
        return result.toString();
    }

    final void authorize() throws IOException {
        final String clientId = Keys.loadSpotifyProperty(Keys.CLIENT_ID);
        final String clientSecret = Keys.loadSpotifyProperty(Keys.CLIENT_SECRET);

        URL url = new URL(Keys.TOKEN_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

        String data = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", clientId, clientSecret);
        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.lines().collect(Collectors.joining(System.lineSeparator()));

        this.token = JsonParser.parseString(response).getAsJsonObject().getAsJsonPrimitive("access_token").getAsString();
        this.expiresIn = JsonParser.parseString(response).getAsJsonObject().getAsJsonPrimitive("expires_in").getAsString();

        connection.disconnect();
    }
}
