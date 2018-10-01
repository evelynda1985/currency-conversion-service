package com.in28minutes.micorservices.currencyconversionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CurrencyConversionController {

    @Autowired
    private CurrencyExchangeServiceProxy proxy;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/currency-converter/from/{from}/to/{to}/quantity/{quantity}")//localhost:8100/currency-converter/from/USD/to/CLO/quantity/1000
    public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity){

        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate()
                                                                .getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                                                                               CurrencyConversionBean.class,
                                                                               uriVariables);

        //we want to take the variables from the response
        CurrencyConversionBean response = responseEntity.getBody();

        return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(),
                quantity, quantity.multiply(response.getConversionMultiple()),
                response.getPort());

        //note verify that all the services involved here are up
        // localhost:8000/currency-exchange/from/USD/to/CLO
        //localhost:8100/currency-converter/from/USD/to/CLO/quantity/1000
    }

    @GetMapping("/currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")//localhost:8100/currency-converter/from/USD/to/CLO/quantity/1000
    public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from, @PathVariable String to, @PathVariable BigDecimal quantity){

        CurrencyConversionBean response = proxy.retrieveExchangeValue(from,to);

        logger.info("{}", response);

        return new CurrencyConversionBean(response.getId(), from, to, response.getConversionMultiple(),
                quantity, quantity.multiply(response.getConversionMultiple()),
                response.getPort());

        //note verify that all the services involved here are up
        // localhost:8000/currency-exchange/from/USD/to/CLO
        //localhost:8100/currency-converter/from/USD/to/CLO/quantity/1000
    }

}
