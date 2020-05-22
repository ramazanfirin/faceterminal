package com.mastertek.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastertek.domain.Compare;
import com.mastertek.repository.CompareRepository;
import com.mastertek.web.rest.errors.BadRequestAlertException;
import com.mastertek.web.rest.util.HeaderUtil;
import com.mastertek.web.rest.vm.GetSimilarityVM;
import com.mastertek.web.rest.vm.SimilarityResult;

import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Compare.
 */
@RestController
@RequestMapping("/api")
public class CompareResource {

    private final Logger log = LoggerFactory.getLogger(CompareResource.class);

    private static final String ENTITY_NAME = "compare";

    private final CompareRepository compareRepository;

    public CompareResource(CompareRepository compareRepository) {
        this.compareRepository = compareRepository;
    }

    /**
     * POST  /compares : Create a new compare.
     *
     * @param compare the compare to create
     * @return the ResponseEntity with status 201 (Created) and with body the new compare, or with status 400 (Bad Request) if the compare has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @PostMapping("/compares")
    @Timed
    public ResponseEntity createCompare(@RequestBody Compare compare,HttpServletRequest request) throws URISyntaxException, ClientProtocolException, IOException {
        log.debug("REST request to save Compare : {}", compare);
        if (compare.getId() != null) {
            throw new BadRequestAlertException("A new compare cannot already have an ID", ENTITY_NAME, "idexists");
        }
        //Compare result = compareRepository.save(compare);
        
       
        
//        return ResponseEntity.created(new URI("/api/compares/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
//            .body(result);

        SimilarityResult similarityResult = compare(compare,request.getRemoteHost());
        return ResponseEntity.ok().body(similarityResult);
    }

    
    public SimilarityResult compare(Compare compare,String ip ) throws ClientProtocolException, IOException {
		byte[] image1 = compare.getImage1();
		byte[] image2 = compare.getImage2();

		ObjectMapper objectMapper = new ObjectMapper();
		HttpClient client = HttpClientBuilder.create().build();
		String base64_1 = encodeFileToBase64Binary(image1);
		String base64_2 = encodeFileToBase64Binary(image2);
		
		GetSimilarityVM getSimilarityVM = new GetSimilarityVM();
		getSimilarityVM.setPicinfo1(base64_1);
		getSimilarityVM.setPicinfo2(base64_2);
		
		HttpPost httpPut = new HttpPost("http://" + "192.168.2.10" + "/action/GetPictureSimilarity");
		StringEntity entity = new StringEntity(objectMapper.writeValueAsString(getSimilarityVM), Charset.forName("UTF-8"));
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
		
		JsonNode actualObj = objectMapper.readTree(result);
		JsonNode info = actualObj.get("info");
		String similar = info.get("Similarity").asText();
		System.out.println(result);
		
		SimilarityResult similarityResult = new SimilarityResult();;
		similarityResult.setDuration(duration);
		similarityResult.setSimilarity(similar);
		
		
		System.out.println(objectMapper.writeValueAsString(similarityResult));
		return similarityResult;
    }
    
    private String encodeFileToBase64Binary( byte[] bytes) {
    	return "data:image/jpeg;base64,"+Base64.getEncoder().encodeToString(bytes);
    }
    
    /**
     * PUT  /compares : Updates an existing compare.
     *
     * @param compare the compare to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated compare,
     * or with status 400 (Bad Request) if the compare is not valid,
     * or with status 500 (Internal Server Error) if the compare couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @PutMapping("/compares")
    @Timed
    public ResponseEntity<Compare> updateCompare(@RequestBody Compare compare,HttpServletRequest request) throws URISyntaxException, ClientProtocolException, IOException {
        log.debug("REST request to update Compare : {}", compare);
        if (compare.getId() == null) {
            return createCompare(compare, request);
        }
        Compare result = compareRepository.save(compare);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compare.getId().toString()))
            .body(result);
    }

    /**
     * GET  /compares : get all the compares.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of compares in body
     */
    @GetMapping("/compares")
    @Timed
    public List<Compare> getAllCompares() {
        log.debug("REST request to get all Compares");
        return compareRepository.findAll();
        }

    /**
     * GET  /compares/:id : get the "id" compare.
     *
     * @param id the id of the compare to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the compare, or with status 404 (Not Found)
     */
    @GetMapping("/compares/{id}")
    @Timed
    public ResponseEntity<Compare> getCompare(@PathVariable Long id) {
        log.debug("REST request to get Compare : {}", id);
        Compare compare = compareRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compare));
    }

    /**
     * DELETE  /compares/:id : delete the "id" compare.
     *
     * @param id the id of the compare to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/compares/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompare(@PathVariable Long id) {
        log.debug("REST request to delete Compare : {}", id);
        compareRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
