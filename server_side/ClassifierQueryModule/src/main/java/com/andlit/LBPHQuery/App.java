package com.andlit.LBPHQuery;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import com.google.gson.JsonObject;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;

public class App 
{
	private static final int SEARCH_RADIUS = 1;
    private static final int NEIGHBORS = 8;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 8;
    private static final double THRESHOLD = 120;
    
    public static void main( String[] args ) {
    	// sanity check
    	if( args.length < 3 )
    		printErrorAndDie(1,"Not enough arguments. (<classifier path> <database path> <face path>)");
    	
    	// connect to user's DB
        Connection conn = null;
		try {
			conn = connect(args[1]);
		} catch (ClassNotFoundException e) { printErrorAndDie(2,"JDBC driver class not found"); }
        if (conn == null) 
        	printErrorAndDie(3,"Couldn't connect to user's database");
        
        // load recognizer and face in grayscale
        LBPHFaceRecognizer l = createLBPHFaceRecognizer(SEARCH_RADIUS,NEIGHBORS,GRID_X,GRID_Y,THRESHOLD);
        l.load(args[0]);
        Mat img = imread(args[2],0);
        
        // run classifier on the face
        IntPointer f = new IntPointer(1);
        DoublePointer c = new DoublePointer(1);
        l.predict(img, f, c);
        int [] foundLabels = new int[1];
        double [] confidence = new double[1];
        
        // get results
        f.get(foundLabels);
        c.get(confidence);
        
        // Get name and lastname from database
        Statement s = null;
		try {
			s = conn.createStatement();
		} catch (SQLException e) { printErrorAndDie(4,e.getMessage()); }
        ResultSet res = null;
		try {
			res = s.executeQuery("SELECT * FROM known_ppl WHERE id = "+foundLabels[0]);
		} catch (SQLException e) { printErrorAndDie(5,e.getMessage()); }
        
		// Output the result
        String name = null, last = null;
		try {
			name = res.getString(3);
			last = res.getString(4);
		} catch (SQLException e) { printErrorAndDie(6,e.getMessage()); } 
        
		// successful response
		JsonObject resp = new JsonObject();
		resp.addProperty("error", 0);
        resp.addProperty("name",name);
        resp.addProperty("last",last);
        resp.addProperty("distance",confidence[0]);
        System.out.println(resp.toString());
    }
    
    public static Connection connect(String filename) throws ClassNotFoundException {
        Connection conn = null;
        try {
            // db parameters
            conn = DriverManager.getConnection("jdbc:sqlite:"+filename);
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public static void printErrorAndDie(int code,String msg) {
    	JsonObject resp = new JsonObject();
    	resp.addProperty("error", code);
    	resp.addProperty("message", msg);
    	System.out.println(resp.toString());
    	System.exit(code);
    }
}
