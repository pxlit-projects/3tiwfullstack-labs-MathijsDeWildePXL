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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        List<Employee> employees = employeeRepository.findAll();
        assertEquals(1, employees.size());

        Employee savedEmployee = employees.get(0);
        assertEquals("Jan", savedEmployee.getName());
        assertEquals(24, savedEmployee.getAge());
        assertEquals("student", savedEmployee.getPosition());
        assertEquals(1L, savedEmployee.getOrganizationId());
        assertEquals(1L, savedEmployee.getDepartmentId());
        assertNotNull(savedEmployee.getId());
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

        Employee saved1 = employeeRepository.save(employee1);
        Employee saved2 = employeeRepository.save(employee2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Employee> allEmployees = employeeRepository.findAll();
        assertEquals(2, allEmployees.size());
        assertTrue(allEmployees.stream().anyMatch(e -> e.getName().equals("John")));
        assertTrue(allEmployees.stream().anyMatch(e -> e.getName().equals("Jane")));
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
                .andExpect(jsonPath("$.name").value("Bob"));

        Optional<Employee> foundEmployee = employeeRepository.findById(savedEmployee.getId());
        assertTrue(foundEmployee.isPresent());
        assertEquals("Bob", foundEmployee.get().getName());
        assertEquals(28, foundEmployee.get().getAge());
        assertEquals("Analyst", foundEmployee.get().getPosition());
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
                .andExpect(jsonPath("$.length()").value(2));

        List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(1L);
        assertEquals(2, departmentEmployees.size());
        assertTrue(departmentEmployees.stream().allMatch(e -> e.getDepartmentId().equals(1L)));
        assertTrue(departmentEmployees.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(departmentEmployees.stream().anyMatch(e -> e.getName().equals("Charlie")));
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
                .andExpect(jsonPath("$.length()").value(2));

        List<Employee> organizationEmployees = employeeRepository.findByOrganizationId(1L);
        assertEquals(2, organizationEmployees.size());
        assertTrue(organizationEmployees.stream().allMatch(e -> e.getOrganizationId().equals(1L)));
        assertTrue(organizationEmployees.stream().anyMatch(e -> e.getName().equals("Eve")));
        assertTrue(organizationEmployees.stream().anyMatch(e -> e.getName().equals("Frank")));
    }
}