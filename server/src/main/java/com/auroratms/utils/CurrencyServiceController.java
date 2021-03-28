package com.auroratms.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Currency service for getting the exchange rates between different currencies
 * Created here so we don't have to deal with CORS configuration on the client
 */
@RestController
@RequestMapping("api/currency")
@PreAuthorize("isAuthenticated()")
public class CurrencyServiceController {

    @Value("${currency.service.apikey}")
    private String currencyServiceApiKey;

    @GetMapping("/exchangerate/{fromToCurrencies}")
    @ResponseBody
    public ResponseEntity getExchangeRate(@PathVariable String fromToCurrencies) {
        String url = String.format("https://free.currconv.com/api/v7/convert?q=%s&compact=ultra&apiKey=%s",
                fromToCurrencies, currencyServiceApiKey);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }
}
