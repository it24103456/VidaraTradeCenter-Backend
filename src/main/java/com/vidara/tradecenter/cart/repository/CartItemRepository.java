package com.vidara.tradecenter.cart.repository;

import com.vidara.tradecenter.cart.model.Cart;
import com.vidara.tradecenter.cart.model.CartItem;
import com.vidara.tradecenter.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

  Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

  void deleteByCartId(Long cartId);
}
