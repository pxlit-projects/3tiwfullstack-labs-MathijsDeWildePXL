package be.pxl.services.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private Long id;
    private Long organizationId;
    private String name;
    private List<Employee> employees;
}
