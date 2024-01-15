package com.cydeo.service;

import com.cydeo.dto.CompanyDTO;
import org.springframework.validation.BindingResult;

import java.util.List;

import java.util.List;

public interface CompanyService {
    List<CompanyDTO> getCompanyDtoByLoggedInUser();

    CompanyDTO findById(Long companyId);

    List<CompanyDTO> getCompanyList();

    CompanyDTO updateCompany(CompanyDTO company);

    CompanyDTO createCompany(CompanyDTO newCompany);

    void activateCompany(long companyId);

    void deactivateCompany(long companyId);
}