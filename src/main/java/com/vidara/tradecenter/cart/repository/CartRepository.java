package com.vidara.tradecenter.cart.repository;

import com.vidara.tradecenter.cart.model.Cart;
import com.vidara.tradecenter.cart.model.enums.CartStatus;
import com.vidara.tradecenter.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUserAndStatus(User user, CartStatus status);

  Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

  boolean existsByUserAndStatus(User user, CartStatus status);
}
