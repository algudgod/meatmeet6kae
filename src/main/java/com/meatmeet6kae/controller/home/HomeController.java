package com.meatmeet6kae.controller.home;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping(value="/home")
    public String home() {
        logger.debug("home in"); // 로그 찍기
        return "home";
    }
}
