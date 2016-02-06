
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
 * ///1, get path
 * 2, update of current gps waypoint location
 * 3, rover comes on line
 * ///4, database requests delivery
 * 5, rover at destination
 * 
 * Client side protocols
 * 10, get all waypoints
 * 11, update waypoint connection, connect
 * 12, delete waypoint
 * 13, request delivery
 * 14, add waypoint
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
    public static void main(String[] args )
    {  
        try{  
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
           
            while(r2.next()) {
                obj.put("numPoints",  numPoints);
                obj.put("id",    r2.getString(1));
                obj.put("longe", r2.getString(2));
                obj.put("lat", r2.getString(3));
                obj.put("enterTime",  r2.getString(4));
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
        Connection conn=null;
        try
        { 
            String numEvents = null;
            conn = getConnection();
            
            //set the prepared statement
            PreparedStatement pstmt = conn.prepareStatement("UPDATE waypoints SET numAttendees=? WHERE id LIKE ?");
            
            pstmt.setString(1, (String) obj.get("adjID")));
            pstmt.setString(2, String.format("%d",obj.get("id")));  
            pstmt.executeUpdate();
            
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
     *This will add a new event to the database
     */
    void addWaypoint(JSONObject obj, PrintWriter out) {
        Connection conn = null;
        
        try{
            //get a connection & set it to change the db automatically
            
            String eventId = null;
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
            pstmt.setString(4,"");
            
            System.out.println(pstmt);
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
        
        JSONObject jsonObject = null;
        
        if (11<= requestInt && requestInt <=14){
            //if client side request requires json 
            
            if(in.hasNextLine()){
                request=in.nextLine();
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
                
                System.out.println("req = " +req);
            }
        }

        try {
            if (requestInt == 1){
                //1, get path
                sendWaypointPath(out);
            }else if(requestInt == 2){
                //2, database requests current gps waypoint
            }else if(requestInt == 3){
                //3, rover comes on line
            }else if(requestInt == 4){
                //4, database requests delivery
            }else if(requestInt == 5){
                //5, rover at destination
            }else if(requestInt == 10){
                //6, get all waypoints
                getAllWaypoints(out);
            }else if(requestInt == 11){
                //7, update waypoint connection, connect
                updateAdj(jsonObject,out);
            }else if(requestInt == 12){
                //8, delete waypoint
                deleteWaypoint(jsonObject, out);
            }else if(requestInt == 13){
                //9, request delivery
            }else if(requestInt == 14){
                addWaypoint(jsonObject,out);
            }else if(requestInt == 15){
                getRoverLocation(out);
            }else {
                //invalid communication
                System.out.println("ERROR, INVALID REQUEST");
            }
        }catch (Exception e) {  
            //System.out.println(requestSyntax);
            //out.println(requestSyntax);
            System.out.println("ERROR invoking call in handleRequest"+ e.toString());
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
