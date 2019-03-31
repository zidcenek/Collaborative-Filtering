package cz.cvut.fit.vwm.collaborativefiltering.data.json;

import cz.cvut.fit.vwm.collaborativefiltering.data.model.Song;
import cz.cvut.fit.vwm.collaborativefiltering.data.model.User;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MockDataJsonParser {

    @NotNull
    private static String getFileContent(String fileName) {
        StringBuilder result = new StringBuilder();
        ClassLoader classLoader = MockDataJsonParser.class.getClassLoader();
        try {
            File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                result.append(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @NotNull
    public static List<Song> parseSongs(String jsonString) {
        List<Song> songs = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray tracks = json.getJSONObject("toptracks").getJSONArray("track");
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = (JSONObject) tracks.get(i);
                String title = track.getString("name");
                String mbid = track.getString("mbid");
                String artist = track.getJSONObject("artist").getString("name");
                int lastFmRank = track.getJSONObject("@attr").getInt("rank");
                String url = track.getString("url");
                songs.add(new Song(0, mbid, artist, title, lastFmRank, url));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return songs;
    }

    @NotNull
    public static List<User> parseUsers(String filename) {
        String jsonTxt = getFileContent(filename);
        List<User> users = new ArrayList<>();

        try {
            JSONArray ja = new JSONArray(jsonTxt);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject user = (JSONObject) ja.get(i);
                String name = user.getString("name");
                String surname = user.getString("surname");
                String email = user.getString("email");
                String password = user.getString("password");
                users.add(new User(0, name, surname, email, password));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return users;
    }
}