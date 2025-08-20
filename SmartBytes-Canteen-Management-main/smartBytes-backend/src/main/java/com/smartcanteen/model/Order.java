
package com.smartcanteen.model;

import com.smartcanteen.login.enity.EOrderStatus;
import com.smartcanteen.security.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Order contains foreign key of User, so it can be linked to a specific user
    // thats why it is Owner of this relationship but User does not contain foreign key of Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Removed: @CreationTimestamp
    // Removed: @Column(name = "order_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    // Removed: private LocalDateTime orderDate; // Field removed

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EOrderStatus status;

    // a single order can have multiple items
    // Order does not contain foreign key of OrderItem, but OrderItem contains foreign key of Order 
    // thats why we use mappedBy and OrderItem is owner of this relationship
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    

    
}