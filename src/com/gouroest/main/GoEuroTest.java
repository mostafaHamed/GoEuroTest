package com.gouroest.main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.gouroest.model.City;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * @author mhamed
 *
 */
public class GoEuroTest {

	String cityName;
	public City city;
	public ArrayList<City> cities = new ArrayList<City>();
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "_id,name,type,latitude,longitude";

	/**
	 * This method calls a web-service with a specific input and return with a JsonArray as a reply
	 * @param cityName
	 * @return JsonArray 
	 */
	private JSONArray callWebserviceAndGetResponse(String cityName){
		ClientConfig clientConfig = new DefaultClientConfig();
		Client client = Client.create(clientConfig);
		WebResource webResource = client.resource("http://api.goeuro.com/api/v2/position/suggest/en/" + cityName);
		String response = (String) webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
		JSONArray jsonResponse = null;
		try {
			jsonResponse = new JSONArray(response);
		} catch (JSONException e) {
			System.out.println("Error in retrieving response from web-service");
			e.printStackTrace();
		}
		return jsonResponse;
	} 
	
	
	/**
	 * This method construct an object from City from json object reply and fill cities ArrayList
	 * @param obj Json response
	 */
	private void constructCity(JSONArray obj) {
		long id;
		String name;
		String type;
		double latitude;
		double longitude;
		
		for(int i = 0; i < obj.length(); i++){
			JSONObject rec;
			JSONObject geo_position;
			try {
				rec = obj.getJSONObject(i);
				id = rec.getLong("_id");
				name = rec.getString("name");
				type = rec.getString("type");
				geo_position = rec.getJSONObject("geo_position");
				latitude = geo_position.getDouble("latitude");
				longitude = geo_position.getDouble("longitude");
				city = new City(id, name, type, latitude, longitude);
				cities.add(city);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * This method creates and fills csv file.
	 * @param cities arraylist of City
	 */
	private void fillExcelSheet(ArrayList<City> cities){
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(cityName + ".csv");
			fileWriter.append(FILE_HEADER.toString());
			fileWriter.append(NEW_LINE_SEPARATOR);
			for (City city : cities) {
				fileWriter.append(String.valueOf(city.getId()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(city.getName());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(city.getType());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(city.getLatitude()));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(city.getLongitude()));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			System.out.println("Done!!!");
			System.out.println("Check created excel sheet named " + cityName + ".csv in the same folder where \"GoEuroTest.jar\" exists.");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * This is an entry method for the application
	 * @param arg
	 */
	private void process(String arg){
		JSONArray response = callWebserviceAndGetResponse(arg);
		constructCity(response);
		fillExcelSheet(cities);
	}
	
	public static void main(String[] args) {
		GoEuroTest goEuroTest = new GoEuroTest();
		goEuroTest.cityName = args[0];
		goEuroTest.process(goEuroTest.cityName);
	}
}
