package com.vone8.bookstore.orders;

import com.vone8.bookstore.common.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** The Orders REST API. */
@RestController
@RequestMapping("/v1/orders")
@Tag(name = "Orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listOrders", summary = "List orders", description = "Optionally only one customer's orders.")
    public List<Order> list(
            @Parameter(description = "Customer email, e.g. asha@vone8.com") @RequestParam(required = false) String customerEmail) {
        return service.list(customerEmail);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getOrder", summary = "Get one order")
    @ApiResponse(responseCode = "200", description = "The order")
    @ApiResponse(responseCode = "404", description = "No order with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Order get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(operationId = "placeOrder", summary = "Place an order", description = "Takes the books out of stock and records today's prices.")
    @ApiResponse(responseCode = "201", description = "Order placed")
    @ApiResponse(responseCode = "400", description = "Invalid body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "A book in the order does not exist",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Not enough copies in stock",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Order> place(@Valid @RequestBody OrderRequest request) {
        Order order = service.place(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @PostMapping("/{id}/cancel")
    @Operation(operationId = "cancelOrder", summary = "Cancel an order", description = "Marks the order CANCELLED and puts the copies back in stock.")
    @ApiResponse(responseCode = "200", description = "Cancelled")
    @ApiResponse(responseCode = "404", description = "No order with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Already cancelled",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Order cancel(@PathVariable Long id) {
        return service.cancel(id);
    }
}
