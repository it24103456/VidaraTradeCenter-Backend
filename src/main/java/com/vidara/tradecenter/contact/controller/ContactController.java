package com.vidara.tradecenter.contact.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.contact.dto.ContactInquiryRequest;
import com.vidara.tradecenter.contact.service.ContactInquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@Validated
public class ContactController {

    private final ContactInquiryService contactInquiryService;

    public ContactController(ContactInquiryService contactInquiryService) {
        this.contactInquiryService = contactInquiryService;
    }

    @PostMapping("/inquiry")
    public ResponseEntity<ApiResponse<Void>> submitInquiry(@Valid @RequestBody ContactInquiryRequest request) {
        contactInquiryService.sendInquiry(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Thanks — your message was delivered to our support team.", null));
    }
}
