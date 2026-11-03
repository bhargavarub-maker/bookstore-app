package com.vone8.bookstore.orders;

import com.vone8.bookstore.books.Book;
import com.vone8.bookstore.books.BookService;
import com.vone8.bookstore.common.ConflictException;
import com.vone8.bookstore.common.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Business logic for orders: checks stock, records prices and updates stock. */
@Service
public class OrderService {

    private final OrderRepository repository;
    private final BookService bookService;

    public OrderService(OrderRepository repository, BookService bookService) {
        this.repository = repository;
        this.bookService = bookService;
    }

    public List<Order> list(String customerEmail) {
        if (customerEmail == null) {
            return repository.findAll(Sort.by("id"));
        }
        return repository.findByCustomerEmailIgnoreCaseOrderByIdAsc(customerEmail);
    }

    public Order get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    @Transactional
    public Order place(OrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderRequest.Line line : request.items()) {
            Book book = bookService.get(line.bookId()); // 404 if the book does not exist
            if (book.getStock() < line.quantity()) {
                throw new ConflictException("Not enough stock for '" + book.getTitle() + "': " + book.getStock() + " left");
            }
            book.setStock(book.getStock() - line.quantity());
            items.add(new OrderItem(book.getId(), book.getTitle(), line.quantity(), book.getPrice()));
        }
        return repository.save(new Order(request.customerName(), request.customerEmail(), items));
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = get(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Order " + id + " is already cancelled");
        }
        // Put the copies back on the shelf (skip books that were deleted since).
        for (OrderItem item : order.getItems()) {
            try {
                Book book = bookService.get(item.getBookId());
                book.setStock(book.getStock() + item.getQuantity());
            } catch (NotFoundException ignored) {
                // book no longer in the catalog
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        return order;
    }
}
