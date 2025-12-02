package com.korit.security_study.controller;

import com.korit.security_study.dto.ModifyPasswordReqDto;
import com.korit.security_study.security.model.Principal;
import com.korit.security_study.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/modify/password")
    public ResponseEntity<?> modifyPassword(@RequestBody ModifyPasswordReqDto modifyPasswordReqDto, @AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(accountService.modifyPassword(modifyPasswordReqDto, principal));
    }

    @GetMapping("/principal")
    public ResponseEntity<?> getPrincipal(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(principal);
    }
}
