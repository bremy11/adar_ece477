
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//sql database setup commands 
//CREATE DATABASE adar_database;
//USE adar_database;
//CREATE TABLE waypoints (id INTEGER, longe VARCHAR(256), lat VARCHAR(256),adjID VARCHAR(256), enterTime TIMESTAMP);

//sample insert for testing

//INSERT INTO waypoints Values (1, '3.1415', '3.1415', '2,3', UTC_TIMESTAMP());
//INSERT INTO waypoints Values (2, '2.2222', '2.2222', '1,3', UTC_TIMESTAMP());
//INSERT INTO waypoints Values (3, '3.3333', '3.3333', '1,2', UTC_TIMESTAMP());

//ip is for moore11.cs.purdue.edu


/*API calls enum
 * 
 * 1, get path
 * 2, database requests current gps waypoint
 * 3, rover comes on line
 * 4, database requests delivery
 * 5, rover at destination
 * 
 * Client side protocols
 * 6, get all waypoints
 * 7, update waypoint connection, connect
 * 8, delete waypoint
 * 9, request delivery
 * 
 */
///////////////////////////// Mutlithreaded Server /////////////////////////////

public class boilerServer 
{
    final static int port = 3112;
    //arbitrarily selected port
    
    /**
     *Usage is useless now that we will be using JSON objects
     */
    /*static void printUsage() {
     System.out.println("In another window type:");
     System.out.println("telnet sslabXX.cs.purdue.edu " + port);
     System.out.println("GET-ALL-EVENTS");
     System.out.println("GET-EVENT-INFO|id");
     System.out.println("ADD-EVENT");
     }*/
    
    //main
    public static void main(String[] args )
    {  
        try
        {  
            //printUsage();
            int i = 1;
            ServerSocket s = new ServerSocket(port);
            while (true)
            {  
                Socket incoming = s.accept();
                System.out.println("Spawning " + i);
                Runnable r = new ThreadedHandler(incoming, i);
                Thread t = new Thread(r);
                t.start();
                i++;
            }
        }
        catch (IOException e)
        {  
            e.printStackTrace();
        }
    }
}

/**
 This class handles the client input for one server socket connection. 
 */
class ThreadedHandler implements Runnable
{ 
    final static String ServerUser = "root";
    final static String ServerPassword = "1827";
    int runNum;
    private Socket incoming;
    
    public ThreadedHandler(Socket newSoc, int iteration)
    { 
        incoming = newSoc;
        runNum = iteration;
    }
    
    public static Connection getConnection() throws SQLException, IOException
    {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream("database.properties");
        props.load(in);
        in.close();
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null)
            System.setProperty("jdbc.drivers", drivers);
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        
        System.out.println("url="+url+" user="+ServerUser+" password="+ServerPassword);
        
        return DriverManager.getConnection( url, ServerUser, ServerPassword);
    }
    
    
    /**
     *This function will send all of the current events
     *  that are in the db to populate the app's map.
     *  Here we use JSON objects to store and send the data
     */
    void sendWaypointPath(PrintWriter out) {
        
        Connection conn=null;
        try
        { 
            String numPoints = null;
            conn = getConnection();
            Statement q1 = conn.createStatement();
            Statement q2 = conn.createStatement();
            
            ResultSet r1 = q1.executeQuery("SELECT COUNT(id) FROM waypoints");
            ResultSet r2 = q2.executeQuery( "SELECT longe, lat, adjID FROM waypoints");
            
            //Create a JSON Obj
            JSONObject obj = new JSONObject();
            
            //Get the current number of events
            while(r1.next()) {
                numPoints = r1.getString(1);
            }
            
            //send the events to the app
            
            /*
            while(r2.next()) {
                obj.put("eventCount",  numEvents);
                obj.put("id",    r2.getString(1));
                obj.put("name",  r2.getString(2));
                obj.put("longe", r2.getString(3));
                obj.put("location",  r2.getString(4));
                obj.put("description",  r2.getString(5));
                //might have to handle these differently since these are timestamps
                obj.put("startTime",  r2.getString(6));
                obj.put("endTime",  r2.getString(7));
                ////
                obj.put("numAttendees", r2.getString(8));
                obj.put("lat", r2.getString(9));
                //send event
                System.out.println(obj.toJSONString());
                //out.println("READING");
                out.println(obj.toJSONString());
            }*/
            
            StringBuilder message = new StringBuilder();
            message.append('{');
            message.append(numPoints);
            
             while(r2.next()) {
                message.append('(');
                message.append(r2.getString(1));
                message.append(',');
                message.append(r2.getString(2));
                message.append(')');
            }
             
             message.append('}');
            out.println(message.toString());
           
            r1.close();
            r2.close();
            
        }
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally
        {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {
            }
        }
    }
    
    
    void getAllWaypoints(PrintWriter out) {
        
        Connection conn=null;
        try
        { 
            String numPoints = null;
            conn = getConnection();
            Statement q1 = conn.createStatement();
            Statement q2 = conn.createStatement();
            
            ResultSet r1 = q1.executeQuery("SELECT COUNT(id) FROM waypoints");
            ResultSet r2 = q2.executeQuery( "SELECT * FROM waypoints");
            
            //Create a JSON Obj
            JSONObject obj = new JSONObject();
            
            //Get the current number of events
            while(r1.next()) {
                numPoints = r1.getString(1);
            }
            
            //send the events to the app
            
            
            while(r2.next()) {
                obj.put("numEvents",  numEvents);
                obj.put("id",    r2.getString(1));
                obj.put("longe", r2.getString(2));
                obj.put("lat", r2.getString(3));
                obj.put("enterTime",  r2.getString(4));
                
                //send event
                System.out.println(obj.toJSONString());
                //out.println("READING");
                out.println(obj.toJSONString());
            }
          
            
            r1.close();
            r2.close();
            
        }
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally
        {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {
            }
        }
    }
    
    void updateAdj( JSONObject obj, PrintWriter out) {
        
        /*System.out.println();
         System.out.println(obj.toJSONString());
         System.out.println(); */
        
        Connection conn=null;
        try
        { 
            String numEvents = null;
            conn = getConnection();
            
            
            //get a conncetion
            
            //set the prepared statement
            PreparedStatement pstmt = conn.prepareStatement("UPDATE waypoints SET numAttendees=? WHERE id LIKE ?");
            
            //pstmt.setString(1, (Integer) obj.get("id"));
            
            //System.out.println("id = " + String.format("%d",obj.get("id")));
            pstmt.setString(1, obj.get("adjID")));
            pstmt.setString(2, String.format("%d",obj.get("id")));
            //execute the query
            //System.out.println(pstmt);
            
            pstmt.executeUpdate();
            
            //close the result
            
        }
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally
        {
            try {if (conn!=null) conn.close();}
            catch (Exception e) {}
        }
    }
    /**
     *This function will get the total number of events
     */ 
    void getCount(PrintWriter out) {
        Connection conn=null;
        try
        { 
            //get a connection
            conn = getConnection();
            //set the prepared statement
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(id) FROM events");
            //execute the query
            ResultSet result = pstmt.executeQuery();
            
            //create a new JSON object
            JSONObject query = new JSONObject();
            //populate & send the JSON object
            while(result.next()) {
                query.put("numEvents", result.getString(1));
                out.println(query.toJSONString());
            }
            result.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally
        {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {
            }
        }
    } 
    /**
     *This will add a new event to the database
     */
    void addWaypoint(JSONObject obj, PrintWriter out) {
        Connection conn = null;
        
        try{
            //get a connection & set it to change the db automatically
            conn = getConnection();
            
            String eventId = null;
            conn = getConnection();
            
            
            String numEvents = null;
            conn = getConnection();
            
            Statement q1 = conn.createStatement();
            ResultSet r1 = q1.executeQuery("select ifnull(max(id), 1)+1 from events");
            while(r1.next()) {
                eventId = r1.getString(1);
                if (eventId==null){ eventId = "1";}
            }
            //System.out.println("numEvents = " + numEvents);
            r1.close();

            conn.setAutoCommit(true);
            String sql = "INSERT INTO events VALUES(?,?,?,?, UTC_TIMESTAMP())";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            //get all info from the JSON object 
            pstmt.setString(1,eventId);
            pstmt.setString(2,(String) obj.get("longe"));
            pstmt.setString(3,(String) obj.get("lat"));
            pstmt.setString(4,(String) obj.get("adjID"));
            
            System.out.println(pstmt);
            //update the db
            pstmt.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {}
        }
    }
    
    void deleteWaypoint(JSONObject obj, PrintWriter out) {
        Connection conn = null;
        
        try{
            //get the connection
            conn = getConnection();
            //set the connection to autocommit changes to the db
            conn.setAutoCommit(true);
            //set the prepared statement
            String sql = "DELETE FROM events WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,(String) obj.get("id"));
            //update the db
            pstmt.executeUpdate();
        }
        catch(Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
        }
        finally {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {}
        }
    }
    
    void handleRequest( InputStream inStream, OutputStream outStream) 
    {
        
        //rmOldEvents();
        Scanner in = new Scanner(inStream);         
        PrintWriter out = new PrintWriter(outStream, true /* autoFlush */);
        
        // Get parameters of the call
        String request = "fail";
        
        int requestInt;
        
        if(in.hasNextLine()){
            requestInt=in.nextInt();
        }else {
            return;
        }
         if(in.hasNextLine()){
             request=in.nextLine();
         }
         
        /*API calls enum
         * 
         * /////1, get path
         * ////2, database requests current gps waypoint
         * 3, rover comes on line
         * ///////4, database requests delivery
         * 5, rover at destination
         * 
         * Client side protocols
         * 6, get all waypoints
         * 7, update waypoint connection, connect
         * 8, delete waypoint
         * 9, request delivery
         * 
         */
        
        
        
        if (requestInt == 1){
            //1, get path
            
            sendWaypointPath(out);
            return;
        }else if(requestInt == 2){
            //2, database requests current gps waypoint
        }else if(requestInt == 3){
            //3, rover comes on line
            
        }else if(requestInt == 4){
            //4, database requests delivery
        }else if(requestInt == 5){
            //5, rover at destination
        }else if(requestInt == 6){
            //6, get all waypoints
        }else if(requestInt == 7){
            //7, update waypoint connection, connect
        }else if(requestInt == 8){
            //8, delete waypoint
        }else if(requestInt == 9){
            //9, request delivery
        }else {
            //invalid communication
            System.out.println("ERROR, INVALID REQUEST");
        }
        Object obj = null;
        JSONParser parser = new JSONParser();
        try{
            obj = parser.parse(request);
        }catch(Exception e)
        {
            out.println("ERROR: handleRequest couldn't parse JSON" + e.toString());
        }
        
        //get the command from the JSON object 
        JSONObject jsonObject = (JSONObject) obj;
        System.out.println(jsonObject.toJSONString());
        String req = (String) jsonObject.get("command");
        
        System.out.println("req = " +req);
        try {
            //perform the requested operation
            if (req.equals("GET-ALL-EVENTS")) {
                //System.out.println("line = 0");
                //getAllEvents(out);
            }else if (req.equals("GET-EVENT-INFO")) {
                //System.out.println("line = 1");
                getEventInfo(jsonObject, out);
            }else if (req.equals("GET-CNT")) {
                //System.out.println("line = 2");
                getCount(out);
            }else if (req.equals("ADD-EVENT")) {
                //System.out.println("line = 3");
                addEvent(jsonObject, out);
            }else if (req.equals("DEL-EVENT")) {
                //System.out.println("line = 4");
                deleteEvent(jsonObject, out);
            }else if ( req.equals("ATTEND-EVENT")){
                //System.out.println("line = 5");
                attendEvent(jsonObject, out);
            }
            System.out.println();
        }
        catch (Exception e) {  
            //System.out.println(requestSyntax);
            //out.println(requestSyntax);
            System.out.println(e.toString());
            out.println(e.toString());
        }
    }
    
    //Will run forever, handling incoming requests to the server
    public void run() {  
        try
        {  
            try
            {
                InputStream inStream = incoming.getInputStream();
                OutputStream outStream = incoming.getOutputStream();
                handleRequest(inStream, outStream);
                
            }
            catch (IOException e)
            {  
                e.printStackTrace();
            }
            finally
            {
                incoming.close();
            }
        }
        catch (IOException e)
        {  
            e.printStackTrace();
        }
    }
}
