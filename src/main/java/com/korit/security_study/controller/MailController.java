package com.korit.security_study.controller;

import com.korit.security_study.dto.SendMailReqDto;
import com.korit.security_study.security.model.Principal;
import com.korit.security_study.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMail(@RequestBody SendMailReqDto sendMailReqDto, @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(mailService.sendMail(sendMailReqDto, principal));
    }

    @GetMapping("/verify")
    public String verify(Model model, @RequestParam String verifyToken) {
        Map<String, Object> resultMap = mailService.verify(verifyToken);
        model.addAllAttributes(resultMap);
        return "result_page";
    }
}
