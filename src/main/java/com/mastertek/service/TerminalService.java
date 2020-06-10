package com.mastertek.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertek.web.rest.util.PersonUtil;
import com.mastertek.web.rest.vm.OpenDoorInfoVM;
import com.mastertek.web.rest.vm.OpenDoorVM;
import com.mastertek.web.rest.vm.PersonInsertVM;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator AuditEventRepository
 */
@Service
@Transactional
public class TerminalService {
	
	@Autowired
	ObjectMapper objectMapper;

	 public void verify(@RequestBody String pBody,HttpServletRequest request) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
	    JsonNode jsonNode = objectMapper.readValue(pBody, JsonNode.class);
	 
	    BufferedImage snap = getImage(jsonNode,"SanpPic");
	    save(snap);
	    
	    BufferedImage registered = getImage(jsonNode,"RegisteredPic");
	    save(registered);
	 }	  
	 
	public void snap(@RequestBody String pBody, HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readValue(pBody, JsonNode.class);

		BufferedImage snap = getImage(jsonNode, "SanpPic");
		save(snap);

		String deviceId = getDeviceId(jsonNode);
		String ip = getDeviceIp(request);
		openDoor(deviceId,ip);
	}  

	 private BufferedImage getImage(JsonNode jsonNode,String fieldName) throws IOException {
		String data = jsonNode.get("SanpPic").toString();
	    String base64Image = data.split(",")[1];
	    byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
	    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));	 
	    return img;
	 }
	 
	 private void save(BufferedImage img) throws IOException {
		ImageIO.write(img, "png", new File("c:\\temp\\"+UUID.randomUUID()+".png"));	
			
	 }
	 
	 private String getDeviceId(JsonNode jsonNode) {
		JsonNode info = jsonNode.get("info");
	    String deviceId = info.get("DeviceID").toString();
	    return deviceId;	
	 }
	 
	 private String getDeviceIp(HttpServletRequest request) {
		 String ip = request.getRemoteHost();	
		 return ip;
	 }
	 
	 
	public void openDoor(String deviceId, String ip) throws ClientProtocolException, IOException {

		OpenDoorVM openDoorVM = new OpenDoorVM();
		OpenDoorInfoVM openDoorInfoVM = new OpenDoorInfoVM(deviceId, "YOU CAN PASS...");
		openDoorVM.setInfo(openDoorInfoVM);

		ObjectMapper objectMapper = new ObjectMapper();
		HttpClient client = HttpClientBuilder.create().build();

		HttpPost httpPut = new HttpPost("http://" + ip + "/action/OpenDoor");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(openDoorVM), Charset.forName("UTF-8"));
		httpPut.setEntity(entity);
		String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
		httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

		HttpResponse response = client.execute(httpPut);

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200)
			throw new RuntimeException("error on communication. status code :" + statusCode);

		System.out.println("işlem tamamlandı");
	}	    
	 
	
	
	public void insertPerson(String deviceId,String name,Long isCheckSimilarity,String picinfo) throws ClientProtocolException, IOException {
		PersonInsertVM personInsertVM = PersonUtil.preparePersonImport(deviceId, name, isCheckSimilarity, picinfo);
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPut = new HttpPost("http://" + "192.168.2.10" + "/action/AddPerson");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(personInsertVM), Charset.forName("UTF-8"));
		httpPut.setEntity(entity);
		String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
		httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

		Long start  = System.currentTimeMillis();
		HttpResponse response = client.execute(httpPut);
		Long end  = System.currentTimeMillis();
		Long duration = end-start;
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200)
			throw new RuntimeException("error on communication. status code :" + statusCode);
		String result = EntityUtils.toString(response.getEntity());
		System.out.println(result);
		System.out.println("bitti");
	}
	
	public void editPerson(String deviceId,String personUUID,String newName,Long isCheckSimilarity,String picinfo) throws ClientProtocolException, IOException {
		PersonInsertVM personInsertVM = PersonUtil.preparePersonImport(deviceId, newName, isCheckSimilarity, picinfo);
		personInsertVM.getInfo().setPersonUUID(personUUID);
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPut = new HttpPost("http://" + "192.168.2.10" + "/action/EditPerson");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(personInsertVM), Charset.forName("UTF-8"));
		httpPut.setEntity(entity);
		String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
		httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

		Long start  = System.currentTimeMillis();
		HttpResponse response = client.execute(httpPut);
		Long end  = System.currentTimeMillis();
		Long duration = end-start;
		
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200)
			throw new RuntimeException("error on communication. status code :" + statusCode);
		String result = EntityUtils.toString(response.getEntity());
		System.out.println(result);
		System.out.println("bitti");
	}
	
	
	public void insertPersonTemporary(String deviceId,String name,Long isCheckSimilarity,String picinfo,String validBegin,String validEnd) {
		PersonUtil.preparePersonImportTemporary(deviceId, name, isCheckSimilarity, picinfo, validBegin, validEnd);
	}
	
	public void deletePerson() {
		
	}
	
	public void getPersonList() {
		
	}
   }

