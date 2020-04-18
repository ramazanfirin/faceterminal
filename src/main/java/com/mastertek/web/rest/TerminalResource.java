package com.mastertek.web.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertek.domain.User;
import com.mastertek.repository.UserRepository;
import com.mastertek.security.SecurityUtils;
import com.mastertek.service.MailService;
import com.mastertek.service.UserService;
import com.mastertek.service.dto.UserDTO;
import com.mastertek.web.rest.errors.EmailAlreadyUsedException;
import com.mastertek.web.rest.errors.EmailNotFoundException;
import com.mastertek.web.rest.errors.InternalServerErrorException;
import com.mastertek.web.rest.errors.InvalidPasswordException;
import com.mastertek.web.rest.errors.LoginAlreadyUsedException;
import com.mastertek.web.rest.vm.KeyAndPasswordVM;
import com.mastertek.web.rest.vm.ManagedUserVM;
import com.mastertek.web.rest.vm.OpenDoorInfoVM;
import com.mastertek.web.rest.vm.OpenDoorVM;
import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/")
public class TerminalResource {

    private final Logger log = LoggerFactory.getLogger(TerminalResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    public TerminalResource(UserRepository userRepository, UserService userService, MailService mailService) {

        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (!checkPasswordLength(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {throw new LoginAlreadyUsedException();});
        userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw new EmailAlreadyUsedException();});
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public UserDTO getAccount() {
        return userService.getUserWithAuthorities()
            .map(UserDTO::new)
            .orElseThrow(() -> new InternalServerErrorException("User could not be found"));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }
        userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
            userDTO.getLangKey(), userDTO.getImageUrl());
   }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param password the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = "/account/change-password")
    @Timed
    public void changePassword(@RequestBody String password) {
        if (!checkPasswordLength(password)) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(password);
   }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset-password/init")
    @Timed
    public void requestPasswordReset(@RequestBody String mail) {
       mailService.sendPasswordResetMail(
           userService.requestPasswordReset(mail)
               .orElseThrow(EmailNotFoundException::new)
       );
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset-password/finish")
    @Timed
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
    
    @PostMapping(path = "/Subscribe/Snap")
    @Timed
    public void stranger(@RequestBody String pBody,HttpServletRequest request) throws JsonParseException, JsonMappingException, IOException {
    	request.getParameterMap();
    	System.out.println("bitti");
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	JsonNode jsonNode = objectMapper.readValue(pBody, JsonNode.class);
    	
    	String data = jsonNode.get("SanpPic").toString();
    	String base64Image = data.split(",")[1];
    	byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
    	BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
    	ImageIO.write(img, "png", new File("c:\\temp\\"+UUID.randomUUID()+".png"));
    	System.out.println("bitti");
    	
    	JsonNode info = jsonNode.get("info");
    	String deviceId = info.get("DeviceID").toString();
    	String ip = request.getRemoteHost();
    	openDoor(deviceId,ip);
    	
    }
    
    
    public static void openDoor(String deviceId,String ip) throws ClientProtocolException, IOException {
    	

    	OpenDoorVM openDoorVM = new OpenDoorVM();
    	OpenDoorInfoVM openDoorInfoVM = new OpenDoorInfoVM(deviceId, "YOU CAN PASS...");
    	openDoorVM.setInfo(openDoorInfoVM);
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	HttpClient client = HttpClientBuilder.create().build();
    	
    	HttpPost httpPut = new HttpPost("http://"+ip+"/action/OpenDoor");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(openDoorVM),Charset.forName("UTF-8"));
		httpPut.setEntity(entity);
		String encoding = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
		httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
		
		HttpResponse response = client.execute(httpPut);
		
		int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode!=200)
			throw new RuntimeException("error on communication. status code :" +statusCode);
		
		System.out.println("işlem tamamlandı");
    }
    
    public static void main(String[] args) throws ClientProtocolException, IOException {
		//openDoor();
	}
    
}
