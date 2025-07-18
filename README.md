# E-commerce Discount Service

A comprehensive discount calculation system for fashion e-commerce platforms built with Spring Boot. This service handles multiple types of discounts including brand-specific offers, category deals, bank card offers, and voucher codes with proper stacking and validation.

## 🎯 Features

- **Brand-specific discounts** (e.g., "Min 40% off on PUMA")
- **Category-specific deals** (e.g., "Extra 10% off on T-shirts")
- **Bank card offers** (e.g., "10% instant discount on ICICI Bank cards")
- **Voucher codes** (e.g., "SUPER69 for 69% off")
- **Intelligent discount stacking** with proper order: Brand/Category → Voucher → Bank Offers
- **Customer tier-based eligibility**
- **Minimum cart value validation**
- **Maximum discount caps**
- **Comprehensive error handling**

## 🏗️ Architecture

### Design Principles

1. **Clean Architecture**: Clear separation of concerns with distinct layers (Model, Service, Repository)
2. **SOLID Principles**: Single responsibility, open/closed, dependency inversion
3. **Testability**: Comprehensive unit and integration tests with high coverage
4. **Extensibility**: Easy to add new discount types and rules
5. **Performance**: Efficient discount calculation algorithms

### Key Components

```
src/main/java/com/unifiz/ecommerce/discount/
├── model/              # Domain models and data structures
├── service/            # Business logic and discount calculations
├── repository/         # Data access layer
├── config/             # Configuration and test data setup
└── exception/          # Custom exceptions
```

### Discount Processing Flow

```
1. Validate Input (Cart Items, Customer, Payment Info)
2. Calculate Original Price
3. Apply Brand & Category Discounts (Parallel)
4. Apply Voucher Discount (If provided)
5. Apply Bank Offers (If payment info available)
6. Calculate Final Price & Generate Summary
```

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Installation & Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd ecommerce-discount-service
   ```

2. **Build the project**

   ```bash
   mvn clean compile
   ```

3. **Run tests**

   ```bash
   mvn test
   ```

4. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8080`

### Alternative: Running with JAR

```bash
mvn clean package
java -jar target/ecommerce-discount-service-0.0.1-SNAPSHOT.jar
```

## 📊 Test Scenarios

The application comes pre-loaded with test data demonstrating the assignment scenario:

### Primary Test Case: PUMA T-shirt Scenario

- **Product**: PUMA T-shirt (₹2,000 each)
- **Quantity**: 2 items
- **Total**: ₹4,000

**Applied Discounts:**

1. **Brand Discount**: 40% off on PUMA = ₹1,600 savings
2. **Category Discount**: 10% off on T-shirts = ₹400 savings
3. **Bank Offer**: 10% off with ICICI card = ₹200 savings
4. **Final Price**: ₹1,800 (₹2,200 total savings)

### Available Discount Codes

| Code       | Type       | Discount | Conditions                          |
| ---------- | ---------- | -------- | ----------------------------------- |
| `PUMA40`   | Brand      | 40% off  | PUMA products, min ₹1,000           |
| `TSHIRT10` | Category   | 10% off  | T-shirts, min ₹500                  |
| `ICICI10`  | Bank Offer | 10% off  | ICICI cards, min ₹1,000             |
| `SUPER69`  | Voucher    | 69% off  | Gold/Platinum customers, min ₹2,000 |
| `NIKE30`   | Brand      | 30% off  | Nike products, min ₹2,000           |
| `HDFC15`   | Bank Offer | 15% off  | HDFC cards, min ₹3,000              |
| `SHOES15`  | Category   | 15% off  | Shoes, min ₹2,000                   |

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DiscountServiceImplTest

# Run with coverage
mvn jacoco:prepare-agent test jacoco:report
```

### Test Coverage

- **Unit Tests**: Service layer with mocked dependencies
- **Integration Tests**: Full application context with real components
- **Edge Cases**: Error handling, validation, boundary conditions
- **Business Scenarios**: Complete discount stacking workflows

### Test Architecture

```
src/test/java/
├── service/                    # Unit tests with Mockito
│   └── DiscountServiceImplTest
└── integration/                # Integration tests
    └── DiscountServiceIntegrationTest
```

## 🔧 Configuration

### Discount Rule Management

Discount rules are configured through the `TestDataConfig` class and stored in an in-memory repository. In production, this would be backed by a database.

### Adding New Discount Rules

```java
DiscountRule newRule = DiscountRule.builder()
    .name("NEWBRAND50")
    .type(DiscountType.BRAND)
    .discountValue(new BigDecimal("50"))
    .isPercentage(true)
    .applicableBrands(List.of("NewBrand"))
    .minimumCartValue(new BigDecimal("1500"))
    .maximumDiscountAmount(new BigDecimal("3000"))
    .isActive(true)
    .validFrom(LocalDateTime.now())
    .validTo(LocalDateTime.now().plusDays(30))
    .build();

discountRuleRepository.save(newRule);
```

## 📚 API Documentation

### Core Service Interface

```java
public interface DiscountService {

    // Calculate discounts without voucher code
    DiscountedPrice calculateCartDiscounts(
        List<CartItem> cartItems,
        CustomerProfile customer,
        Optional<PaymentInfo> paymentInfo
    );

    // Calculate discounts with voucher code
    DiscountedPrice calculateCartDiscounts(
        List<CartItem> cartItems,
        CustomerProfile customer,
        String discountCode,
        Optional<PaymentInfo> paymentInfo
    );

    // Validate discount code
    boolean validateDiscountCode(
        String code,
        List<CartItem> cartItems,
        CustomerProfile customer
    );
}
```

### Sample Usage

```java
// Create cart items
List<CartItem> cartItems = Arrays.asList(
    CartItem.builder()
        .product(pumaProduct)
        .quantity(2)
        .size("L")
        .build()
);

// Customer profile
CustomerProfile customer = CustomerProfile.builder()
    .id("CUSTOMER_001")
    .tier("GOLD")
    .build();

// Payment information
PaymentInfo payment = PaymentInfo.builder()
    .method("CARD")
    .bankName("ICICI")
    .cardType("CREDIT")
    .build();

// Calculate discounts
DiscountedPrice result = discountService.calculateCartDiscounts(
    cartItems, customer, "SUPER69", Optional.of(payment)
);

System.out.println("Original Price: ₹" + result.getOriginalPrice());
System.out.println("Final Price: ₹" + result.getFinalPrice());
System.out.println("Applied Discounts: " + result.getAppliedDiscounts());
System.out.println("Message: " + result.getMessage());
```

## 🏛️ Design Decisions

### 1. Discount Stacking Order

**Decision**: Brand/Category → Voucher → Bank Offers

**Rationale**:

- Brand/category discounts are product-level and should be applied first
- Vouchers are customer-initiated and get priority over automatic bank offers
- Bank offers are applied last on the already discounted amount

### 2. Repository Pattern

**Decision**: Interface-based repository with in-memory implementation

**Rationale**:

- Abstraction allows easy swapping of data storage (database, cache, etc.)
- In-memory implementation perfect for demo/testing
- Clean separation of data access concerns

### 3. Builder Pattern for Models

**Decision**: Lombok @Builder for all model classes

**Rationale**:

- Immutable objects with clear construction
- Reduces boilerplate code
- Enhanced readability and maintainability

### 4. Exception Handling Strategy

**Decision**: Custom exceptions with detailed error messages

**Rationale**:

- Clear separation between validation and calculation errors
- Detailed error messages for debugging
- Proper exception propagation up the call stack

### 5. BigDecimal for Currency

**Decision**: Use BigDecimal for all monetary calculations

**Rationale**:

- Precision in financial calculations
- Avoids floating-point arithmetic issues
- Industry standard for currency handling

## 🔮 Future Enhancements

### Immediate Improvements

- [ ] REST API endpoints for web integration
- [ ] Database persistence layer
- [ ] Caching for frequently accessed discount rules
- [ ] Audit logging for discount applications

### Advanced Features

- [ ] Time-based discount campaigns
- [ ] Geographic restrictions
- [ ] Usage limits per customer
- [ ] A/B testing for discount strategies
- [ ] Analytics and reporting dashboard

### Performance Optimizations

- [ ] Parallel discount rule evaluation
- [ ] Redis caching for discount rules
- [ ] Database query optimization
- [ ] Bulk discount calculations

## 🧑‍💻 Development

### Code Quality Standards

- **Test Coverage**: Minimum 90% line coverage
- **Code Style**: Google Java Style Guide
- **Documentation**: Comprehensive JavaDoc for public APIs
- **Logging**: Structured logging with appropriate levels

### Git Workflow

```bash
# Feature development
git checkout -b feature/new-discount-type
git commit -m "feat: add customer tier-based discounts"
git push origin feature/new-discount-type

# Bug fixes
git checkout -b fix/discount-calculation-edge-case
git commit -m "fix: handle zero quantity cart items"
```

### Commit Message Convention

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation updates
- `test:` Test additions/modifications
- `refactor:` Code refactoring

## 📝 Technical Decisions & Assumptions

### Assumptions Made

1. **Single Currency**: All prices in INR (₹)
2. **In-Memory Storage**: Sufficient for demo purposes
3. **Customer Tiers**: Simple string-based tier system
4. **Synchronous Processing**: No async discount calculations needed
5. **Single Tenant**: No multi-tenancy requirements

### Technical Choices

1. **Spring Boot 3.2**: Latest stable version with Java 17 support
2. **JUnit 5**: Modern testing framework with better assertions
3. **Mockito**: Industry standard mocking framework
4. **Lombok**: Reduces boilerplate code significantly
5. **BigDecimal**: Precise monetary calculations

### Trade-offs

1. **Simplicity vs Flexibility**: Chose simple in-memory storage over complex database setup
2. **Performance vs Accuracy**: Chose BigDecimal accuracy over double performance
3. **Features vs Time**: Focused on core discount logic over advanced features

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Ensure all tests pass
5. Submit a pull request with clear description

## 📞 Support

For questions or issues:

- Create an issue in the repository
- Contact: [Your Contact Information]

---

**Built with ❤️ for Unifiz Backend Developer Assignment**
