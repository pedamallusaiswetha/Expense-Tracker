import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TestAPI {

    public static void main(String[] args) throws Exception {

        URL url = new URL("http://localhost:8080/api/transactions");

        HttpURLConnection con =
            (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setDoOutput(true);

        con.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        );

        String data =
            "type=EXPENSE" +
            "&amount=250" +
            "&category=Travel" +
            "&description=Bus%20ticket" +
            "&date=2026-09-08";

        try (OutputStream output = con.getOutputStream()) {
            output.write(data.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = con.getResponseCode();

        System.out.println("Response Code: " + responseCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}