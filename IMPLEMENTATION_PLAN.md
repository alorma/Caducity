# Caducity - Grocery Expiration Tracker
## Implementation Plan & Architecture

---

## 📋 Executive Summary

Caducity is a Kotlin Multiplatform application designed to help users track their groceries and avoid food waste by monitoring expiration dates. The app will allow users to:
- Add groceries they've purchased
- Track multiple instances of the same product with different expiration dates
- Get notifications before items expire
- View statistics about their inventory

---

## 🏗️ Architecture Overview

### Technology Stack
- **Platform**: Kotlin Multiplatform (Android, JS, WASM)
- **UI Framework**: Jetpack Compose Multiplatform
- **Database**: SQLDelight (recommended for KMP)
- **Navigation**: Jetpack Navigation 3 (already in use)
- **Architecture Pattern**: MVI/MVVM (using Compose's state management, no ViewModel dependency)

### Project Structure
```
composeApp/src/
├── commonMain/kotlin/com/alorma/caducity/
│   ├── data/
│   │   ├── database/         # SQLDelight database definitions
│   │   ├── model/           # Data models
│   │   └── repository/      # Repository implementations
│   ├── domain/
│   │   ├── model/           # Domain models
│   │   └── usecase/         # Business logic use cases
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── dashboard/   # Main dashboard (existing)
│   │   │   ├── products/    # Product list screen
│   │   │   ├── product/     # Product detail screen
│   │   │   └── add/         # Add/Edit product screen
│   │   ├── components/      # Reusable UI components
│   │   └── theme/           # Theme configuration (existing)
│   └── App.kt               # Main app entry point (existing)
```

---

## 🗄️ Database Schema (SQLDelight)

### Entity Relationship Diagram
```
┌─────────────────┐         ┌──────────────────────┐
│    Product      │1      * │   ProductInstance    │
├─────────────────┤◄────────┤──────────────────────┤
│ id (PK)         │         │ id (PK)              │
│ name            │         │ productId (FK)       │
│ category        │         │ expirationDate       │
│ defaultShelfLife│         │ purchaseDate         │
│ barcode         │         │ quantity             │
│ imageUrl        │         │ status               │
│ createdAt       │         │ location             │
│ updatedAt       │         │ notes                │
└─────────────────┘         │ createdAt            │
                            │ updatedAt            │
                            └──────────────────────┘
```

### Table Definitions

#### 1. **Product Table**
Stores information about product types (e.g., "Milk", "Eggs")

```sql
CREATE TABLE Product (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    defaultShelfLifeDays INTEGER,
    barcode TEXT,
    imageUrl TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

CREATE INDEX idx_product_name ON Product(name);
CREATE INDEX idx_product_category ON Product(category);
CREATE INDEX idx_product_barcode ON Product(barcode);
```

**Fields:**
- `id`: Unique identifier
- `name`: Product name (e.g., "Whole Milk")
- `category`: Product category (e.g., "Dairy", "Vegetables", "Meat")
- `defaultShelfLifeDays`: Typical shelf life in days (optional)
- `barcode`: Barcode/UPC for scanning (optional, future feature)
- `imageUrl`: URL or local path to product image (optional)
- `createdAt`: Unix timestamp of creation
- `updatedAt`: Unix timestamp of last update

#### 2. **ProductInstance Table**
Stores individual purchases of products with specific expiration dates

```sql
CREATE TABLE ProductInstance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    productId INTEGER NOT NULL,
    expirationDate INTEGER NOT NULL,
    purchaseDate INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'active',
    location TEXT,
    notes TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (productId) REFERENCES Product(id) ON DELETE CASCADE
);

CREATE INDEX idx_instance_product ON ProductInstance(productId);
CREATE INDEX idx_instance_expiration ON ProductInstance(expirationDate);
CREATE INDEX idx_instance_status ON ProductInstance(status);
```

**Fields:**
- `id`: Unique identifier
- `productId`: Reference to Product table
- `expirationDate`: Unix timestamp of expiration
- `purchaseDate`: Unix timestamp of purchase
- `quantity`: Number of units (default: 1)
- `status`: Current status ('active', 'consumed', 'expired', 'discarded')
- `location`: Storage location (e.g., "Fridge", "Pantry", "Freezer")
- `notes`: User notes about this instance
- `createdAt`: Unix timestamp of creation
- `updatedAt`: Unix timestamp of last update

### Predefined Categories
```kotlin
enum class ProductCategory(val displayName: String) {
    DAIRY("Dairy"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    MEAT("Meat"),
    FISH("Fish"),
    BAKERY("Bakery"),
    BEVERAGES("Beverages"),
    FROZEN("Frozen Foods"),
    CANNED("Canned Goods"),
    SNACKS("Snacks"),
    CONDIMENTS("Condiments"),
    OTHER("Other")
}
```

### Storage Locations
```kotlin
enum class StorageLocation(val displayName: String) {
    FRIDGE("Refrigerator"),
    FREEZER("Freezer"),
    PANTRY("Pantry"),
    COUNTER("Counter"),
    OTHER("Other")
}
```

### Product Instance Status
```kotlin
enum class InstanceStatus(val displayName: String) {
    ACTIVE("Active"),
    CONSUMED("Consumed"),
    EXPIRED("Expired"),
    DISCARDED("Discarded")
}
```

---

## 📊 Data Models

### Domain Models

```kotlin
// Domain/Product.kt
data class Product(
    val id: Long,
    val name: String,
    val category: ProductCategory,
    val defaultShelfLifeDays: Int?,
    val barcode: String?,
    val imageUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

// Domain/ProductInstance.kt
data class ProductInstance(
    val id: Long,
    val productId: Long,
    val expirationDate: Long,
    val purchaseDate: Long,
    val quantity: Int,
    val status: InstanceStatus,
    val location: StorageLocation?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)

// Domain/ProductWithInstances.kt
// Note: import kotlinx.datetime.Clock
data class ProductWithInstances(
    val product: Product,
    val instances: List<ProductInstance>
) {
    val totalActiveQuantity: Int
        get() = instances.filter { it.status == InstanceStatus.ACTIVE }
            .sumOf { it.quantity }
    
    val nearestExpirationDate: Long?
        get() = instances.filter { it.status == InstanceStatus.ACTIVE }
            .minOfOrNull { it.expirationDate }
    
    val hasExpired: Boolean
        get() = nearestExpirationDate?.let { 
            it < Clock.System.now().toEpochMilliseconds() 
        } ?: false
}
```

---

## 🎯 Use Cases (Business Logic)

### Product Use Cases
- `GetAllProductsUseCase`: Retrieve all products
- `GetProductByIdUseCase`: Get specific product details
- `CreateProductUseCase`: Add new product
- `UpdateProductUseCase`: Update product information
- `DeleteProductUseCase`: Remove product (cascades to instances)
- `SearchProductsUseCase`: Search products by name or category

### Product Instance Use Cases
- `GetAllInstancesUseCase`: Retrieve all instances
- `GetInstancesByProductUseCase`: Get all instances of a product
- `GetActiveInstancesUseCase`: Get only active instances
- `GetExpiringInstancesUseCase`: Get instances expiring within N days
- `CreateInstanceUseCase`: Add new product instance
- `UpdateInstanceUseCase`: Update instance details
- `UpdateInstanceStatusUseCase`: Mark instance as consumed/expired
- `DeleteInstanceUseCase`: Remove instance

### Dashboard Use Cases
- `GetDashboardStatsUseCase`: Calculate dashboard statistics
  - Total active items
  - Items expiring soon (within 3 days)
  - Items expired
  - Items by category breakdown

---

## 🖥️ UI Screens

### 1. Dashboard Screen (Enhanced)
**Purpose**: Overview of grocery inventory

**Components:**
- Statistics cards:
  - Total active items
  - Items expiring soon (3 days)
  - Items expired today
- Quick action button: Add new product
- Recent additions list
- Expiring soon list (sorted by expiration date)

**Status**: ✅ Basic structure exists, needs enhancement

---

### 2. Products List Screen (NEW)
**Purpose**: View all products and their instances

**Components:**
- Search bar
- Filter by category dropdown
- Sort options (name, expiration date, quantity)
- Product cards showing:
  - Product name and category
  - Total active quantity
  - Nearest expiration date
  - Status indicator (expired/expiring soon/fresh)
- FAB (Floating Action Button) to add new product

---

### 3. Product Detail Screen (NEW)
**Purpose**: View all instances of a specific product

**Components:**
- Product header (name, category, image)
- Statistics summary
- List of instances with:
  - Expiration date
  - Purchase date
  - Quantity
  - Location
  - Status
  - Action buttons (edit, mark consumed, delete)
- Add new instance button

---

### 4. Add/Edit Product Screen (NEW)
**Purpose**: Create or modify product and instance

**Form Fields:**
- Product name (text input)
- Category (dropdown)
- Barcode (text input, optional)
- Default shelf life (number input, optional)
- Image (file picker, optional)
- **Instance details:**
  - Purchase date (date picker)
  - Expiration date (date picker)
  - Quantity (number input)
  - Location (dropdown)
  - Notes (text area)

**Actions:**
- Save button
- Cancel button

---

### 5. Settings Screen (Enhanced)
**Purpose**: App configuration

**New Settings:**
- Notification settings:
  - Enable expiration notifications
  - Notification lead time (1, 3, 7 days)
- Display settings:
  - Date format
  - Default view (list/grid)
- Data management:
  - Export database
  - Import database
  - Clear all data

**Status**: ✅ Basic structure exists, needs enhancement

---

## 🔔 Notification System (Future Phase)

### Notification Types
1. **Expiration Warning**: N days before expiration
2. **Expiration Day**: On the day of expiration
3. **Post-Expiration**: Item has expired

### Implementation Considerations
- Platform-specific implementations
- Android: WorkManager for scheduled notifications
- JS/WASM: Browser notifications API (limited)

---

## 📱 UI/UX Design Principles

### Color Coding for Expiration Status
- 🟢 **Fresh**: > 7 days until expiration (Green)
- 🟡 **Expiring Soon**: 3-7 days until expiration (Yellow/Amber)
- 🟠 **Critical**: 1-3 days until expiration (Orange)
- 🔴 **Expired**: Past expiration date (Red)

### Accessibility
- Material 3 theming with proper contrast ratios
- Support for dark mode
- Screen reader compatibility
- Touch target sizes (min 48dp)

---

## 🚀 Implementation Phases

### ✅ Phase 0: Planning (Current)
- [x] Define requirements
- [x] Design database schema
- [x] Plan architecture
- [x] Create this document

### 📦 Phase 1: Core Data Layer
- [ ] Add SQLDelight dependency
- [ ] Create database schema files
- [ ] Generate database classes
- [ ] Implement repository pattern
- [ ] Create domain models
- [ ] Write basic unit tests

### 🎨 Phase 2: Basic UI Implementation
- [ ] Create Product list screen
- [ ] Create Add/Edit product screen
- [ ] Update navigation to include new screens
- [ ] Add necessary icons
- [ ] Implement basic CRUD operations

### 📊 Phase 3: Dashboard Enhancement
- [ ] Connect dashboard to real data
- [ ] Implement statistics calculations
- [ ] Add expiring items widget
- [ ] Add quick actions

### 🔍 Phase 4: Advanced Features
- [ ] Implement search and filtering
- [ ] Add sorting options
- [ ] Product detail screen
- [ ] Instance management
- [ ] Batch operations

### 🔔 Phase 5: Notifications (Future)
- [ ] Design notification system
- [ ] Implement platform-specific code
- [ ] Add notification settings
- [ ] Test notification delivery

### 🎁 Phase 6: Polish & Additional Features
- [ ] Add barcode scanning (mobile only)
- [ ] Image capture/upload
- [ ] Data export/import
- [ ] Statistics and reports
- [ ] Onboarding flow
- [ ] App tutorial

---

## 🧪 Testing Strategy

### Unit Tests
- Repository layer tests
- Use case tests
- Data model validation
- Business logic verification

### UI Tests
- Screen composition tests
- Navigation flow tests
- User interaction tests

### Integration Tests
- Database operations
- End-to-end user flows

---

## 📦 Dependencies to Add

### Required
```toml
[versions]
sqldelight = "2.0.1"
kotlinx-datetime = "0.6.0"

[libraries]
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
sqldelight-js = { module = "app.cash.sqldelight:sqljs-driver", version.ref = "sqldelight" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

[plugins]
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

### Optional (Future Phases)
- Image loading library (Coil for Compose)
- Barcode scanning (ML Kit for Android)
- Export/Import (kotlinx-serialization - already included)

---

## 🎯 Success Metrics

### Functional Requirements
- ✅ Users can add products
- ✅ Users can track multiple instances per product
- ✅ Users can see expiration dates
- ✅ Users can mark items as consumed
- ✅ Dashboard shows accurate statistics

### Non-Functional Requirements
- ⚡ Fast database operations (< 100ms)
- 📱 Responsive UI across all platforms
- 🔒 Data persistence across app restarts
- 🎨 Consistent Material 3 design

---

## 📝 Notes & Considerations

### Data Persistence
- SQLDelight provides type-safe SQL for KMP
- Supports Android, iOS, JS, and Native targets
- Uses platform-specific drivers

### Date Handling
- Use `kotlinx-datetime` for cross-platform date operations
- Store dates as Unix timestamps (Long) in database
- Convert to/from LocalDate for UI display

### Image Storage
- Start with URL strings (external storage)
- Future: Local file storage per platform
- Consider size limits and caching

### Scalability
- Database indexed properly for common queries
- Pagination for large lists
- Lazy loading of images

### Privacy & Data
- All data stored locally (no server required)
- Export functionality for user data portability
- Clear data option for privacy

---

## 🔗 References

- [SQLDelight Documentation](https://cashapp.github.io/sqldelight/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material 3 Guidelines](https://m3.material.io/)
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)

---

**Document Version**: 1.0  
**Last Updated**: 2025-12-08  
**Status**: Planning Complete - Ready for Implementation
