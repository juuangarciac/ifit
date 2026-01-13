package com.ifit.ronnie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.service.Eliud;
import com.ifit.ronnie.service.Kael;
import com.ifit.ronnie.service.Ronnie;
import com.ifit.ronnie.service.Serena;


@RestController
@RequestMapping("/chat")
public class AssistantController {
    
    @Autowired
    private Ronnie ronnie;

    @Autowired
    private Serena serena;

    @Autowired
    private Eliud eliud;

    @Autowired
    private Kael kael;

    @GetMapping("/ronnie")
    public String chatWithRonnie(@RequestParam int memoryId, @RequestParam String message) {
        return ronnie.chat(memoryId, message);
    }
    @GetMapping("/serena")
    public String chatWithSerena(@RequestParam int memoryId, @RequestParam String message) {
        return serena.chat(memoryId, message);
    }
    @GetMapping("/eliud")
    public String chatWithEliud(@RequestParam int memoryId, @RequestParam String message) {
        return eliud.chat(memoryId, message);
    }
    @GetMapping("/kael")
    public String chatWithKael(@RequestParam int memoryId, @RequestParam String message) {
        return kael.chat(memoryId, message);
    }
}
