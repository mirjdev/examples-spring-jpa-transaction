package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.entity.inheritance_joined.repository.DocBonusRepository;
import com.mirjdev.examplesspring.entity.inheritance_joined.repository.DocCouponRepository;
import com.mirjdev.examplesspring.entity.inheritance_joined.repository.DocParentRepository;
import com.mirjdev.examplesspring.entity.inheritance_joined.repository.DocPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doc/inheritance_joined")
@RequiredArgsConstructor
public class DocInheritanceJoinedController {

    private final DocParentRepository docParentRepository;
    private final DocBonusRepository docBonusRepository;
    private final DocPaymentRepository docPaymentRepository;
    private final DocCouponRepository couponRepository;


    @GetMapping("/1")
    public String get1() {
        return docParentRepository.findAll().toString();
    }

    @GetMapping("/2")
    public String get2() {
        return docPaymentRepository.findAll().toString();
    }

    @GetMapping("/3")
    public String get3() {
        return docBonusRepository.findAll().toString();
    }

    @GetMapping("/4")
    public String get4() {
        return couponRepository.findAll().toString();
    }
}
