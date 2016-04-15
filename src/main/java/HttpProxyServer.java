import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import sun.misc.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Created by Paulina Sadowska on 08.04.2016.
 */
public class HttpProxyServer
{
    private static StatisticsManager statsManager = new StatisticsManager("stats");
    private static final int PORT = 8000;
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 4000;
    private static final int STATUS_FORBIDDEN = 403;
    private static final String messageForbidden = "This website is on your black list! Get out!";

    public static void main(String[] args) throws Exception
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new ProxyHandler());
        System.out.println("Starting server on port: " + PORT);
        statsManager.add("joemonster.org", 14);
        statsManager.add("9gag.org", 14);
        server.start();
    }

    static class ProxyHandler implements HttpHandler {

        /* (non-Javadoc)
         * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
         */
        public void handle(HttpExchange exchange) throws IOException {


            //open URL connection
            URL requestUrl = exchange.getRequestURI().toURL();
            if(isOnBlackList(requestUrl.getHost(), readBlackListFile())){
                sendErrorResponse(exchange, STATUS_FORBIDDEN, messageForbidden.getBytes());
            }
            System.out.println("opening connection with "+ requestUrl);
            HttpURLConnection connection = (HttpURLConnection)requestUrl.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            //write exchange headers to connection
            writeURLConnectionHeaders(connection, exchange.getRequestHeaders());

            //set request method
            String requestMethod = exchange.getRequestMethod();
            connection.setRequestMethod(requestMethod);
            //if we want to send something to server
            if(requestMethod.equals("POST") || requestMethod.equals("PUT")) {
                writeExchangeBodyToURLConnection(connection, exchange.getRequestBody());
            }

            //connect then get input stream
            try{
                connection.connect();
            }
            catch(Exception e) {
                System.err.println("connection error: "+ e.getMessage());
            }

            InputStream response = null;
            try {
                int status = connection.getResponseCode();
                if(status>=200 &&status < 400){
                    response = connection.getInputStream();
                    sendExchangeResponseWithHeaders(exchange, connection.getHeaderFields(), response, status);
                }
                else{
                    response = connection.getErrorStream();
                    byte[] res = IOUtils.readFully(response, -1, true);
                    sendErrorResponse(exchange, status, res);
                }
            } catch(Exception e) {
                response = connection.getErrorStream();
                System.err.println("connection response error: " + e.getMessage());
                System.err.println("Response method: "+ connection.getRequestMethod());
            }

            if(response != null)
                response.close();

            connection.disconnect();



        }

        private ArrayList<String> readBlackListFile() throws IOException{
            ArrayList<String> blackList = new ArrayList<String>();
            try
            {
                FileReader fileReader = new FileReader("black_list");
                BufferedReader buffer = new BufferedReader(fileReader);
                String line;
                while((line = buffer.readLine()) != null){
                    URL url = new URL(line);
                    blackList.add(url.getHost());
                }
            }
            catch (IOException ex)
            {
                System.err.println("Exception thrown when reading file. \nMessage:\n"+ex.getMessage());
                throw ex;
            }
            return blackList;
        }

        private boolean isOnBlackList(String requestUrl, ArrayList<String> blackList){
            for(String address : blackList)
            {
                if(requestUrl.equals(address)){
                    return true;
                }
            }
            return false;

        }

        private void sendExchangeResponseWithHeaders(HttpExchange exchange, Map<String, List<String>> headerFields, InputStream response, int status) throws IOException {

            byte[] responseBytes = IOUtils.readFully(response, -1, true);
            OutputStream responseBody = exchange.getResponseBody();
            writeHTTPExchangeHeaders(exchange, headerFields, status, responseBytes.length);
            statsManager.add(exchange.getRequestURI().getHost(), responseBytes.length);
            responseBody.write(responseBytes);
            responseBody.close();

        }

        private void sendErrorResponse(HttpExchange exchange, int status, byte[] response) throws IOException {

            OutputStream responseBody = exchange.getResponseBody();
            exchange.sendResponseHeaders(status, response.length);
            responseBody.write(response);
            responseBody.close();
        }

        private void writeExchangeBodyToURLConnection(HttpURLConnection connection, InputStream requestBody) throws IOException {
            connection.setDoOutput(true);
            OutputStream out = connection.getOutputStream();
            out.write(IOUtils.readFully(requestBody, -1, true));
            out.close();
        }

        private int setResponseLength(int status, int responseLength) {
            if(status == HttpURLConnection.HTTP_NO_CONTENT || status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return  -1;
            }
            return responseLength;

        }

        private void writeURLConnectionHeaders(HttpURLConnection connection, Headers headers) {
            Set<String> keys = headers.keySet();
            for(String headerKey: keys)
            {
                List<String> values = headers.get(headerKey);
                for(String value: values){
                    connection.setRequestProperty(headerKey, value);
                    System.out.println(headerKey + " : " + value);
                }
            }
        }

        private void writeHTTPExchangeHeaders(HttpExchange exchange,  Map<String, List<String>> headers, int status, int responseLength) throws IOException {
            Set<String> keys = headers.keySet();
            boolean chunked = false;
            for(String headerKey: keys)
            {
                if(headerKey!=null){
                    List<String> values = headers.get(headerKey);
                    for(String value: values){
                        exchange.getResponseHeaders().set(headerKey, value);
                        System.out.println("header: "+headerKey+ " : " +value);
                    }
                    if(headerKey.equals("Transfer-Encoding")){
                        chunked = true;
                    }
                }
            }
            if(!chunked) {
                exchange.sendResponseHeaders(status, setResponseLength(status, responseLength));
            }
            else {
                exchange.sendResponseHeaders(status, 0);
            }
        }
    }

}
