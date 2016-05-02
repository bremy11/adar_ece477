
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import dijkstra.*;

//sql database setup commands 
//CREATE DATABASE adar_database;
//USE adar_database;
//CREATE TABLE waypoints (id INTEGER, longe VARCHAR(256), lat VARCHAR(256),adjID VARCHAR(256), enterTime TIMESTAMP);

//sample insert for testing

//INSERT INTO waypoints Values (1, '45', '44', '2,3', UTC_TIMESTAMP());
//INSERT INTO waypoints Values (1, '45', '45', '1,3', UTC_TIMESTAMP());
//INSERT INTO waypoints Values (1, '45', '46', '1,2', UTC_TIMESTAMP());


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
    
   // final static int port = 3112;
   
    final static int port = 3112;
   //arbitrarily selected port
    
    /**
     *Usage is useless now that we will be using JSON objects
     */

    /*static Semaphore availableRover = new Semaphore(0);
    static Semaphore lockGlobals = new Semaphore(1); //used to lock global variables 
    static float msgLat = 0;
    static float msgLonge = 0;
    static float roverLonge = 0;
    static float roverLat = 0;
    static int atLocation = 0;*/

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
    * This class handles the client input for one server socket connection. 
     */
class ThreadedHandler implements Runnable
{ 
    final static String ServerUser = "root";
    final static String ServerPassword = "1827";
    int runNum;
    private Socket incoming;

    static Semaphore availableRover = new Semaphore(0);
    static Semaphore lockGlobals = new Semaphore(1); //used to lock global variables
    static Semaphore availableRequest = new Semaphore(0); 
    static double msgLat;
    static double msgLonge;
    static double roverLonge;
    static double roverLat;
    static int atLocation;
    /*public ThreadedHandler(Socket newSoc, int iteration, Semaphore availableRover, Semaphore lockGlobals, 
        float msgLat, float msgLonge,float roverLonge,  float roverLat, int atLocation)*/
    public ThreadedHandler(Socket newSoc, int iteration){ 
        incoming = newSoc;
        runNum = iteration;

    }
   public static double gpsConvert(double deg){
	deg = Math.abs(deg);
	if(deg > 90){
		return Math.floor(deg/100.0)+(Math.floor(deg%100.0)/60.0)+( (deg%1)*100.0/360.0);
   	}
   return deg; 
   }
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        //http://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java
    	lat1 = gpsConvert(lat1);
	lat2 = gpsConvert(lat2);
	lng1 = gpsConvert(lng1);
	lng2 = gpsConvert(lng2);
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (float) (earthRadius * c);
        
        return dist;
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
            
            ResultSet r1 = q1.executeQuery("SELECT MAX(id) FROM waypoints");
            ResultSet r2 = q2.executeQuery( "SELECT id, longe, lat, adjID FROM waypoints");
            
            //Create a JSON Obj
            JSONObject obj = new JSONObject();

            while(r1.next()) {
                numPoints = r1.getString(1);
            }
            
           // int waypointSize = r2.getFetchSize();
            int waypointSize =Integer.parseInt(numPoints)+1; 
	    EdgeWeightedDigraph wayPoints = new EdgeWeightedDigraph(waypointSize+2);
            int sourceID = waypointSize;
            int destID = waypointSize+1;
            //id converted to index
            
            HashMap<Integer,Integer> idToIndex = new HashMap();
            int[] indexToId = new int[waypointSize];
            //int[] idList = new int[waypointSize];
            double[] longeList = new double[waypointSize+2];
            double[] latList = new double[waypointSize+2];
            String[] adjIdList = new String[waypointSize];
            
            //source and sink coordinates
            
            double longeSource= 45;
            double latSource = 43;
            double longeDest= 45;
            double latDest= 47;

	   longeSource = gpsConvert(roverLonge);
	   latSource = gpsConvert(roverLat);
	longeDest = gpsConvert(msgLonge);
	latDest = gpsConvert(msgLat);
	System.out.println("longeDest= "+longeDest+" latDest= "+latDest);
	
	latList[sourceID] = latSource;
	latList[destID] = latDest;
	longeList[sourceID] =longeSource;
	longeList[destID] = longeDest;
            int i = 0;
            
	    while(r2.next()) 
            {
                //two way, handles discrpancies/deleted waypoints
                indexToId[i] = r2.getInt(1);
                idToIndex.put(indexToId[i], i);
                
                longeList[i] = gpsConvert(Double.parseDouble(r2.getString(2)));
                latList[i] = gpsConvert(Double.parseDouble(r2.getString(3)));
                adjIdList[i] = r2.getString(4);
		i++;
            }
            int id;
            int adj;
            double dist;
            double distFromSource;
            double distToDest;
            
            double lat1 = 0;
            double lng1= 0;
            double lat2= 0;
            double lng2= 0;
            
            double shortDistDest = 999999999;
            int shortDistDestId = -1;
            double shortDistSource = 999999999;
            int shortDistSourceId = -1;

            for (i = 0; i < waypointSize; i++)
            {
                //id = indexToId.get(i);
		//
		List<String> items;
		if (adjIdList[i].contains(",")){
                 items = Arrays.asList(adjIdList[i].split("\\s*,\\s*"));
                }else {
		
		items = Arrays.asList(adjIdList[i]);	
		}
                for (String temp : items){ 
		 // System.out.println("adj: "+temp); 
		   int adjCur = idToIndex.get(Integer.parseInt(temp));
                    
                    lat1 = latList[i];
                    lng1 = longeList[i];
                    lat2 = latList[adjCur];
                    lng2 = longeList[adjCur];
                    
                    dist = distFrom(lat1, lng1, lat2, lng2);
                  // System.out.println(i+", "+adjCur+", "+dist); 
                    wayPoints.addEdge(new DirectedEdge(i, adjCur, dist));
                }
                //need to find points closest to dest and source for all points
                
                distFromSource = distFrom(latSource, longeSource, latList[i], longeList[i]);
                if (distFromSource <= shortDistSource)
                {
                    shortDistSource = distFromSource;
                    shortDistSourceId = i;
                }

                distToDest = distFrom(latDest, longeDest, latList[i], longeList[i]);
                if (distToDest <= shortDistDest)
                {
                    shortDistDest = distToDest;
                    shortDistDestId = i;
                }
		System.out.println("index: "+i+" distFromSource: "+distFromSource+" distToDest: "+ distToDest);
            }

            if (shortDistSourceId == -1 || shortDistDest == -1)
            {
                throw new Exception("in sendWaypointPath, source or dest id not set");
            }

            wayPoints.addEdge(new DirectedEdge(sourceID, shortDistSourceId, shortDistSource));
            wayPoints.addEdge(new DirectedEdge(shortDistSourceId, destID, shortDistDest));
	// wayPoints.addEdge(new DirectedEdge(sourceID, destID, .5));


            //close the sql quirres
            r1.close();
            r2.close();
          System.out.println(wayPoints);
            DijkstraSP sp = new DijkstraSP(wayPoints, sourceID);
            
            StringBuilder message = new StringBuilder();
            if (sp.hasPathTo(destID)) {
                message.append('$');
                int pointCount = 0;
		for (DirectedEdge x : sp.pathTo(destID))
		{
			pointCount++;
		}
		message.append(pointCount-1);
                int tempPoint=0;
		for (DirectedEdge x : sp.pathTo(destID))
                {
		    if (tempPoint < (pointCount-1)){
                    message.append('{');
                    //message.append(Double.toString(longeList[x.to()]));
                    message.append(Double.toString(longeDest));
		    message.append(',');
		    message.append(Double.toString(latDest));
                    //message.append(Double.toString(latList[x.to()]));
                    message.append('}');
		    }
		    tempPoint++;
                }
                message.append('}'); 
		System.out.println(message+"\n");
		out.println(message);
            }
            else {
                throw new Exception("path not connected");
            }
        }catch (Exception e) {
            System.out.println(e.toString());
	    e.printStackTrace();
            out.println(e.toString());
        }finally
        {
            try {
                if (conn!=null) conn.close();
            }
            catch (Exception e) {
            }
        }
    }

    //main rover request
    void roverOnline(Scanner in, PrintWriter out) {
        Connection conn=null;
        try
        { 
            //JSONObject roverCoordinates = new JSONObject();
            //long, lat\n   //found
            //1\n   //found
            String request;
            String[] splitInput;
            System.out.println("waiting for request");
            availableRover.release();
           
	    //		NOTE: uncomment the request aquire 
	    availableRequest.acquire();//wait for available request
            System.out.println("rover accepted request");

           // out.println("2,45.0,45.0,45.0,46.0");
            //send shrotest path
            while (true)
            {
                if(in.hasNextLine())
                {
                    request=in.nextLine();
		    
		    System.out.println(request);
                    splitInput = request.split(",");
		    while( splitInput.length !=2){
			request=in.nextLine();
		    	 splitInput = request.split(",");
		    	
		    }
                    if (splitInput.length ==1 && splitInput[0].equals("1"))
                    {
                        //rover at location
                        System.out.println("rover at location");
                        //lockGlobals.acquire();
                        atLocation = 1;
                        //lockGlobals.release();
                        availableRover.release();//increment semaphore, rover available
                        break;
                    }else if(splitInput.length ==2)
                    {
                        float latTemp = 0;
                        float longTemp= 0;
                        //catch here?
                        
                        //roverLonge = Float.parseFloat(splitInput[0]);
                        //roverLat = Float.parseFloat(splitInput[1]);
                       // lockGlobals.acquire();
                       // roverLonge =longTemp;
                       // roverLat = latTemp;
                       // lockGlobals.release();
                    	System.out.println("sending waypoints");
			sendWaypointPath(out);
			while(true){
				Thread.sleep(500);
				requestRoverLocation(out);
			}
		    }else 
                    {
                        //Error
                        throw new Exception("Error, split of rover input wasn't 1 or 2");
                    }
                }else {
                    throw new Exception("Input closed before rover reached destination");
                }
            }
            //check request criteria here and send to client
        }
        catch (Exception e) {
            e.printStackTrace();
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
    
    void requestRoverLocation( PrintWriter out) {
        Connection conn=null;
        try
        { 
            
            StringBuilder message = new StringBuilder();
          //  lockGlobals.acquire();
            
		message.append('@');
		message.append(1);
                    message.append('{');
                    message.append(Double.toString(roverLonge));
                    message.append(',');
     		message.append(Double.toString(roverLat));
                    message.append('}');
                message.append('}'); 
		out.println(message);
	//	System.out.println("roverLonge:" + roverLonge +" roverLat:"+roverLat);
            //lockGlobals.release();
            //check request criteria here and send to client
        	System.out.println(message);
	}
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
	    e.printStackTrace();
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
void updateRoverLocation( Scanner in) {
        Connection conn=null;
	JSONObject jsonObject = new JSONObject();
        String request;
	try
       {
       while (in.hasNextLine()){
                request=in.nextLine();
                request= request.replace('[', '{');
                request= request.replace(']', '}');
              //  System.out.println("request = "+request);
                Object obj = null;
                JSONParser parser = new JSONParser();
                try{
                    obj = parser.parse(request);
                }catch(Exception e)
                {
                }
                
                //get the command from the JSON object 
                jsonObject = (JSONObject) obj;
            

		if (jsonObject != null){
           	 
                //System.out.println(jsonObject.toJSONString()); 
	    
	    roverLat= Double.parseDouble((String) jsonObject.get("lat"));
            roverLonge = -Double.parseDouble((String) jsonObject.get("longe")); 
            //roverCoordinates.put("longe", roverLonge);
            //roverCoordinates.put("lat", roverLat);
            }
	}

            
            //out.println(roverCoordinates.toJSONString());

        }
        catch (Exception e) {
            System.out.println(e.toString());
       	    e.printStackTrace();
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
    void requestDelivery(JSONObject obj, PrintWriter out) {
        Connection conn=null;
        

        try
        {
            JSONObject roverCoordinates = new JSONObject();

            System.out.println("checking for available rover");
          /* if (availableRover.availablePermits() == 0)
            {
                System.out.println("no available rover");
                roverCoordinates.put("success", 0);
                out.println("Error, no available delivery rover");
                //return;
                throw new Exception("No available rover");
            }*/
	
            
	    
            System.out.println("rover designated for delivery");
            //lockGlobals.acquire();
            msgLat = Double.parseDouble((String) obj.get("lat"));
            msgLonge = -Double.parseDouble((String) obj.get("longe")); 
	    //lockGlobals.release();
            roverCoordinates.put("success", 1);
            //roverCoordinates.put("longe", roverLonge);
            //roverCoordinates.put("lat", roverLat);
            
            availableRequest.release(); //increment semaphore count, available request
	 //   availableRover.acquire();


            
            //out.println(roverCoordinates.toJSONString());

        }
        catch (Exception e) {
            System.out.println(e.toString());
            out.println(e.toString());
	    e.printStackTrace();
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
                obj.put("adjID",  r2.getString(4));
                obj.put("enterTime",  r2.getString(5));
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
            PreparedStatement pstmt = conn.prepareStatement("UPDATE waypoints SET adjID=? WHERE id LIKE ?");
            
            pstmt.setString(1, (String) obj.get("adjID"));
            pstmt.setString(2, (String) obj.get("id"));  
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
            ResultSet r1 = q1.executeQuery("select ifnull(max(id), -1)+1 from waypoints");
            while(r1.next()) {
                eventId = r1.getString(1);
                if (eventId==null){ eventId = "1";}
            }
            //System.out.println("numEvents = " + numEvents);
            r1.close();

            conn.setAutoCommit(true);
            String sql = "INSERT INTO waypoints VALUES(?,?,?,?, UTC_TIMESTAMP())";
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
            String sql = "DELETE FROM waypoints WHERE id = ?";
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
/*	while (true){
	out.println("HELLO");
	for (int i = 0; i < 1000000; i++);
	}*/
     /*  while(in.hasNext()){
       System.out.print(in.next());
       out.print('c');
       }*/
       
        if(in.hasNextLine()){
            request=in.nextLine();
            requestInt=Integer.parseInt(request);
        }else {
            return;
        }     
        
        JSONObject jsonObject = null;
        System.out.println("requestInt = "+requestInt);
        if (11<= requestInt && requestInt <=14){
            //if client side request requires json 
            
            if(in.hasNextLine()){
                request=in.nextLine();
                System.out.println("request = "+request);
                request= request.replace('[', '{');
                request= request.replace(']', '}');
                System.out.println("request = "+request);
                Object obj = null;
                JSONParser parser = new JSONParser();
                try{
                    obj = parser.parse(request);
                }catch(Exception e)
                {
                    out.println("ERROR: handleRequest couldn't parse JSON" + e.toString());
                }
                
                //get the command from the JSON object 
                jsonObject = (JSONObject) obj;
                System.out.println(jsonObject.toJSONString()); 
            }
        }

        try {
            if (requestInt == 1){
                //1, get path
                sendWaypointPath(out);
            }else if(requestInt == 2){
                //2, database requests current gps waypoint
                roverOnline(in,out);
            }else if(requestInt == 3){
                //3, rover comes on line
            	sendWaypointPath(out);
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
                requestDelivery(jsonObject, out);//json of request location
            }else if(requestInt == 14){
                addWaypoint(jsonObject,out);
            }else if(requestInt == 15){
                requestRoverLocation(out);
            }else if (requestInt == 20){
	    	updateRoverLocation(in);
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

