package be.pxl.services.services;

import be.pxl.services.domain.Department;
import be.pxl.services.domain.dto.DepartmentRequest;
import be.pxl.services.domain.dto.DepartmentResponse;
import be.pxl.services.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService implements IDepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public void add(DepartmentRequest departmentRequest) {
        Department department = Department.builder()
                .organizationId(departmentRequest.getOrganizationId())
                .name(departmentRequest.getName())
                .build();
        departmentRepository.save(department);
    }

    @Override
    public DepartmentResponse findById(Long id) {
        return departmentRepository.findById(id)
                .map(d -> mapToDepartmentResponse(d, false))
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @Override
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(d -> mapToDepartmentResponse(d, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> findByOrganization(Long organizationId) {
        return departmentRepository.findByOrganizationId(organizationId)
                .stream()
                .map(d -> mapToDepartmentResponse(d, false))
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> findByOrganizationWithEmployees(Long organizationId) {
        return departmentRepository.findByOrganizationId(organizationId)
                .stream()
                .map(d -> mapToDepartmentResponse(d, true))
                .collect(Collectors.toList());
    }

    private DepartmentResponse mapToDepartmentResponse(Department department, boolean includeEmployees) {
        DepartmentResponse.DepartmentResponseBuilder builder = DepartmentResponse.builder()
                .id(department.getId())
                .organizationId(department.getOrganizationId())
                .name(department.getName());
        if (includeEmployees) {
            builder.employees(department.getEmployees());
        }
        return builder.build();
    }
}
