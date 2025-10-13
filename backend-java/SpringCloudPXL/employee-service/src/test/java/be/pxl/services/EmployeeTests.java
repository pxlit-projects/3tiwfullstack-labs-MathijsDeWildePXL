package be.pxl.services;

import be.pxl.services.domain.Employee;
import be.pxl.services.domain.dto.EmployeeRequest;
import be.pxl.services.repository.EmployeeRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class EmployeeTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Container
    private static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void registerMyPostgresPoperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    public void testCreateEmployee() throws Exception {
        EmployeeRequest employeeRequest = EmployeeRequest.builder()
                .age(24)
                .name("Jan")
                .position("student")
                .organizationId(1L)
                .departmentId(1L)
                .build();

        String employeeString = objectMapper.writeValueAsString(employeeRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeString))
                .andExpect(status().isCreated());

        assertEquals(1, employeeRepository.findAll().size());
    }

    @Test
    public void testGetAllEmployees() throws Exception {
        Employee employee1 = Employee.builder()
                .age(25)
                .name("John")
                .position("Developer")
                .organizationId(1L)
                .departmentId(1L)
                .build();
        Employee employee2 = Employee.builder()
                .age(30)
                .name("Jane")
                .position("Manager")
                .organizationId(2L)
                .departmentId(2L)
                .build();

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        Employee employee = Employee.builder()
                .age(28)
                .name("Bob")
                .position("Analyst")
                .organizationId(1L)
                .departmentId(1L)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee/" + savedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.position").value("Analyst"))
                .andExpect(jsonPath("$.organizationId").value(1))
                .andExpect(jsonPath("$.departmentId").value(1));
    }

    @Test
    public void testGetEmployeesByDepartment() throws Exception {
        Employee employee1 = Employee.builder()
                .age(25)
                .name("Alice")
                .position("Developer")
                .organizationId(1L)
                .departmentId(1L)
                .build();
        Employee employee2 = Employee.builder()
                .age(30)
                .name("Charlie")
                .position("Tester")
                .organizationId(1L)
                .departmentId(1L)
                .build();
        Employee employee3 = Employee.builder()
                .age(35)
                .name("Diana")
                .position("Manager")
                .organizationId(1L)
                .departmentId(2L)
                .build();

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].departmentId").value(1))
                .andExpect(jsonPath("$[1].departmentId").value(1));
    }

    @Test
    public void testGetEmployeesByOrganization() throws Exception {
        Employee employee1 = Employee.builder()
                .age(25)
                .name("Eve")
                .position("Developer")
                .organizationId(1L)
                .departmentId(1L)
                .build();
        Employee employee2 = Employee.builder()
                .age(30)
                .name("Frank")
                .position("Analyst")
                .organizationId(1L)
                .departmentId(2L)
                .build();
        Employee employee3 = Employee.builder()
                .age(35)
                .name("Grace")
                .position("Manager")
                .organizationId(2L)
                .departmentId(3L)
                .build();
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee/organization/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].organizationId").value(1))
                .andExpect(jsonPath("$[1].organizationId").value(1));
    }
}
