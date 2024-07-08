package org.example.controller;

import org.example.service.CrptApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("document")
public class CrptController {

    private final CrptApi crptApi;

    public CrptController(CrptApi crptApi) {
        this.crptApi = crptApi;
    }

    @PostMapping("create")
    public String create() {
        CrptApi.Document document = new CrptApi.Document();
        return crptApi.create(document, "certificate");
    }

}
