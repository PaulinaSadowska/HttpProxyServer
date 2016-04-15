import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by Paulina Sadowska on 14.04.2016.
 */
public class StatisticsField{
    private long numOfVisits;
    private long amountOfData;
    public static final String NUM_OF_VISITS_STR = "numOfVisits";
    public static final String AMOUNT_OF_DATA_STR = "amountOfData";

    public StatisticsField(long numOfVisits, long amountOfData){
        this.numOfVisits = numOfVisits;
        this.amountOfData = amountOfData;

    }

    public String toJSONString(){
        StringBuffer sb = new StringBuffer();

        sb.append("{");

        sb.append("\"" + JSONObject.escape(NUM_OF_VISITS_STR)+ "\"");
        sb.append(":");
        sb.append(numOfVisits);

        sb.append(",");

        sb.append("\"" + JSONObject.escape(AMOUNT_OF_DATA_STR)+ "\"");
        sb.append(":");
        sb.append(amountOfData);

        sb.append("}");

        return sb.toString();
    }

    public LinkedHashMap writeJSONString () throws IOException {
        LinkedHashMap obj = new LinkedHashMap();
        obj.put(NUM_OF_VISITS_STR, numOfVisits);
        obj.put(AMOUNT_OF_DATA_STR, amountOfData);
        return obj;

    }
}