package com.vidara.tradecenter.product.service;

import com.vidara.tradecenter.product.model.Brand;
import com.vidara.tradecenter.product.model.Category;
import com.vidara.tradecenter.product.model.Product;
import com.vidara.tradecenter.product.model.enums.ProductStatus;
import com.vidara.tradecenter.product.repository.BrandRepository;
import com.vidara.tradecenter.product.repository.CategoryRepository;
import com.vidara.tradecenter.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for Product-related repository and service operations.
 * Uses @DataJpaTest with PostgreSQL (transactional rollback after each test).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductServiceTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product testProduct;
    private Category testCategory;
    private Brand testBrand;

    private String testSuffix;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().substring(0, 8);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Electronics-" + testSuffix);
        testCategory.setSlug("electronics-" + testSuffix);
        testCategory.setDescription("Electronic devices");
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create test brand
        testBrand = new Brand();
        testBrand.setName("TechBrand-" + testSuffix);
        testBrand.setSlug("techbrand-" + testSuffix);
        testBrand.setDescription("A technology brand");
        testBrand = entityManager.persistAndFlush(testBrand);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Laptop-" + testSuffix);
        testProduct.setSlug("test-laptop-" + testSuffix);
        testProduct.setSku("SKU-LAPTOP-" + testSuffix);
        testProduct.setBasePrice(new BigDecimal("999.99"));
        testProduct.setSalePrice(new BigDecimal("899.99"));
        testProduct.setStatus(ProductStatus.ACTIVE);
        testProduct.setDescription("A high-quality test laptop");
        testProduct.setCategory(testCategory);
        testProduct.setBrand(testBrand);
        testProduct = entityManager.persistAndFlush(testProduct);

        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteById(testProduct.getId());
        entityManager.flush();
        categoryRepository.deleteById(testCategory.getId());
        brandRepository.deleteById(testBrand.getId());
        entityManager.flush();
    }


    // ==================== GET PRODUCT BY ID ====================

    @Nested
    @DisplayName("Get Product By ID")
    class GetProductById {

        @Test
        @DisplayName("Should return product when valid ID is provided")
        void shouldReturnProductWhenValidId() {
            Optional<Product> found = productRepository.findById(testProduct.getId());

            assertTrue(found.isPresent());
            assertEquals("Test Laptop-" + testSuffix, found.get().getName());
            assertEquals("SKU-LAPTOP-" + testSuffix, found.get().getSku());
        }

        @Test
        @DisplayName("Should return empty when product ID does not exist")
        void shouldReturnEmptyWhenInvalidId() {
            Optional<Product> found = productRepository.findById(99999L);

            assertFalse(found.isPresent());
        }
    }


    // ==================== GET PRODUCT BY SLUG ====================

    @Nested
    @DisplayName("Get Product By Slug")
    class GetProductBySlug {

        @Test
        @DisplayName("Should find product by slug")
        void shouldFindProductBySlug() {
            Optional<Product> found = productRepository.findBySlug("test-laptop-" + testSuffix);

            assertTrue(found.isPresent());
            assertEquals("Test Laptop-" + testSuffix, found.get().getName());
        }

        @Test
        @DisplayName("Should return empty for non-existent slug")
        void shouldReturnEmptyForNonExistentSlug() {
            Optional<Product> found = productRepository.findBySlug("nonexistent-slug-xyz");

            assertFalse(found.isPresent());
        }
    }


    // ==================== GET PRODUCT BY SKU ====================

    @Nested
    @DisplayName("Get Product By SKU")
    class GetProductBySku {

        @Test
        @DisplayName("Should find product by SKU")
        void shouldFindProductBySku() {
            Optional<Product> found = productRepository.findBySku("SKU-LAPTOP-" + testSuffix);

            assertTrue(found.isPresent());
            assertEquals("Test Laptop-" + testSuffix, found.get().getName());
        }

        @Test
        @DisplayName("Should return true when SKU exists")
        void shouldReturnTrueWhenSkuExists() {
            assertTrue(productRepository.existsBySku("SKU-LAPTOP-" + testSuffix));
        }

        @Test
        @DisplayName("Should return false when SKU does not exist")
        void shouldReturnFalseWhenSkuNotExists() {
            assertFalse(productRepository.existsBySku("NON-EXISTENT-SKU"));
        }
    }


    // ==================== CREATE PRODUCT ====================

    @Nested
    @DisplayName("Create Product")
    class CreateProduct {

        @Test
        @DisplayName("Should save product with all fields")
        void shouldSaveProductWithAllFields() {
            Product newProduct = new Product();
            newProduct.setName("New Phone-" + testSuffix);
            newProduct.setSlug("new-phone-" + testSuffix);
            newProduct.setSku("SKU-PHONE-" + testSuffix);
            newProduct.setBasePrice(new BigDecimal("599.99"));
            newProduct.setStatus(ProductStatus.ACTIVE);
            newProduct.setDescription("A new phone");
            newProduct.setWeight(new BigDecimal("0.18"));
            newProduct.setDimensions("15x7x0.8 cm");
            newProduct.setCategory(testCategory);
            newProduct.setBrand(testBrand);

            Product saved = productRepository.save(newProduct);

            assertNotNull(saved.getId());
            assertEquals("New Phone-" + testSuffix, saved.getName());
            assertEquals("new-phone-" + testSuffix, saved.getSlug());
            assertEquals("SKU-PHONE-" + testSuffix, saved.getSku());
            assertEquals(new BigDecimal("599.99"), saved.getBasePrice());
            assertEquals(new BigDecimal("0.18"), saved.getWeight());
            assertEquals("15x7x0.8 cm", saved.getDimensions());

            // Cleanup
            productRepository.deleteById(saved.getId());
        }

        @Test
        @DisplayName("Should default status to DRAFT")
        void shouldDefaultStatusToDraft() {
            Product newProduct = new Product();
            newProduct.setName("Draft Product-" + testSuffix);
            newProduct.setSlug("draft-product-" + testSuffix);
            newProduct.setSku("SKU-DRAFT-" + testSuffix);
            newProduct.setBasePrice(new BigDecimal("49.99"));

            Product saved = productRepository.save(newProduct);

            assertEquals(ProductStatus.DRAFT, saved.getStatus());

            // Cleanup
            productRepository.deleteById(saved.getId());
        }

        @Test
        @DisplayName("Should reject duplicate SKU")
        void shouldRejectDuplicateSku() {
            Product duplicate = new Product();
            duplicate.setName("Duplicate-" + testSuffix);
            duplicate.setSlug("duplicate-" + testSuffix);
            duplicate.setSku("SKU-LAPTOP-" + testSuffix); // Same SKU as testProduct
            duplicate.setBasePrice(new BigDecimal("100.00"));

            assertThrows(Exception.class, () -> {
                productRepository.saveAndFlush(duplicate);
            });
        }
    }


    // ==================== UPDATE PRODUCT ====================

    @Nested
    @DisplayName("Update Product")
    class UpdateProduct {

        @Test
        @DisplayName("Should update product name and price")
        void shouldUpdateProductNameAndPrice() {
            Product product = productRepository.findById(testProduct.getId()).orElseThrow();
            product.setName("Updated Laptop-" + testSuffix);
            product.setBasePrice(new BigDecimal("1099.99"));

            Product updated = productRepository.save(product);

            assertEquals("Updated Laptop-" + testSuffix, updated.getName());
            assertEquals(new BigDecimal("1099.99"), updated.getBasePrice());
        }

        @Test
        @DisplayName("Should update product status")
        void shouldUpdateProductStatus() {
            Product product = productRepository.findById(testProduct.getId()).orElseThrow();
            product.setStatus(ProductStatus.INACTIVE);

            Product updated = productRepository.save(product);

            assertEquals(ProductStatus.INACTIVE, updated.getStatus());
        }

        @Test
        @DisplayName("Should update product category")
        void shouldUpdateProductCategory() {
            Category newCategory = new Category();
            newCategory.setName("Computers-" + testSuffix);
            newCategory.setSlug("computers-" + testSuffix);
            newCategory = entityManager.persistAndFlush(newCategory);

            Product product = productRepository.findById(testProduct.getId()).orElseThrow();
            product.setCategory(newCategory);
            Product updated = productRepository.save(product);

            assertEquals(newCategory.getId(), updated.getCategory().getId());

            // Cleanup
            product.setCategory(testCategory);
            productRepository.save(product);
            categoryRepository.deleteById(newCategory.getId());
        }
    }


    // ==================== DELETE PRODUCT ====================

    @Nested
    @DisplayName("Delete Product")
    class DeleteProduct {

        @Test
        @DisplayName("Should delete product by ID")
        void shouldDeleteProduct() {
            Product toDelete = new Product();
            toDelete.setName("ToDelete-" + testSuffix);
            toDelete.setSlug("to-delete-" + testSuffix);
            toDelete.setSku("SKU-DELETE-" + testSuffix);
            toDelete.setBasePrice(new BigDecimal("10.00"));
            toDelete = entityManager.persistAndFlush(toDelete);

            Long deleteId = toDelete.getId();
            productRepository.deleteById(deleteId);
            entityManager.flush();

            assertFalse(productRepository.findById(deleteId).isPresent());
        }
    }


    // ==================== FIND BY STATUS ====================

    @Nested
    @DisplayName("Find Products By Status")
    class FindByStatus {

        @Test
        @DisplayName("Should find active products")
        void shouldFindActiveProducts() {
            Pageable pageable = PageRequest.of(0, 100);
            Page<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);

            assertTrue(activeProducts.getTotalElements() >= 1);
            assertTrue(activeProducts.getContent().stream()
                    .anyMatch(p -> p.getSku().equals("SKU-LAPTOP-" + testSuffix)));
        }

        @Test
        @DisplayName("Should return empty for status with no products")
        void shouldReturnEmptyForStatusWithNoProducts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> inactiveProducts = productRepository.findByStatus(ProductStatus.INACTIVE, pageable);

            assertEquals(0, inactiveProducts.getTotalElements());
        }
    }


    // ==================== FIND BY CATEGORY ====================

    @Nested
    @DisplayName("Find Products By Category")
    class FindByCategory {

        @Test
        @DisplayName("Should find products by category ID")
        void shouldFindProductsByCategoryId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> products = productRepository.findByCategoryId(testCategory.getId(), pageable);

            assertTrue(products.getTotalElements() >= 1);
            assertTrue(products.getContent().stream()
                    .anyMatch(p -> p.getSku().equals("SKU-LAPTOP-" + testSuffix)));
        }

        @Test
        @DisplayName("Should return empty for non-existent category")
        void shouldReturnEmptyForNonExistentCategory() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> products = productRepository.findByCategoryId(99999L, pageable);

            assertEquals(0, products.getTotalElements());
        }
    }


    // ==================== FIND BY BRAND ====================

    @Nested
    @DisplayName("Find Products By Brand")
    class FindByBrand {

        @Test
        @DisplayName("Should find products by brand ID")
        void shouldFindProductsByBrandId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> products = productRepository.findByBrandId(testBrand.getId(), pageable);

            assertTrue(products.getTotalElements() >= 1);
        }
    }


    // ==================== SEARCH BY KEYWORD ====================

    @Nested
    @DisplayName("Search Products By Keyword")
    class SearchByKeyword {

        @Test
        @DisplayName("Should find products matching name keyword")
        void shouldFindByNameKeyword() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.searchByKeyword("Laptop-" + testSuffix, pageable);

            assertTrue(results.getTotalElements() >= 1);
        }

        @Test
        @DisplayName("Should find products matching description keyword")
        void shouldFindByDescriptionKeyword() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.searchByKeyword("high-quality", pageable);

            assertTrue(results.getTotalElements() >= 1);
        }

        @Test
        @DisplayName("Should return empty for non-matching keyword")
        void shouldReturnEmptyForNonMatchingKeyword() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> results = productRepository.searchByKeyword("zzz-no-match-" + testSuffix, pageable);

            assertEquals(0, results.getTotalElements());
        }
    }


    // ==================== PAGINATION ====================

    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @DisplayName("Should return correct page size")
        void shouldReturnCorrectPageSize() {
            // Add more products
            for (int i = 0; i < 5; i++) {
                Product p = new Product();
                p.setName("PagProduct-" + i + "-" + testSuffix);
                p.setSlug("pag-product-" + i + "-" + testSuffix);
                p.setSku("SKU-PAG-" + i + "-" + testSuffix);
                p.setBasePrice(new BigDecimal("10.00"));
                p.setStatus(ProductStatus.ACTIVE);
                p.setCategory(testCategory);
                entityManager.persistAndFlush(p);
            }

            Pageable pageable = PageRequest.of(0, 3);
            Page<Product> page = productRepository.findByCategoryId(testCategory.getId(), pageable);

            assertEquals(3, page.getSize());
            assertTrue(page.getTotalElements() >= 6); // 1 testProduct + 5 extra
        }
    }


    // ==================== JPA SPECIFICATION FILTER ====================

    @Nested
    @DisplayName("JPA Specification Filter")
    class SpecificationFilter {

        @Test
        @DisplayName("Should filter by category using specification")
        void shouldFilterByCategory() {
            Specification<Product> spec = (root, query, cb) ->
                    cb.equal(root.get("category").get("id"), testCategory.getId());

            Page<Product> results = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertTrue(results.getTotalElements() >= 1);
            results.getContent().forEach(p ->
                    assertEquals(testCategory.getId(), p.getCategory().getId()));
        }

        @Test
        @DisplayName("Should filter by price range using specification")
        void shouldFilterByPriceRange() {
            Specification<Product> spec = Specification.where(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("basePrice"), new BigDecimal("500.00"))
            ).and(
                    (root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), new BigDecimal("1500.00"))
            );

            Page<Product> results = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertTrue(results.getTotalElements() >= 1);
            results.getContent().forEach(p -> {
                assertTrue(p.getBasePrice().compareTo(new BigDecimal("500.00")) >= 0);
                assertTrue(p.getBasePrice().compareTo(new BigDecimal("1500.00")) <= 0);
            });
        }

        @Test
        @DisplayName("Should filter by combined category and status")
        void shouldFilterByCategoryAndStatus() {
            Specification<Product> spec = Specification.where(
                    (root, query, cb) -> cb.equal(root.get("category").get("id"), testCategory.getId())
            ).and(
                    (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE)
            );

            Page<Product> results = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertTrue(results.getTotalElements() >= 1);
            results.getContent().forEach(p -> {
                assertEquals(testCategory.getId(), p.getCategory().getId());
                assertEquals(ProductStatus.ACTIVE, p.getStatus());
            });
        }
    }
}