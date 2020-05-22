package com.mastertek.web.rest;

import com.mastertek.TerminalApp;

import com.mastertek.domain.Compare;
import com.mastertek.repository.CompareRepository;
import com.mastertek.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.mastertek.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CompareResource REST controller.
 *
 * @see CompareResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TerminalApp.class)
public class CompareResourceIntTest {

    private static final String DEFAULT_URL_1 = "AAAAAAAAAA";
    private static final String UPDATED_URL_1 = "BBBBBBBBBB";

    private static final String DEFAULT_URL_2 = "AAAAAAAAAA";
    private static final String UPDATED_URL_2 = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE_1 = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE_1 = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_IMAGE_1_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_1_CONTENT_TYPE = "image/png";

    private static final byte[] DEFAULT_IMAGE_2 = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE_2 = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_IMAGE_2_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_2_CONTENT_TYPE = "image/png";

    @Autowired
    private CompareRepository compareRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCompareMockMvc;

    private Compare compare;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CompareResource compareResource = new CompareResource(compareRepository);
        this.restCompareMockMvc = MockMvcBuilders.standaloneSetup(compareResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Compare createEntity(EntityManager em) {
        Compare compare = new Compare()
            .url1(DEFAULT_URL_1)
            .url2(DEFAULT_URL_2)
            .image1(DEFAULT_IMAGE_1)
            .image1ContentType(DEFAULT_IMAGE_1_CONTENT_TYPE)
            .image2(DEFAULT_IMAGE_2)
            .image2ContentType(DEFAULT_IMAGE_2_CONTENT_TYPE);
        return compare;
    }

    @Before
    public void initTest() {
        compare = createEntity(em);
    }

    @Test
    @Transactional
    public void createCompare() throws Exception {
        int databaseSizeBeforeCreate = compareRepository.findAll().size();
        
        

        // Create the Compare
        restCompareMockMvc.perform(post("/api/compares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(compare)))
            .andExpect(status().isCreated());

        // Validate the Compare in the database
        List<Compare> compareList = compareRepository.findAll();
        assertThat(compareList).hasSize(databaseSizeBeforeCreate + 1);
        Compare testCompare = compareList.get(compareList.size() - 1);
        assertThat(testCompare.getUrl1()).isEqualTo(DEFAULT_URL_1);
        assertThat(testCompare.getUrl2()).isEqualTo(DEFAULT_URL_2);
        assertThat(testCompare.getImage1()).isEqualTo(DEFAULT_IMAGE_1);
        assertThat(testCompare.getImage1ContentType()).isEqualTo(DEFAULT_IMAGE_1_CONTENT_TYPE);
        assertThat(testCompare.getImage2()).isEqualTo(DEFAULT_IMAGE_2);
        assertThat(testCompare.getImage2ContentType()).isEqualTo(DEFAULT_IMAGE_2_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void createCompareWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = compareRepository.findAll().size();

        // Create the Compare with an existing ID
        compare.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCompareMockMvc.perform(post("/api/compares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(compare)))
            .andExpect(status().isBadRequest());

        // Validate the Compare in the database
        List<Compare> compareList = compareRepository.findAll();
        assertThat(compareList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllCompares() throws Exception {
        // Initialize the database
        compareRepository.saveAndFlush(compare);

        // Get all the compareList
        restCompareMockMvc.perform(get("/api/compares?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(compare.getId().intValue())))
            .andExpect(jsonPath("$.[*].url1").value(hasItem(DEFAULT_URL_1.toString())))
            .andExpect(jsonPath("$.[*].url2").value(hasItem(DEFAULT_URL_2.toString())))
            .andExpect(jsonPath("$.[*].image1ContentType").value(hasItem(DEFAULT_IMAGE_1_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image1").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_1))))
            .andExpect(jsonPath("$.[*].image2ContentType").value(hasItem(DEFAULT_IMAGE_2_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image2").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE_2))));
    }

    @Test
    @Transactional
    public void getCompare() throws Exception {
        // Initialize the database
        compareRepository.saveAndFlush(compare);

        // Get the compare
        restCompareMockMvc.perform(get("/api/compares/{id}", compare.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(compare.getId().intValue()))
            .andExpect(jsonPath("$.url1").value(DEFAULT_URL_1.toString()))
            .andExpect(jsonPath("$.url2").value(DEFAULT_URL_2.toString()))
            .andExpect(jsonPath("$.image1ContentType").value(DEFAULT_IMAGE_1_CONTENT_TYPE))
            .andExpect(jsonPath("$.image1").value(Base64Utils.encodeToString(DEFAULT_IMAGE_1)))
            .andExpect(jsonPath("$.image2ContentType").value(DEFAULT_IMAGE_2_CONTENT_TYPE))
            .andExpect(jsonPath("$.image2").value(Base64Utils.encodeToString(DEFAULT_IMAGE_2)));
    }

    @Test
    @Transactional
    public void getNonExistingCompare() throws Exception {
        // Get the compare
        restCompareMockMvc.perform(get("/api/compares/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCompare() throws Exception {
        // Initialize the database
        compareRepository.saveAndFlush(compare);
        int databaseSizeBeforeUpdate = compareRepository.findAll().size();

        // Update the compare
        Compare updatedCompare = compareRepository.findOne(compare.getId());
        // Disconnect from session so that the updates on updatedCompare are not directly saved in db
        em.detach(updatedCompare);
        updatedCompare
            .url1(UPDATED_URL_1)
            .url2(UPDATED_URL_2)
            .image1(UPDATED_IMAGE_1)
            .image1ContentType(UPDATED_IMAGE_1_CONTENT_TYPE)
            .image2(UPDATED_IMAGE_2)
            .image2ContentType(UPDATED_IMAGE_2_CONTENT_TYPE);

        restCompareMockMvc.perform(put("/api/compares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCompare)))
            .andExpect(status().isOk());

        // Validate the Compare in the database
        List<Compare> compareList = compareRepository.findAll();
        assertThat(compareList).hasSize(databaseSizeBeforeUpdate);
        Compare testCompare = compareList.get(compareList.size() - 1);
        assertThat(testCompare.getUrl1()).isEqualTo(UPDATED_URL_1);
        assertThat(testCompare.getUrl2()).isEqualTo(UPDATED_URL_2);
        assertThat(testCompare.getImage1()).isEqualTo(UPDATED_IMAGE_1);
        assertThat(testCompare.getImage1ContentType()).isEqualTo(UPDATED_IMAGE_1_CONTENT_TYPE);
        assertThat(testCompare.getImage2()).isEqualTo(UPDATED_IMAGE_2);
        assertThat(testCompare.getImage2ContentType()).isEqualTo(UPDATED_IMAGE_2_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void updateNonExistingCompare() throws Exception {
        int databaseSizeBeforeUpdate = compareRepository.findAll().size();

        // Create the Compare

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCompareMockMvc.perform(put("/api/compares")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(compare)))
            .andExpect(status().isCreated());

        // Validate the Compare in the database
        List<Compare> compareList = compareRepository.findAll();
        assertThat(compareList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteCompare() throws Exception {
        // Initialize the database
        compareRepository.saveAndFlush(compare);
        int databaseSizeBeforeDelete = compareRepository.findAll().size();

        // Get the compare
        restCompareMockMvc.perform(delete("/api/compares/{id}", compare.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Compare> compareList = compareRepository.findAll();
        assertThat(compareList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Compare.class);
        Compare compare1 = new Compare();
        compare1.setId(1L);
        Compare compare2 = new Compare();
        compare2.setId(compare1.getId());
        assertThat(compare1).isEqualTo(compare2);
        compare2.setId(2L);
        assertThat(compare1).isNotEqualTo(compare2);
        compare1.setId(null);
        assertThat(compare1).isNotEqualTo(compare2);
    }
}
