# Issue #60 Implementation Plan: Variants as Separate Entity

## Executive Summary

Based on feedback from @alorma, we will implement **Interpretation 3: Separate Variant Entity**. Variants will be first-class entities that can be created independently, with a many-to-one relationship between instances and variants.

## Architecture Overview

### New Data Model

```
Product (1) -----> (*) Variant (1) -----> (*) ProductInstance
```

**Example:**
- Product: "Yogurt"
  - Variant: "Strawberry"
    - Instance 1: Expires Jan 15
    - Instance 2: Expires Jan 20
  - Variant: "Greek"
    - Instance 1: Expires Jan 18
  - Variant: "Vanilla"
    - Instance 1: Expires Jan 22

### Key Characteristics

1. **Variants are independent**: Can be created before any instances exist
2. **Variants belong to products**: Each variant is associated with one product
3. **Instances reference variants**: Each instance has a `variantId` (optional - instances can exist without variants)
4. **Backward compatible**: Existing instances with identifiers can be migrated

## Database Schema Changes

### Phase 1: New Variant Entity

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/VariantRoomEntity.kt` (NEW)

```kotlin
@Entity(
  tableName = "variants",
  foreignKeys = [
    ForeignKey(
      entity = ProductRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["productId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("productId")]
)
data class VariantRoomEntity(
  @PrimaryKey
  val id: String,
  val productId: String,
  val name: String,
  val createdAt: Long,
)
```

### Phase 2: Update ProductInstance Schema

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/ProductInstanceRoomEntity.kt`

Add new field (nullable for backward compatibility):
```kotlin
@Entity(
  tableName = "product_instances",
  foreignKeys = [
    ForeignKey(
      entity = ProductRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["productId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(  // NEW
      entity = VariantRoomEntity::class,
      parentColumns = ["id"],
      childColumns = ["variantId"],
      onDelete = ForeignKey.SET_NULL
    )
  ],
  indices = [Index("productId"), Index("variantId")]  // Added variantId index
)
data class ProductInstanceRoomEntity(
  @PrimaryKey
  val id: String,
  val productId: String,
  val identifier: String,  // Keep for backward compatibility
  val variantId: String? = null,  // NEW - optional reference to variant
  val expirationDate: Long,
  val pausedDate: Long? = null,
  val remainingDays: Int? = null,
  val consumedDate: Long? = null,
)
```

### Phase 3: Database Migration

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/AppDatabase.kt`

Add migration from current version to new version:

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
  override fun migrate(database: SupportSQLiteDatabase) {
    // 1. Create variants table
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS variants (
        id TEXT NOT NULL PRIMARY KEY,
        productId TEXT NOT NULL,
        name TEXT NOT NULL,
        createdAt INTEGER NOT NULL,
        FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE
      )
    """)
    database.execSQL("CREATE INDEX IF NOT EXISTS index_variants_productId ON variants(productId)")
    
    // 2. Migrate existing identifiers to variants
    // Get distinct identifier/productId combinations
    database.execSQL("""
      INSERT INTO variants (id, productId, name, createdAt)
      SELECT 
        lower(hex(randomblob(16))),
        productId,
        identifier,
        MIN(expirationDate)
      FROM product_instances
      WHERE consumedDate IS NULL
      GROUP BY productId, identifier
    """)
    
    // 3. Add variantId column to product_instances
    database.execSQL("ALTER TABLE product_instances ADD COLUMN variantId TEXT")
    
    // 4. Populate variantId based on identifier match
    database.execSQL("""
      UPDATE product_instances
      SET variantId = (
        SELECT v.id 
        FROM variants v 
        WHERE v.productId = product_instances.productId 
        AND v.name = product_instances.identifier
        LIMIT 1
      )
      WHERE consumedDate IS NULL
    """)
    
    // 5. Add index and foreign key
    database.execSQL("CREATE INDEX IF NOT EXISTS index_product_instances_variantId ON product_instances(variantId)")
  }
}
```

## Domain Model Changes

### New Domain Models

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/model/Variant.kt` (NEW)

```kotlin
data class Variant(
  val id: String,
  val productId: String,
  val name: String,
  val createdAt: Instant,
)
```

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/model/ProductWithVariants.kt` (NEW)

```kotlin
data class ProductWithVariants(
  val product: Product,
  val variants: ImmutableList<Variant>,
)
```

### Update ProductInstance

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/model/ProductInstance.kt`

Add variantId field:
```kotlin
data class ProductInstance(
  val id: String,
  val identifier: String,  // Keep for backward compatibility
  val variantId: String? = null,  // NEW
  val expirationDate: Instant,
  val status: InstanceStatus,
  val pausedDate: Instant? = null,
)
```

## Data Access Layer

### New VariantDao

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/VariantDao.kt` (NEW)

```kotlin
@Dao
interface VariantDao {
  
  @Query("SELECT * FROM variants WHERE productId = :productId ORDER BY name ASC")
  fun getVariantsByProduct(productId: String): Flow<List<VariantRoomEntity>>
  
  @Query("SELECT * FROM variants WHERE id = :variantId")
  suspend fun getVariant(variantId: String): VariantRoomEntity?
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVariant(variant: VariantRoomEntity)
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVariants(variants: List<VariantRoomEntity>)
  
  @Query("DELETE FROM variants WHERE id = :variantId")
  suspend fun deleteVariant(variantId: String)
  
  @Query("SELECT COUNT(*) FROM product_instances WHERE variantId = :variantId AND consumedDate IS NULL")
  suspend fun getActiveInstanceCount(variantId: String): Int
  
  // For backup/restore
  @Query("SELECT * FROM variants")
  suspend fun getAllVariantsSync(): List<VariantRoomEntity>
  
  @Query("DELETE FROM variants")
  suspend fun clearAllVariants()
}
```

### New VariantDataSource Interface

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/VariantDataSource.kt` (NEW)

```kotlin
interface VariantDataSource {
  
  fun getVariantsByProduct(productId: String): Flow<ImmutableList<Variant>>
  
  suspend fun getVariant(variantId: String): Variant?
  
  suspend fun createVariant(productId: String, name: String): Variant
  
  suspend fun deleteVariant(variantId: String): Result<Unit>
  
  suspend fun getActiveInstanceCount(variantId: String): Int
}
```

### Implementation

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/RoomVariantDataSource.kt` (NEW)

```kotlin
class RoomVariantDataSource(
  database: AppDatabase,
  private val appClock: AppClock,
) : VariantDataSource {
  
  private val variantDao = database.variantDao()
  
  override fun getVariantsByProduct(productId: String): Flow<ImmutableList<Variant>> {
    return variantDao.getVariantsByProduct(productId)
      .map { entities ->
        entities.map { it.toModel() }.toImmutableList()
      }
  }
  
  override suspend fun getVariant(variantId: String): Variant? {
    return variantDao.getVariant(variantId)?.toModel()
  }
  
  override suspend fun createVariant(productId: String, name: String): Variant {
    val variant = Variant(
      id = UUID.randomUUID().toString(),
      productId = productId,
      name = name,
      createdAt = appClock.now(),
    )
    variantDao.insertVariant(variant.toRoomEntity())
    return variant
  }
  
  override suspend fun deleteVariant(variantId: String): Result<Unit> {
    val instanceCount = variantDao.getActiveInstanceCount(variantId)
    return if (instanceCount > 0) {
      Result.failure(IllegalStateException("Cannot delete variant with active instances"))
    } else {
      variantDao.deleteVariant(variantId)
      Result.success(Unit)
    }
  }
  
  override suspend fun getActiveInstanceCount(variantId: String): Int {
    return variantDao.getActiveInstanceCount(variantId)
  }
}
```

## Use Cases

### New Use Cases

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/CreateVariantUseCase.kt` (NEW)

```kotlin
class CreateVariantUseCase(
  private val variantDataSource: VariantDataSource,
) {
  suspend operator fun invoke(productId: String, name: String): Result<Variant> {
    if (name.isBlank()) {
      return Result.failure(IllegalArgumentException("Variant name cannot be blank"))
    }
    return try {
      val variant = variantDataSource.createVariant(productId, name)
      Result.success(variant)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
```

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/GetProductVariantsUseCase.kt` (NEW)

```kotlin
class GetProductVariantsUseCase(
  private val variantDataSource: VariantDataSource,
) {
  operator fun invoke(productId: String): Flow<ImmutableList<Variant>> {
    return variantDataSource.getVariantsByProduct(productId)
  }
}
```

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/DeleteVariantUseCase.kt` (NEW)

```kotlin
class DeleteVariantUseCase(
  private val variantDataSource: VariantDataSource,
) {
  suspend operator fun invoke(variantId: String): Result<Unit> {
    return variantDataSource.deleteVariant(variantId)
  }
}
```

### Update AddInstanceToProductUseCase

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/usecase/AddInstanceToProductUseCase.kt`

Update to support variantId:
```kotlin
class AddInstanceToProductUseCase(
  private val productDataSource: ProductDataSource,
  private val variantDataSource: VariantDataSource,  // NEW
) {
  suspend operator fun invoke(
    productId: String,
    variantId: String?,  // NEW - optional variant
    identifier: String,  // Keep for backward compatibility
    expirationDate: LocalDate,
    quantity: Int = 1,
  ): Result<Unit> {
    // Validation
    if (identifier.isBlank()) {
      return Result.failure(IllegalArgumentException("Identifier cannot be blank"))
    }
    
    // Verify variant exists if provided
    if (variantId != null) {
      val variant = variantDataSource.getVariant(variantId)
      if (variant == null) {
        return Result.failure(IllegalArgumentException("Variant not found"))
      }
    }
    
    // Create instances
    repeat(quantity) {
      val instance = ProductInstance(
        id = UUID.randomUUID().toString(),
        identifier = identifier,
        variantId = variantId,  // NEW
        expirationDate = expirationDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
        status = InstanceStatus.Fresh,
      )
      productDataSource.addInstance(productId, instance)
    }
    
    return Result.success(Unit)
  }
}
```

## UI Changes

### Variant Management Screen (NEW)

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/variants/VariantsScreen.kt` (NEW)

Create a screen to manage variants for a product:
- List all variants for a product
- Add new variant
- Delete variant (if no active instances)
- Show instance count per variant

### Update CreateInstanceBottomSheet

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/product/create/CreateInstanceBottomSheet.kt`

Update to show variant selection:

```kotlin
@Composable
fun CreateInstanceBottomSheet(
  productId: String,
  availableVariants: ImmutableList<VariantUiModel>,  // NEW
  onSave: (variantId: String?, identifier: String, expirationDate: LocalDate, quantity: Int) -> Unit,
  onCreateVariant: () -> Unit,  // NEW - opens variant creation dialog
  onDismiss: () -> Unit,
  // ... other parameters
) {
  var selectedVariantId by remember { mutableStateOf<String?>(null) }
  var identifier by remember { mutableStateOf("") }
  
  // Variant selection dropdown
  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it }
  ) {
    TextField(
      value = selectedVariant?.name ?: "Select variant",
      label = { Text("Variant") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      readOnly = true,
      modifier = Modifier.fillMaxWidth().menuAnchor()
    )
    
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      // Existing variants
      availableVariants.forEach { variant ->
        DropdownMenuItem(
          text = { Text("${variant.name} (${variant.instanceCount})") },
          onClick = {
            selectedVariantId = variant.id
            identifier = variant.name
            expanded = false
          }
        )
      }
      
      Divider()
      
      // Create new variant option
      DropdownMenuItem(
        text = { 
          Row {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create new variant")
          }
        },
        onClick = {
          expanded = false
          onCreateVariant()
        }
      )
    }
  }
  
  // ... rest of form (expiration date, quantity, etc.)
}
```

### Create Variant Dialog (NEW)

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/variants/CreateVariantDialog.kt` (NEW)

```kotlin
@Composable
fun CreateVariantDialog(
  productId: String,
  onVariantCreated: (Variant) -> Unit,
  onDismiss: () -> Unit,
) {
  var variantName by remember { mutableStateOf("") }
  var isCreating by remember { mutableStateOf(false) }
  
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create New Variant") },
    text = {
      TextField(
        value = variantName,
        onValueChange = { variantName = it },
        label = { Text("Variant Name") },
        placeholder = { Text("e.g., Strawberry, Greek, Low-fat") },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      Button(
        onClick = {
          isCreating = true
          // Call ViewModel to create variant
        },
        enabled = variantName.isNotBlank() && !isCreating
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
```

## Updated Grouping Logic

### Update ProductInstanceGroup

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/model/ProductInstanceGroup.kt`

Add variant reference:
```kotlin
data class ProductInstanceGroup(
  val identifier: String,  // Keep for backward compatibility
  val variantId: String? = null,  // NEW
  val variantName: String? = null,  // NEW - for display
  val instances: ImmutableList<ProductInstance>,
)
```

### Update Use Cases

Modify `ObtainDashboardProductsUseCase` and `ObtainProductDetailUseCase` to group by `variantId` first, falling back to `identifier` for backward compatibility:

```kotlin
val groups = productWithInstances.instances
  .groupBy { instance -> 
    // Group by variantId if present, otherwise by identifier
    instance.variantId ?: instance.identifier
  }
  .map { (key, instancesInGroup) ->
    val firstInstance = instancesInGroup.first()
    ProductInstanceGroup(
      identifier = firstInstance.identifier,
      variantId = firstInstance.variantId,
      variantName = firstInstance.variantId?.let { 
        // Fetch variant name from variant entity
        variantDataSource.getVariant(it)?.name 
      },
      instances = instancesInGroup
        .sortedWith(instanceComparator)
        .toImmutableList()
    )
  }
  .sortedWith(groupComparator)
  .toImmutableList()
```

## Dependency Injection

### Update Koin Modules

**File**: `app/src/main/kotlin/com/alorma/caducity/di/AppModule.kt`

```kotlin
val variantModule = module {
  // Data sources
  singleOf(::RoomVariantDataSource) bind VariantDataSource::class
  
  // Use cases
  factoryOf(::CreateVariantUseCase)
  factoryOf(::GetProductVariantsUseCase)
  factoryOf(::DeleteVariantUseCase)
  
  // ViewModels
  viewModelOf(::VariantsViewModel)
}
```

Add to application:
```kotlin
startKoin {
  modules(
    appModule,
    dataModule,
    variantModule,  // NEW
    // ... other modules
  )
}
```

## String Resources

### Add New Strings

**File**: `app/src/main/res/values/strings.xml`

```xml
<!-- Variants -->
<string name="variant_label">Variant</string>
<string name="variant_placeholder">Select or create variant</string>
<string name="variant_name_label">Variant Name</string>
<string name="variant_name_placeholder">e.g., Strawberry, Greek, Low-fat</string>
<string name="variant_create_title">Create New Variant</string>
<string name="variant_create_button">Create Variant</string>
<string name="variant_delete_confirm">Delete variant "%1$s"?</string>
<string name="variant_delete_error_has_instances">Cannot delete variant with active instances</string>
<string name="variant_instance_count">%1$d items</string>
<string name="variant_manage_title">Manage Variants</string>
<string name="variant_empty_state">No variants yet. Create one to organize your instances.</string>
```

Translate to Spanish (es) and Catalan (ca).

## Testing Strategy

### Unit Tests

1. **VariantDao tests**:
   - Insert variant
   - Query variants by product
   - Delete variant
   - Get active instance count

2. **VariantDataSource tests**:
   - Create variant
   - Cannot delete variant with instances
   - Get variants by product

3. **Use case tests**:
   - CreateVariantUseCase validates name
   - DeleteVariantUseCase prevents deletion with instances
   - AddInstanceToProductUseCase with variantId

### Integration Tests

1. **Migration test**:
   - Existing identifiers migrate to variants
   - VariantId populated correctly
   - Backward compatibility maintained

2. **End-to-end flow**:
   - Create product
   - Create variant
   - Create instance with variant
   - View grouped instances

### UI Tests

1. **Variant management screen**:
   - List variants
   - Create variant
   - Delete variant

2. **Instance creation**:
   - Select existing variant
   - Create new variant inline
   - Backward compatibility (instances without variants)

## Migration Strategy

### Backward Compatibility

1. **Keep identifier field**: Ensures existing code continues to work
2. **Optional variantId**: Instances can exist without variants
3. **Auto-migration**: Existing identifiers become variants automatically
4. **Graceful degradation**: If variant lookup fails, fall back to identifier

### User-Facing Changes

1. **Existing data**: All existing instances grouped by identifier become grouped by auto-created variants
2. **New workflow**: Users can pre-create variants before adding instances
3. **UI enhancement**: Variant dropdown shows existing variants with instance counts
4. **No data loss**: All existing instances and their groupings are preserved

## Implementation Timeline

### Phase 1: Database & Domain (Week 1)
- [ ] Create VariantRoomEntity
- [ ] Create VariantDao
- [ ] Write database migration
- [ ] Update ProductInstanceRoomEntity schema
- [ ] Create Variant domain model
- [ ] Create VariantDataSource interface and implementation
- [ ] Write unit tests for data layer

### Phase 2: Use Cases (Week 1-2)
- [ ] Create variant management use cases
- [ ] Update AddInstanceToProductUseCase
- [ ] Update grouping logic in existing use cases
- [ ] Write use case tests

### Phase 3: UI Components (Week 2)
- [ ] Create VariantsScreen
- [ ] Create CreateVariantDialog
- [ ] Update CreateInstanceBottomSheet with variant selection
- [ ] Add navigation to variant management

### Phase 4: Integration & Testing (Week 2-3)
- [ ] Integration testing
- [ ] Migration testing
- [ ] UI testing
- [ ] Manual QA on various scenarios

### Phase 5: Localization & Polish (Week 3)
- [ ] Add all string resources
- [ ] Translate to es and ca
- [ ] Polish UI/UX
- [ ] Code review and refinement

**Total Estimated Time**: 2-3 weeks for complete implementation

## Risk Assessment

### High Risk Areas
- **Database migration**: Must handle existing data correctly
- **Backward compatibility**: Ensure old instances still work
- **Cascade deletes**: Variant deletion must be handled carefully

### Mitigation
- Comprehensive migration tests with sample data
- Keep identifier field as fallback
- Prevent variant deletion if instances exist
- Staged rollout with feature flag

## Success Criteria

- [ ] Users can create variants independently
- [ ] Variants appear in dropdown when creating instances
- [ ] Existing instances migrate correctly to variants
- [ ] No data loss during migration
- [ ] All tests pass
- [ ] UI is intuitive and responsive
- [ ] Properly localized (en, es, ca)
- [ ] Backward compatible with existing data

---

**Status**: Ready for implementation based on @alorma's decision for Interpretation 3.

**Next Steps**:
1. Review and approve this plan
2. Create GitHub issues for each phase
3. Begin Phase 1 implementation
