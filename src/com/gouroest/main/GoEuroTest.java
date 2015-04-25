package com.gouroest.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.ws.rs.core.MediaType;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
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

	public City city;
	public ArrayList<City> cities = new ArrayList<City>();

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
	 * This method creates and fills an excel sheet.
	 * @param cities arraylist of City
	 */
	private void fillExcelSheet(ArrayList<City> cities){
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		
		HSSFFont boldFont = workbook.createFont();
		boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		HSSFCellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont); 

		HSSFSheet sheet = workbook.createSheet("Cities");
        int rowIndex = 0;
        int cellIndex = 0;
        
        Row row = sheet.createRow(rowIndex++);
        HSSFCell idCell = (HSSFCell) row.createCell(cellIndex++);
        idCell.setCellValue("_id");
        idCell.setCellStyle(boldStyle);
        
        HSSFCell nameCell = (HSSFCell) row.createCell(cellIndex++);
        nameCell.setCellValue("name");
        nameCell.setCellStyle(boldStyle);
        
        HSSFCell typeCell = (HSSFCell) row.createCell(cellIndex++);
        typeCell.setCellValue("name");
        typeCell.setCellStyle(boldStyle);

        HSSFCell latitudeCell = (HSSFCell) row.createCell(cellIndex++);
        latitudeCell.setCellValue("latitude");
        latitudeCell.setCellStyle(boldStyle);
        
        HSSFCell longitudeCell = (HSSFCell) row.createCell(cellIndex++);
        longitudeCell.setCellValue("longitude");
        longitudeCell.setCellStyle(boldStyle);

        for(City city : cities){
        	cellIndex = 0;
            row = sheet.createRow(rowIndex++);
            row.createCell(cellIndex++).setCellValue(city.getId());
            row.createCell(cellIndex++).setCellValue(city.getName());
            row.createCell(cellIndex++).setCellValue(city.getType());
            row.createCell(cellIndex++).setCellValue(city.getLatitude());
            row.createCell(cellIndex++).setCellValue(city.getLongitude());
        }
         
        try {
            FileOutputStream out = 
                    new FileOutputStream(new File("Cities Sheet.xls"));
            workbook.write(out);
            out.close();
            System.out.println("Done!!!");
            System.out.println("Check created excel sheet named \"Cities Sheet.xls\" in the same folder where \"GoEuroTest.jar\" exists.");
        } catch (IOException e) {
            e.printStackTrace();
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
		goEuroTest.process(args[0]);
	}
}
