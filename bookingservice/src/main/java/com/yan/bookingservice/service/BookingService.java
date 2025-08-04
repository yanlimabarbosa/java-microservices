package com.yan.bookingservice.service;

import com.yan.bookingservice.client.InventoryServiceClient;
import com.yan.bookingservice.entity.Customer;
import com.yan.bookingservice.repository.CustomerRepository;
import com.yan.bookingservice.request.BookingRequest;
import com.yan.bookingservice.response.BookingResponse;
import com.yan.bookingservice.response.InventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @Autowired
    public BookingService(final CustomerRepository customerRepository,
                          final InventoryServiceClient inventoryServiceClient) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public BookingResponse createBooking(final BookingRequest request){
        // Check if customer exists
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if(customer == null){
            throw new RuntimeException("User not found");
        }
        // Check if there is enough inventory
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        if(inventoryResponse.getCapacity() < request.getTicketCount()){
            throw new RuntimeException("Not enough inventory");
        }
        System.out.println(inventoryResponse);
        return BookingResponse.builder().build();
    }
}
