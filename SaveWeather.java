import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.*;

// if "fetch" flag
//     download & parse weather from weather.com
//     save necessary into weather.txt file
//     print date
// if "
public class SaveWeather {
    private static final String jsonFile = "weather.json", SPACE_SEPARATOR = " ", NEWLINE = "\n\t\t",
            weatherURL = "http://api.wunderground.com/api//conditions/forecast10day/lang:EN/q/autoip.json";
    private static final int days = 7, lineMaxLength = 60;
    private static final Charset dataCharset = Charset.defaultCharset();
    private static JSONObject data;

    private static String breakLines(String input, int maxLineLength) {
        String[] tokens = input.split(SPACE_SEPARATOR);
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i];

            if (lineLen + (SPACE_SEPARATOR + word).length() > maxLineLength) {
                if (i > 0) {
                    output.append(NEWLINE);
                }
                lineLen = 0;
            }
            if (i < tokens.length - 1 && (lineLen + (word + SPACE_SEPARATOR).length() + tokens[i + 1].length() <=
                    maxLineLength)) {
                word += SPACE_SEPARATOR;
            }
            output.append(word);
            lineLen += word.length();
        }
        return output.toString();
    }

    private static JSONObject loadData(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new JSONObject(new String(encoded, dataCharset));
    }

    public static void main(String[] args) {
        if (args.length == 0)
            return;

        if (args[0].equals("-fetch")) {
            try {
                URL website = new URL(weatherURL);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(jsonFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                data = loadData(jsonFile);
                System.out.println(data.getJSONObject("current_observation").get("observation_time"));

            } catch (IOException e) {
                System.out.println("Fetch failed\n");
            }
        }
        else if (args[0].equals("-location")){
            try {
                data = loadData(jsonFile);
            } catch (IOException e) {
                System.out.println("Cannot find weather file");
            }
            System.out.println(data.getJSONObject("current_observation").getJSONObject("display_location").get("full"));
        }
        else if (args[0].equals("-forecast")) {
            int n = Integer.parseInt(args[1]);
            try {
                data = loadData(jsonFile);
            } catch (IOException e) {
                System.out.println("Cannot find weather file");
            }
            JSONObject simpleForescast = data.getJSONObject("forecast").getJSONObject("simpleforecast");
            JSONArray simpleArray = simpleForescast.getJSONArray("forecastday");
            JSONObject txtForescast = data.getJSONObject("forecast").getJSONObject("txt_forecast");
            JSONArray txtArray = txtForescast.getJSONArray("forecastday");
            JSONObject date = simpleArray.getJSONObject(n / 2).getJSONObject("date");
            if (n % 2 == 0) {
                System.out.print(date.get("weekday_short") + ", ");
                System.out.print(date.get("monthname") + ", ");
                System.out.print(date.get("day") + ": ");
            }
            else {
                System.out.print(txtArray.getJSONObject(n).get("title") + ": ");
            }
            System.out.println(breakLines((String)txtArray.getJSONObject(n).get("fcttext_metric"), lineMaxLength));
        }
    }
}
