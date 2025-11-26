package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BlogController {

    @GetMapping("/blog")
    public String blog(){
        return "blog";
    }
}
