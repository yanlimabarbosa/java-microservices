package com.yan.bookingservice.controller;

import com.yan.bookingservice.request.BookingRequest;
import com.yan.bookingservice.response.BookingResponse;
import com.yan.bookingservice.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json", path = "/booking")
    public BookingResponse createBooking(@RequestBody BookingRequest request){
        return bookingService.createBooking(request);
    }
}
