package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.entity.inheritance_single.StDocParent;
import com.mirjdev.examplesspring.entity.inheritance_single.repository.StDocBonusRepository;
import com.mirjdev.examplesspring.entity.inheritance_single.repository.StDocCouponRepository;
import com.mirjdev.examplesspring.entity.inheritance_single.repository.StDocParentRepository;
import com.mirjdev.examplesspring.entity.inheritance_single.repository.StDocPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doc/inheritance_single")
@RequiredArgsConstructor
public class DocInheritanceSingleController {

    private final StDocParentRepository docParentRepository;
    private final StDocBonusRepository docBonusRepository;
    private final StDocPaymentRepository docPaymentRepository;
    private final StDocCouponRepository couponRepository;


    @GetMapping("/1")
    public String get1() {
        List<StDocParent> all = docParentRepository.findAll();
        return all.toString();
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
