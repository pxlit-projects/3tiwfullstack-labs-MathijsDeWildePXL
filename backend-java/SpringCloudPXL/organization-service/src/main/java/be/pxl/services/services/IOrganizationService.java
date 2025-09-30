package be.pxl.services.services;

import be.pxl.services.domain.dto.OrganizationResponse;

public interface IOrganizationService {
    OrganizationResponse findById(Long id);

    OrganizationResponse findByIdWithDepartments(Long id);

    OrganizationResponse findByIdWithDepartmentsAndEmployees(Long id);

    OrganizationResponse findByIdWithEmployees(Long id);
}
