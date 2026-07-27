package com.coco8talk.pm.common.chaos;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 *
 * @author silas
 * @since 2026/7/27 00:34
 */
@RestController
@RequestMapping("/chaos")
public class ChaosController {

    private final ChaosRegister chaosRegister;

    public ChaosController(ChaosRegister chaosRegister) {
        this.chaosRegister = chaosRegister;
    }

    @PostMapping("isEnabled")
    public Boolean isEnabled(@RequestParam String chaosName){
        return chaosRegister.isEnabled(chaosName);
    }

    @PostMapping("register/{name}")
    public void register(@PathVariable String name, @RequestBody ChaosFlagState chaosFlagState){
        chaosRegister.register(name, chaosFlagState);
    }

    @GetMapping("status")
    public Map<String, ChaosFlagState> status(){
        return chaosRegister.getAll();
    }

}
