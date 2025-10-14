package be.pxl.services.services;

import be.pxl.services.domain.Organization;
import be.pxl.services.domain.dto.OrganizationResponse;
import be.pxl.services.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService implements IOrganizationService {
    private final OrganizationRepository organizationRepository;

    private Organization getOrganizationOrThrow(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

    private OrganizationResponse mapToOrganizationResponse(Organization org, boolean includeDepartments, boolean includeEmployees) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .address(org.getAddress())
                .departments(includeDepartments ? org.getDepartments() : null)
                .employees(includeEmployees ? org.getEmployees() : null)
                .build();
    }

    @Override
    public OrganizationResponse findById(Long id) {
        Organization org = getOrganizationOrThrow(id);
        return mapToOrganizationResponse(org, false, false);
    }

    @Override
    public OrganizationResponse findByIdWithDepartments(Long id) {
        Organization org = getOrganizationOrThrow(id);
        return mapToOrganizationResponse(org, true, false);
    }

    @Override
    public OrganizationResponse findByIdWithDepartmentsAndEmployees(Long id) {
        Organization org = getOrganizationOrThrow(id);
        return mapToOrganizationResponse(org, true, true);
    }

    @Override
    public OrganizationResponse findByIdWithEmployees(Long id) {
        Organization org = getOrganizationOrThrow(id);
        return mapToOrganizationResponse(org, false, true);
    }
}