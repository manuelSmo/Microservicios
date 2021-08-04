package com.example.servicecustomer.controller;

import com.example.servicecustomer.repository.entity.Customer;
import com.example.servicecustomer.repository.entity.Region;
import com.example.servicecustomer.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.xml.ws.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping(value = "/customers")
public class CustomerRest {

    @Autowired
    CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> listAllCustomers(@RequestParam(name = "regionId", required = false) Long regionId){
        List<Customer> customers = new ArrayList<>();
        if (null == regionId){
            customers = customerService.findCustomerAll();
            if (customers.isEmpty()){
                return ResponseEntity.noContent().build();
            }
        }else{
            Region region = new Region();
            region.setId(regionId);
            customers= customerService.findCustomersByRegion(region);
            if (customers == null){
                log.error("Customers with Region id {} not found.", region);
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok(customers);
    }

    @GetMapping(value = "/{id}")
    public  ResponseEntity<Customer> getCustomer(@PathVariable("id") Long id){
        log.info("Fetching Customer with id {}", id);
        Customer customer= customerService.getCustomer(id);
        if (null == customer){
        log.error("Customer with id {} not found.", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer, BindingResult result){
        log.info("Creating customer : {}", customer);
        if (result.hasErrors()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,this.formatMessage(result));
        }

        Customer customerDB = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerDB);

    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable("id") Long id,@RequestBody Customer customer){
        log.info("Updatig customer with id {}", id);

        Customer currentCustomer = customerService.getCustomer(id);
        if (currentCustomer == null){
            log.error("Customer with id {} not found.", id);
            return ResponseEntity.notFound().build();
        }

        customer.setId(id);
        currentCustomer = customerService.updateCustomer(customer);
        return ResponseEntity.ok(currentCustomer);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Customer> deleteCustomer(@PathVariable("id") Long id){
        log.info("Fetching & Deleting customer with id {}", id);
        Customer currentCustomer = customerService.getCustomer(id);
        if (currentCustomer == null){
            log.error("Customer with id {} not found.", id);
            return ResponseEntity.notFound().build();
        }

        currentCustomer = customerService.deleteCustomer(currentCustomer);
        return ResponseEntity.ok(currentCustomer);
    }


    private String formatMessage(BindingResult result){
        List<Map<String,String>>errors = result.getFieldErrors().stream()
                .map(err ->{
                    Map<String, String> error= new HashMap<>();
                    error.put(err.getField(),err.getDefaultMessage());
                    return error;
                }).collect(Collectors.toList());

        ErrorMessage errorMessage = ErrorMessage.builder()
                .code("01")
                .messages(errors).build();

        ObjectMapper mapper= new ObjectMapper();
        String jsonString = "";
        try{
            jsonString = mapper.writeValueAsString(errorMessage);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return jsonString;
    }


}
