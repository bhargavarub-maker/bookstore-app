package com.vone8.bookstore.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * One line of an order. It stores the book id, title and price at the time of purchase
 * rather than a database link to the books table, the way a separate Orders service would.
 */
@Entity
@Table(name = "order_items")
@Schema(description = "One book in an order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Schema(example = "1")
    private Long bookId;

    @Column(nullable = false)
    @Schema(example = "Dune")
    private String title;

    @Column(nullable = false)
    @Schema(example = "2")
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(example = "9.99")
    private BigDecimal unitPrice;

    protected OrderItem() {
        // required by JPA
    }

    public OrderItem(Long bookId, String title, int quantity, BigDecimal unitPrice) {
        this.bookId = bookId;
        this.title = title;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    @Schema(description = "quantity x unitPrice", example = "19.98")
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
