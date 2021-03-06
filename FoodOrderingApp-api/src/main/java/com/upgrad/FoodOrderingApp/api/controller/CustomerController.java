package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.upgrad.FoodOrderingApp.api.model.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;


@RestController @RequestMapping("/customer")
public class CustomerController {


    @Autowired private CustomerService customerService;

    @RequestMapping(method = RequestMethod.POST, path = "/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signUp(@RequestBody final SignupCustomerRequest signupCustomerRequest)
            throws SignUpRestrictedException {

        //validate this request.


        if (signupCustomerRequest.getFirstName().isEmpty()||signupCustomerRequest.getContactNumber().isEmpty()||signupCustomerRequest.getEmailAddress().isEmpty()||signupCustomerRequest.getPassword().isEmpty()) {
            throw new SignUpRestrictedException("SGR -005", "Except last name all fields should be filled");
        }

        signupCustomerRequest.getContactNumber().length();

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setEmailAddress(signupCustomerRequest.getEmailAddress());
        customerEntity.setLastname(signupCustomerRequest.getLastName());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setSalt("salt");
        customerEntity.setUuid(UUID.randomUUID().toString());

        final CustomerEntity responseCustomer = customerService.saveCustomer(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse();
        signupCustomerResponse.setId(responseCustomer.getUuid());
        signupCustomerResponse.setStatus("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String auth)
            throws AuthenticationFailedException {

        byte[] decode = Base64.getDecoder().decode(auth.split("Basic ")[1]);


        String decodedText = new String(decode);

        if (!decodedText.contains(":")) {
            throw new AuthenticationFailedException("ATH-003",
                    "Incorrect format of decoded customer name and password");
        }

        String decodedArray[] = decodedText.split(":");
        System.out.println(decodedArray[0]);

        CustomerAuthEntity customerAuthEntity = customerService.login(decodedArray[0], decodedArray[1]);

        CustomerEntity customer = customerAuthEntity.getCustomer();


        LoginResponse loginResponse = new LoginResponse();
        //create the login response
        loginResponse.setContactNumber(customer.getContactNumber());

        loginResponse.setEmailAddress(customer.getEmailAddress());
        loginResponse.setFirstName(customer.getFirstName());
        loginResponse.setId(customer.getUuid());
        loginResponse.setLastName(customer.getLastname());
        loginResponse.setMessage("Logged in Sucessfully");


        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", customerAuthEntity.getAccessToken());
        List<String> header = new ArrayList<>();
        header.add("accessToken");
        headers.setAccessControlAllowHeaders(header);

        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/logout", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String auth)
            throws AuthorizationFailedException {

        String accessToken = auth.split("BearerToken ")[1];

        customerService.logout(accessToken);


        LogoutResponse logoutResponse = new LogoutResponse();
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }


}