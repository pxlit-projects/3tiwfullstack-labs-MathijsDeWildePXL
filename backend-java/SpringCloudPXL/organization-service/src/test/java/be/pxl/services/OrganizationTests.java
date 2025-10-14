package be.pxl.services;

import be.pxl.services.domain.Organization;
import be.pxl.services.repository.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class OrganizationTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Container
    private static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void registerMyPostgresPoperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();
    }

    @Test
    public void testGetOrganizationById() throws Exception {
        Organization organization = Organization.builder()
                .name("Tech Corp")
                .address("123 Tech Street")
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/organization/" + savedOrganization.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech Corp"))
                .andExpect(jsonPath("$.address").value("123 Tech Street"));

        Optional<Organization> foundOrganization = organizationRepository.findById(savedOrganization.getId());
        assertTrue(foundOrganization.isPresent());
        assertEquals("Tech Corp", foundOrganization.get().getName());
        assertEquals("123 Tech Street", foundOrganization.get().getAddress());
    }

    @Test
    public void testGetOrganizationByIdWithDepartments() throws Exception {
        Organization organization = Organization.builder()
                .name("Business Solutions Inc")
                .address("456 Business Ave")
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/organization/" + savedOrganization.getId() + "/with-departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Business Solutions Inc"))
                .andExpect(jsonPath("$.address").value("456 Business Ave"));

        Optional<Organization> foundOrganization = organizationRepository.findById(savedOrganization.getId());
        assertTrue(foundOrganization.isPresent());
        assertEquals("Business Solutions Inc", foundOrganization.get().getName());
    }

    @Test
    public void testGetOrganizationByIdWithEmployees() throws Exception {
        Organization organization = Organization.builder()
                .name("Global Services Ltd")
                .address("789 Global Plaza")
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/organization/" + savedOrganization.getId() + "/with-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Global Services Ltd"))
                .andExpect(jsonPath("$.address").value("789 Global Plaza"));

        Optional<Organization> foundOrganization = organizationRepository.findById(savedOrganization.getId());
        assertTrue(foundOrganization.isPresent());
        assertEquals("Global Services Ltd", foundOrganization.get().getName());
    }

    @Test
    public void testGetOrganizationByIdWithDepartmentsAndEmployees() throws Exception {
        Organization organization = Organization.builder()
                .name("Enterprise Corp")
                .address("101 Enterprise Blvd")
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/organization/" + savedOrganization.getId() + "/with-departments-and-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Enterprise Corp"))
                .andExpect(jsonPath("$.address").value("101 Enterprise Blvd"));

        Optional<Organization> foundOrganization = organizationRepository.findById(savedOrganization.getId());
        assertTrue(foundOrganization.isPresent());
        assertEquals("Enterprise Corp", foundOrganization.get().getName());
    }
}