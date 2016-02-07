import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class serverTestClient {
    
    String serverAddress = "moore11.cs.purdue.edu";
    
    private BufferedReader in;
    private PrintWriter out;
    //CREATE TABLE waypoints (id INTEGER, longe VARCHAR(256), lat VARCHAR(256),adjID VARCHAR(256), enterTime TIMESTAMP);
    
    public void addWaypointTest() throws IOException {
        Socket socket = new Socket(serverAddress, 3112);
        // Make connection and initialize streams
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println(14);
            
            JSONObject obj = new JSONObject();
            obj.put("longe", "1.2345");
            obj.put("lat", "1.2345");
            out.println(obj.toJSONString());
            
            //out.println(obj.toJSONString());
            //System.out.println(obj.toJSONString());
            
            // Consume the initial welcoming messages from the server
            String t;
            Object ob = null;
            JSONObject jsonObject;
            JSONParser parser = new JSONParser();
            
            while((t = in.readLine()) !=null)
            {
                try
                {
                    System.out .println(t);
                    ob = parser.parse(t);
                    jsonObject = (JSONObject) ob;
                    System.out.println(jsonObject.toJSONString());
                }catch (Exception e) {
                    System.out.println(e.toString());
                    out.println(e.toString());
                }
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }finally
        {
            socket.close();
        }
    }
    
    public void getWaypointsTest() throws IOException {
        System.out.println("IN getWaypointsTest");
        Socket socket = new Socket(serverAddress, 3112);
        // Make connection and initialize streams
        System.out.println("IN getWaypointsTest");
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println(10);
            out.flush();
            System.out.println("flushed");
            String t;
            Object ob = null;
            JSONObject jsonObject;
            JSONParser parser = new JSONParser();
            
            while((t = in.readLine()) !=null)
            {
                System.out.println("IN readline loop");
                try
                {
                    ob = parser.parse(t);
                    jsonObject = (JSONObject) ob;
                    System.out.println(jsonObject.toJSONString());
                }catch (Exception e) {
                    System.out.println(e.toString());
                    out.println(e.toString());
                }
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }finally
        {
            socket.close();
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        serverTestClient n = new serverTestClient();
        n.addWaypointTest();
        n.getWaypointsTest();
        
    }
}
