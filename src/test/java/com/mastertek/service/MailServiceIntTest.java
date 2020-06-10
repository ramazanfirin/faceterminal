package com.mastertek.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mastertek.TerminalApp;
import com.mastertek.web.rest.util.PersonUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TerminalApp.class)
public class MailServiceIntTest {

	@Autowired
	TerminalService terminalService;
	
	@Before
    public void setup() {
       
    }

    @Test
    public void testSendEmail() throws Exception {
    	String path = "C:\\Users\\ramazan\\Desktop\\Face_744335_215_1576081266164.jpg";
    	String deviceId="1338170";
    	
    	byte[] image = PersonUtil.imageToByteArray(path);
    	String picInfo = PersonUtil.encodeFileToBase64Binary(image);
    	
        terminalService.insertPerson(deviceId, UUID.randomUUID().toString(), 1l, picInfo);
    }

    @Test
    public void editPerson() throws Exception {
    	String path = "C:\\Users\\ramazan\\Desktop\\Face_744335_215_1576081266164.jpg";
    	String deviceId="1338170";
    	
    	byte[] image = PersonUtil.imageToByteArray(path);
    	String picInfo = PersonUtil.encodeFileToBase64Binary(image);
    	
        terminalService.insertPerson(deviceId, UUID.randomUUID().toString(), 1l, picInfo);
    }
    
   
}
