package com.cydeo.service.impl;

import com.cydeo.client.CountryClient;
import com.cydeo.dto.ClientVendorDTO;
import com.cydeo.dto.CompanyDTO;
import com.cydeo.dto.CountryInfoDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.Company;
import com.cydeo.enums.ClientVendorType;
import com.cydeo.exception.ClientVendorNotFoundException;
import com.cydeo.mapper.MapperUtil;
import com.cydeo.entity.ClientVendor;
import com.cydeo.service.ClientVendorService;
import com.cydeo.service.InvoiceService;
import com.cydeo.service.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.cydeo.repository.ClientVendorRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
public class ClientVendorServiceImpl implements ClientVendorService {


    private final ClientVendorRepository clientVendorRepository;
    private final MapperUtil mapperUtil;
    private final SecurityService securityService;
    private final InvoiceService invoiceService;

    private final CountryClient countryClient;
    @Value("${COUNTRIES_API_KEY}")
    private String countriesApiKey;

    public ClientVendorServiceImpl(ClientVendorRepository clientVendorRepository, MapperUtil mapperUtil, SecurityService securityService, InvoiceService invoiceService, CountryClient countryClient) {
        this.clientVendorRepository = clientVendorRepository;
        this.mapperUtil = mapperUtil;
        this.securityService = securityService;
        this.invoiceService = invoiceService;
        this.countryClient = countryClient;
    }


    @Override
    public ClientVendorDTO findById(Long id) {
        ClientVendor clientVendor = clientVendorRepository.findById(id).orElseThrow(()->
                new ClientVendorNotFoundException("Client vendor cannot be found with id: "+id));

        return mapperUtil.convert(clientVendor,new ClientVendorDTO());
    }


    @Override
    public List<ClientVendorDTO> getAllClientVendors() {
        UserDTO loggedInUser = securityService.getLoggedInUser();
        List<ClientVendor> clientVendors = clientVendorRepository.findAllByCompanyId(loggedInUser.getCompany().getId());

        return clientVendors.stream().map(clientVendor -> {
            boolean hasInvoice = isClientHasInvoice(clientVendor.getId());
            ClientVendorDTO convert = mapperUtil.convert(clientVendor, new ClientVendorDTO());
            convert.setHasInvoice(hasInvoice);
            return convert;
                        })
                .collect(Collectors.toList());
    }


    @Override
    public ClientVendorDTO saveClientVendor(ClientVendorDTO clientVendorDTO) {
        UserDTO loggedInUser = securityService.getLoggedInUser();
        CompanyDTO companyDTO = loggedInUser.getCompany();
        Company company = mapperUtil.convert(companyDTO, new Company());

        ClientVendor clientVendorToSave = mapperUtil.convert(clientVendorDTO, new ClientVendor());
        clientVendorToSave.setCompany(company);//set company
        ClientVendor savedClientVendor = clientVendorRepository.save(clientVendorToSave);

        return mapperUtil.convert(savedClientVendor, new ClientVendorDTO());
    }


    @Override
    public ClientVendorDTO update(Long id, ClientVendorDTO clientVendorDTO) {
        ClientVendor byId = clientVendorRepository.findById(id).orElseThrow();
        ClientVendor clientVendor = mapperUtil.convert(clientVendorDTO, new ClientVendor());

        clientVendor.setId(byId.getId());
        clientVendor.setCompany( byId.getCompany() );

        ClientVendor saved = clientVendorRepository.save(clientVendor);
        return mapperUtil.convert(saved, new ClientVendorDTO());
    }

    @Override
    public void delete(Long id) {

        Optional<ClientVendor> clientVendorToBeDeleted = clientVendorRepository.findById(id);

        clientVendorToBeDeleted.ifPresent(clientVendor -> {
            if (!invoiceService.existsByClientVendorId(id)) {
                clientVendor.setIsDeleted(true);
                clientVendorRepository.save(clientVendor);
            } else {
                ClientVendorDTO clientVendorDTO = mapperUtil.convert(clientVendor, new ClientVendorDTO());
                if (clientVendorDTO != null) {
                    clientVendorDTO.setHasInvoice(true);
                }
            }
        });

    }

   @Override
    public List<ClientVendorDTO> findClientVendorByClientVendorTypeAndCompany(ClientVendorType clientVendorType) {
       Company company = mapperUtil.convert(securityService.getLoggedInUser().getCompany(),new Company());
        List<ClientVendor> byClientVendorTypeAndCompany =
                clientVendorRepository.findClientVendorByClientVendorTypeAndCompany(clientVendorType, company);

        return byClientVendorTypeAndCompany.stream()
                .map(clientVendor -> mapperUtil.convert(clientVendor, new ClientVendorDTO()))
                .collect(Collectors.toList());
    }

     @Override
    public BindingResult addTypeValidation(String type, BindingResult bindingResult) {
        if (clientVendorRepository.existsByClientVendorName(type)) {
            bindingResult.addError(new FieldError("newType", "type", "This type already exists."));
        }

        return bindingResult;

    }

    @Override
    public boolean isClientHasInvoice(Long id) {
        return invoiceService.existsByClientVendorId(id);
    }

    @Override
    public List<String> getCountries() {
        ResponseEntity<List<CountryInfoDTO>> countries = countryClient.getCountries(countriesApiKey);
        if (countries.getStatusCode().is2xxSuccessful()){
            return countries.getBody().stream()
                    .map(CountryInfoDTO::getName)
                    .collect(Collectors.toList());
        }
        return List.of();

    }


}


