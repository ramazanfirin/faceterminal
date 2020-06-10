package com.mastertek.web.rest.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.mastertek.web.rest.vm.PersonInsertVM;

public class PersonUtil {

	public static PersonInsertVM preparePersonImport(String deviceId,String name,Long isCheckSimilarity,String picinfo) {
		PersonInsertVM result = new PersonInsertVM(deviceId);
		result.getInfo().setPersonUUID(UUID.randomUUID().toString());
		result.getInfo().setName(name);
		
		result.getInfo().setTempvalid(0l);
		result.getInfo().setIsCheckSimilarity(isCheckSimilarity);
		result.setPicinfo(picinfo);
		
		return result;
	}
	
	public static PersonInsertVM preparePersonImportTemporary(String deviceId,String name,Long isCheckSimilarity,String picinfo,String validBegin,String validEnd) {
		PersonInsertVM result = new PersonInsertVM(deviceId);
		result.getInfo().setPersonUUID(UUID.randomUUID().toString());
		result.getInfo().setName(name);
		
		result.getInfo().setTempvalid(1l);
		result.getInfo().setIsCheckSimilarity(isCheckSimilarity);
		result.setPicinfo(picinfo);
		result.getInfo().setValidBegin(validBegin);
		result.getInfo().setValidEnd(validEnd);
		
		return result;
	}
	
	public static String encodeFileToBase64Binary( byte[] bytes) {
    	return "data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(bytes);
    }
	
	public static byte[]  imageToByteArray(String path) throws Exception{
	      BufferedImage bImage = ImageIO.read(new File(path));
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ImageIO.write(bImage, "jpg", bos );
	      byte [] data = bos.toByteArray();
	      return data;
	   }
}
