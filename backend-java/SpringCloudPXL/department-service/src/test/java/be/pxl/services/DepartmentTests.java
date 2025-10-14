package be.pxl.services;

import be.pxl.services.domain.Department;
import be.pxl.services.domain.dto.DepartmentRequest;
import be.pxl.services.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class DepartmentTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DepartmentRepository departmentRepository;

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
        departmentRepository.deleteAll();
    }

    @Test
    public void testCreateDepartment() throws Exception {
        DepartmentRequest departmentRequest = DepartmentRequest.builder()
                .name("IT Department")
                .organizationId(1L)
                .build();

        String departmentString = objectMapper.writeValueAsString(departmentRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/department/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departmentString))
                .andExpect(status().isCreated());

        List<Department> departments = departmentRepository.findAll();
        assertEquals(1, departments.size());

        Department savedDepartment = departments.get(0);
        assertEquals("IT Department", savedDepartment.getName());
        assertEquals(1L, savedDepartment.getOrganizationId());
        assertNotNull(savedDepartment.getId());
    }

    @Test
    public void testGetAllDepartments() throws Exception {
        Department department1 = Department.builder()
                .name("HR Department")
                .organizationId(1L)
                .build();
        Department department2 = Department.builder()
                .name("Finance Department")
                .organizationId(2L)
                .build();

        departmentRepository.save(department1);
        departmentRepository.save(department2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/department/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Department> allDepartments = departmentRepository.findAll();
        assertEquals(2, allDepartments.size());
        assertTrue(allDepartments.stream().anyMatch(d -> d.getName().equals("HR Department")));
        assertTrue(allDepartments.stream().anyMatch(d -> d.getName().equals("Finance Department")));
    }

    @Test
    public void testGetDepartmentById() throws Exception {
        Department department = Department.builder()
                .name("Marketing Department")
                .organizationId(1L)
                .build();

        Department savedDepartment = departmentRepository.save(department);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/department/" + savedDepartment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marketing Department"));

        Optional<Department> foundDepartment = departmentRepository.findById(savedDepartment.getId());
        assertTrue(foundDepartment.isPresent());
        assertEquals("Marketing Department", foundDepartment.get().getName());
        assertEquals(1L, foundDepartment.get().getOrganizationId());
    }

    @Test
    public void testGetDepartmentsByOrganization() throws Exception {
        Department department1 = Department.builder()
                .name("IT Department")
                .organizationId(1L)
                .build();
        Department department2 = Department.builder()
                .name("HR Department")
                .organizationId(1L)
                .build();
        Department department3 = Department.builder()
                .name("Sales Department")
                .organizationId(2L)
                .build();

        departmentRepository.save(department1);
        departmentRepository.save(department2);
        departmentRepository.save(department3);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/department/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Department> organizationDepartments = departmentRepository.findByOrganizationId(1L);
        assertEquals(2, organizationDepartments.size());
        assertTrue(organizationDepartments.stream().allMatch(d -> d.getOrganizationId().equals(1L)));
        assertTrue(organizationDepartments.stream().anyMatch(d -> d.getName().equals("IT Department")));
        assertTrue(organizationDepartments.stream().anyMatch(d -> d.getName().equals("HR Department")));
    }

    @Test
    public void testGetDepartmentsByOrganizationWithEmployees() throws Exception {
        Department department1 = Department.builder()
                .name("Engineering Department")
                .organizationId(1L)
                .build();
        Department department2 = Department.builder()
                .name("Support Department")
                .organizationId(1L)
                .build();

        departmentRepository.save(department1);
        departmentRepository.save(department2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/department/organization/1/with-employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Department> organizationDepartments = departmentRepository.findByOrganizationId(1L);
        assertEquals(2, organizationDepartments.size());
        assertTrue(organizationDepartments.stream().allMatch(d -> d.getOrganizationId().equals(1L)));
    }
}