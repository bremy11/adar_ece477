
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
//CREATE DATABASE boilerMeetup;
//USE boilerMeetup;
//CREATE TABLE events (id INTEGER, name VARCHAR(60), longe VARCHAR(256), location VARCHAR(256), description VARCHAR(256), startTime TIMESTAMP, endTime TIMESTAMP, numAttendees INTEGER, lat VARCHAR(256));

///////////////////////////// Mutlithreaded Server /////////////////////////////

public class boilerServer 
{
	final static int port = 3112;
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
	void getAllEvents(PrintWriter out) {

		Connection conn=null;
		try
		{	
			String numEvents = null;
			conn = getConnection();
			Statement q1 = conn.createStatement();
			Statement q2 = conn.createStatement();

			ResultSet r1 = q1.executeQuery("SELECT COUNT(id) FROM events");
			ResultSet r2 = q2.executeQuery( "SELECT * FROM events");

			//Create a JSON Obj
			JSONObject obj = new JSONObject();

			//Get the current number of events
			while(r1.next()) {
				numEvents = r1.getString(1);
			}

			//send the events to the app
			while(r2.next()) {
				obj.put("eventCount", 	numEvents);
				obj.put("id",  		r2.getString(1));
				obj.put("name", 	r2.getString(2));
				obj.put("longe",	r2.getString(3));
				obj.put("location", 	r2.getString(4));
				obj.put("description", 	r2.getString(5));
				//might have to handle these differently since these are timestamps
				obj.put("startTime", 	r2.getString(6));
				obj.put("endTime", 	r2.getString(7));
				////
				obj.put("numAttendees", r2.getString(8));
				obj.put("lat",	r2.getString(9));
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

	/**
	 *This will return the information for a specific event
	 */
	void getEventInfo( JSONObject obj, PrintWriter out) {

		System.out.println();
		System.out.println(obj.toJSONString());
		System.out.println();		
		
		Connection conn=null;
		try
		{	
			String numEvents = null;
			conn = getConnection();
			
			Statement q1 = conn.createStatement();
			ResultSet r1 = q1.executeQuery("SELECT COUNT(id) FROM events");
			while(r1.next()) {
				numEvents = r1.getString(1);
			}
			//System.out.println("numEvents = " + numEvents);
			r1.close();
			
			//get a conncetion
			
			//set the prepared statement
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM events WHERE id LIKE ?");
			//pstmt.setString(1, (Integer) obj.get("id"));
			
			//System.out.println("id = " + String.format("%d",obj.get("id")));
			pstmt.setString(1, String.format("%d",obj.get("id")));
			//execute the query
			//System.out.println(pstmt);
			
			ResultSet result = pstmt.executeQuery();
			
			//create a new JSON object
			JSONObject newEvent = new JSONObject();

			//populate & send the JSON object
			while(result.next()) {
				newEvent.put("eventCount", numEvents);
				newEvent.put("id",  result.getString(1) );
				newEvent.put("name", result.getString(2));
				newEvent.put("longe", result.getString(3));
				newEvent.put("location", result.getString(4));
				newEvent.put("description", result.getString(5));
				newEvent.put("startTime", result.getString(6));
				newEvent.put("endTime", result.getString(7));
				newEvent.put("numAttendees", result.getString(8));
				newEvent.put("lat",	result.getString(9));
				//send info
				out.println(newEvent.toJSONString());
			}
			//close the result
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
	
	
	void attendEvent( JSONObject obj, PrintWriter out) {

		/*System.out.println();
		System.out.println(obj.toJSONString());
		System.out.println();	*/
		
		Connection conn=null;
		try
		{	
			String numEvents = null;
			conn = getConnection();
			
			
			//get a conncetion
			
			//set the prepared statement
			PreparedStatement pstmt = conn.prepareStatement("UPDATE events SET numAttendees=numAttendees+1 WHERE id LIKE ?");
			
			//pstmt.setString(1, (Integer) obj.get("id"));
			
			//System.out.println("id = " + String.format("%d",obj.get("id")));
			pstmt.setString(1, String.format("%d",obj.get("id")));
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
			try {
				if (conn!=null) conn.close();
			}
			catch (Exception e) {
			}
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
	void addEvent(JSONObject obj, PrintWriter out) {
		Connection conn = null;
		
		try{
			//get a connection & set it to change the db automatically
			conn = getConnection();
			
			String eventId = null;
			conn = getConnection();
			
			
			String numEvents = null;
			conn = getConnection();
			
			Statement q1 = conn.createStatement();
			ResultSet r1 = q1.executeQuery("select max(id)+1 from events");
			while(r1.next()) {
				eventId = r1.getString(1);
				if (eventId==null){ eventId = "1";}
			}
			//System.out.println("numEvents = " + numEvents);
			r1.close();
			
			
			
			
			conn.setAutoCommit(true);
			String sql = "INSERT INTO events VALUES(?,?,?,?,?,?,?,?,?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);

			//get all info from the JSON object	
			pstmt.setString(1,eventId);
			pstmt.setString(2,(String) obj.get("name"));
			pstmt.setString(3,(String) obj.get("longe"));
			pstmt.setString(4,(String) obj.get("location"));
			pstmt.setString(5,(String) obj.get("description"));
			pstmt.setString(6,(String) obj.get("startTime"));
			pstmt.setString(7,(String) obj.get("startTime"));
			pstmt.setString(8, Integer.toString(1));
			pstmt.setString(9,(String) obj.get("lat"));
			
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

	void deleteEvent(JSONObject obj, PrintWriter out) {
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

	void rmOldEvents() {
		Connection conn = null;

		try{
			//get the connection
			conn = getConnection();
			//set the connection to autocommit changes to the db
			conn.setAutoCommit(true);
			//set the prepared statement
			//String sql = "DELETE FROM events WHERE endTime < UTC_TIMESTAMP();";
			String sql = "DELETE FROM events WHERE endTime < DATE_ADD(UTC_TIMESTAMP(), INTERVAL 5 HOUR);";
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			//update the db
			pstmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println(e.toString());
			//out.println(e.toString());
		}
		finally {
			try {
				if (conn!=null) conn.close();
			}
			catch (Exception e) {}
		}
	}
	
	//easiest solution, run this command on every request:
	//DELETE FROM events WHERE endTime < GETDATE();
	void handleRequest( InputStream inStream, OutputStream outStream) 
	{
	
		//rmOldEvents();
	
	
		Scanner in = new Scanner(inStream);         
		PrintWriter out = new PrintWriter(outStream, true /* autoFlush */);
		
		// Get parameters of the call
		String request = "fail";
		if(in.hasNextLine()){
			request=in.nextLine();
			//...
		}else {
		return;
		}
		
		Object obj = null;

		JSONParser parser = new JSONParser();
		
		try{
			obj = parser.parse(request);
		}catch(Exception e)
		{
			System.out.println("HERE" + e.toString());
			out.println(e.toString());
		}

		//get the command from the JSON object	
		JSONObject jsonObject = (JSONObject) obj;
		System.out.println(jsonObject.toJSONString());
		String req = (String) jsonObject.get("command");

		/**
		 *The incoming JSON Object has the following fields:
		 *	command
		 * 	id
		 *	name
		 *	position
		 *	location
		 *	description
		 *	startTime
		 *	endTime
		 *	numAttendees
		 */

		System.out.println("req = " +req);
		try {
			//perform the requested operation
			if (req.equals("GET-ALL-EVENTS")) {
				//System.out.println("line = 0");
				getAllEvents(out);
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

	private Socket incoming;
}
