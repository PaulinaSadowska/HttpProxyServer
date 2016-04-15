/**
 * Created by Paulina Sadowska on 14.04.2016.
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StatisticsManager {

    private final String fileName;



    public StatisticsManager(String fileName){
        this.fileName = fileName;
    }

    public void add(String hostName, long amountOfData){

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = parser.parse(new FileReader(fileName));
            jsonObject = (JSONObject) obj;
        } catch (IOException e) {
            jsonObject = new JSONObject();
            System.err.println(e.getMessage());
        } catch (ParseException e) {
            jsonObject = new JSONObject();
            System.err.println(e.getMessage());
        }

        long numOfVisits = 0;
        if(entryExists(hostName)) {
            numOfVisits = readValue(hostName, StatisticsField.NUM_OF_VISITS_STR);
        }
        try {
            jsonObject.put(hostName, new StatisticsField(numOfVisits+1, amountOfData).writeJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            FileWriter file = new FileWriter("stats");
            file.write(jsonObject.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean entryExists(String hostName) {
        JSONParser parser = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader(fileName));
            JSONObject jsonObject = (JSONObject) obj;

            if(jsonObject.get(hostName)!=null)
                return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }



    private long readValue(String key, String childKey) {
        JSONParser parser = new JSONParser();
        long keyValue=0;
        try {

            Object obj = parser.parse(new FileReader(fileName));

            JSONObject jsonObject = (JSONObject) obj;

            JSONObject childJSONObject = (JSONObject) jsonObject.get(key);
            keyValue = (Long) childJSONObject.get(childKey);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return keyValue;
    }

}
