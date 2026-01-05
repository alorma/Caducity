# Implementation Plan: Group Instances by Identifier

## Overview
Refactor the UI to group product instances by their `identifier` field, creating a hierarchical view: **Product → Identifier Groups → Individual Items**. This improves organization and reduces visual clutter for products with multiple variants (e.g., "Yogurt" with "Strawberry" and "Greek" variants).

## Goals
1. Group instances by identifier within each product
2. Add new `sku` column for barcode/SKU data (separating concerns from identifier)
3. Implement filtering exposed dropdown for identifier entry
4. Maintain existing dashboard view (summary + calendar)
5. Update product list view to show grouped instances

## Current State Analysis

### Database Schema
- **ProductInstanceRoomEntity** (`ProductInstanceRoomEntity.kt:20-29`):
  - `identifier: String` - Already non-nullable, used for grouping
  - No separate barcode/SKU field

### Domain Model
- **ProductInstance** (`ProductInstance.kt:6-24`):
  - `identifier: String` - Non-nullable
  - Status-based display logic already exists

### UI Current Behavior
- **ProductsListItem** (`ProductsListItem.kt:36-119`):
  - Flat list of instances under each product
  - Each instance shows: identifier, status badge, expiration date
  - Sorted by status priority then date

- **ProductsListMapper** (`ProductsListMapper.kt:13-79`):
  - Maps domain models to UI models
  - Sorts instances by status importance (Expired → ExpiringSoon → Fresh → Frozen)

## Implementation Tasks

### Phase 1: Database Schema Migration

#### Task 1.1: Add SKU Column
**Files**: `ProductInstanceRoomEntity.kt`, `AppDatabase.kt`

**Changes**:
1. Add `sku: String?` field to `ProductInstanceRoomEntity` (nullable)
2. Update Room schema version (increment by 1)
3. Create migration:
   ```kotlin
   val MIGRATION_X_Y = object : Migration(X, Y) {
     override fun migrate(database: SupportSQLiteDatabase) {
       database.execSQL("ALTER TABLE product_instances ADD COLUMN sku TEXT")
     }
   }
   ```
4. Add migration to `AppDatabase.kt`

**Acceptance Criteria**:
- Migration runs successfully on app upgrade
- No data loss
- `sku` column is nullable and defaults to null

---

#### Task 1.2: Update Domain Model
**Files**: `ProductInstance.kt`, `RoomEntityMapper.kt`

**Changes**:
1. Add `sku: String?` parameter to `ProductInstance` data class
2. Update `toModel()` mapper to include `sku`
3. Update `toRoomEntity()` mapper to include `sku`

**Acceptance Criteria**:
- Domain model includes `sku` field
- Mappers preserve `sku` data
- No compilation errors

---

### Phase 2: UI Models with Grouping

#### Task 2.1: Create Grouped UI Model
**Files**: New file `ProductsListGroupedUiModel.kt`

**Changes**:
1. Create new UI model for grouped instances:
   ```kotlin
   data class ProductInstanceGroup(
     val identifier: String,
     val instances: ImmutableList<ProductsListInstanceUiModel>,
   )

   sealed interface ProductsListUiModel {
     val id: String
     val name: String
     val description: String

     data class WithInstances(
       override val id: String,
       override val name: String,
       override val description: String,
       val groups: ImmutableList<ProductInstanceGroup>,
     ) : ProductsListUiModel

     data class Empty(...) : ProductsListUiModel
   }
   ```

**Acceptance Criteria**:
- Clear separation of grouping logic
- Immutable collections for Compose stability

---

#### Task 2.2: Update Mapper with Grouping Logic
**Files**: `ProductsListMapper.kt`

**Changes**:
1. Update `toUiModel()` to group instances by identifier:
   ```kotlin
   val groups = instances
     .groupBy { it.identifier }
     .map { (identifier, instancesInGroup) ->
       ProductInstanceGroup(
         identifier = identifier,
         instances = instancesInGroup
           .map { /* map to UI model */ }
           .sortedWith(instanceComparator)
           .toImmutableList()
       )
     }
     .sortedWith(groupComparator) // Sort groups by most urgent status
     .toImmutableList()
   ```

2. Create `groupComparator` to sort groups by:
   - Most urgent status in the group (Expired > ExpiringSoon > Fresh > Frozen)
   - Earliest expiration date in the group

**Acceptance Criteria**:
- Instances grouped by identifier
- Groups sorted by urgency
- Within groups, instances sorted by status then date

---

### Phase 3: UI Components

#### Task 3.1: Update ProductsListItem for Grouping
**Files**: `ProductsListItem.kt`

**Changes**:
1. Update composable to render groups:
   ```kotlin
   Column {
     Text(product.name) // Product header

     product.groups.forEach { group ->
       // Group header
       Text(
         text = group.identifier,
         style = MaterialTheme.typography.labelLarge
       )

       // Instances in group
       group.instances.forEach { instance ->
         Row { /* Instance item */ }
       }
     }
   }
   ```

2. Add visual hierarchy:
   - Product name: `titleMedium`
   - Identifier group: `labelLarge` with padding
   - Instances: Indented with `bodyMedium`

3. Show instance count per group: `"Strawberry (3)"`

**Acceptance Criteria**:
- Clear visual hierarchy
- Groups clearly separated
- Instance count visible per group
- Maintains existing status badge and date display

---

### Phase 4: Instance Creation/Editing UX

#### Task 4.1: Add Exposed Dropdown for Identifier Selection
**Files**: Create new `IdentifierSelectionField.kt`, update add/edit instance screens

**Changes**:
1. Create `IdentifierSelectionField` composable:
   ```kotlin
   @Composable
   fun IdentifierSelectionField(
     value: String,
     onValueChange: (String) -> Unit,
     existingIdentifiers: ImmutableList<String>,
     modifier: Modifier = Modifier,
   )
   ```

2. Use Material 3 `ExposedDropdownMenuBox`:
   - Shows existing identifiers as dropdown options
   - Filters options as user types
   - Allows free-text entry for new identifiers
   - Required field (validation)

3. Integration points:
   - Create instance flow
   - Edit instance flow (if exists)

**Acceptance Criteria**:
- Dropdown shows existing identifiers for current product
- Filters as user types
- Allows new identifier entry
- Required validation works
- Material 3 styling

---

#### Task 4.2: Update Data Source for Identifier Queries
**Files**: `ProductDao.kt`, `RoomProductDataSource.kt`

**Changes**:
1. Add query to get distinct identifiers for a product:
   ```kotlin
   @Query("""
     SELECT DISTINCT identifier
     FROM product_instances
     WHERE productId = :productId
     AND consumedDate IS NULL
     ORDER BY identifier ASC
   """)
   suspend fun getProductIdentifiers(productId: String): List<String>
   ```

2. Add method to `ProductDataSource` interface and implementation

**Acceptance Criteria**:
- Query returns distinct identifiers
- Excludes consumed instances
- Sorted alphabetically

---

### Phase 5: Integration & Polish

#### Task 5.1: Wire Up Identifier Dropdown in ViewModels
**Files**: ViewModels for add/edit instance screens

**Changes**:
1. Expose identifiers list in ViewModel state
2. Fetch identifiers when product is selected
3. Validate identifier is not empty on submit

**Acceptance Criteria**:
- Identifiers load when product selected
- Validation prevents empty submission
- State updates correctly

---

#### Task 5.2: Update String Resources
**Files**: `values*/strings.xml`

**Changes**:
1. Add strings for:
   - "Identifier" label
   - "Select or enter identifier" hint
   - "Identifier required" error message
   - Group count format (e.g., "Strawberry (3 items)")

2. Translate to all supported languages (en, es, ca)

**Acceptance Criteria**:
- All strings localized
- Consistent terminology

---

#### Task 5.3: Update Tests
**Files**: Test files for mappers, ViewModels, UI

**Changes**:
1. Update mapper tests for grouping logic
2. Update ViewModel tests for identifier fetching
3. Update UI tests / screenshot tests for grouped layout

**Acceptance Criteria**:
- All tests pass
- Coverage maintained

---

### Phase 6: Dashboard (No Changes Required)

**Status**: ✅ No changes needed

**Rationale**:
- Dashboard shows summary statistics (counts by status)
- Calendar shows dates with expiring items
- When clicking a date/status, navigates to `ProductsListScreen` which will show the grouped view
- Existing logic remains unchanged

---

## Implementation Order

1. ✅ **Phase 1** (Database) - Foundation for new field
2. ✅ **Phase 2** (UI Models) - Data structures for grouping
3. ✅ **Phase 3** (UI Components) - Visual representation
4. ✅ **Phase 4** (Instance Creation) - User input for identifiers
5. ✅ **Phase 5** (Integration) - Wire everything together
6. ✅ **Phase 6** (Dashboard) - Verify no changes needed

## Testing Strategy

### Unit Tests
- Mapper grouping logic
- Sorting comparators (groups and instances)
- Identifier fetching queries

### UI Tests
- Grouped instance display
- Identifier dropdown filtering
- Required field validation

### Integration Tests
- End-to-end: Add product → Add instances with identifiers → View grouped list
- Migration: Ensure existing data migrates correctly

### Manual Testing
- Create product with multiple identifier groups
- Verify sorting by status urgency
- Test identifier dropdown autocomplete
- Verify dashboard navigation to grouped list

## Migration & Backwards Compatibility

### Data Migration
- **SKU column**: Added as nullable, defaults to null
- **Identifier**: Already non-nullable, no changes
- **No data loss**: Existing identifiers preserved

### User Impact
- **Visual change**: Instances now grouped by identifier
- **Input change**: Identifier dropdown with autocomplete
- **No breaking changes**: All existing features continue to work

## Future Enhancements (Out of Scope)

1. **Barcode scanning**: Use new `sku` field with `BarcodeHelper`
2. **Bulk identifier editing**: Change identifier for multiple instances
3. **Identifier templates**: Suggest common identifiers (e.g., "Regular", "Low-fat")
4. **Collapse/expand groups**: Accordion-style UI for large groups

## Dependencies

- Material 3 Compose: `ExposedDropdownMenuBox`
- Room: Migration support
- kotlinx.collections.immutable: Grouping with `ImmutableList`

## Estimated Complexity

- **Database (Phase 1)**: Low - Simple schema addition
- **UI Models (Phase 2)**: Medium - Grouping logic
- **UI Components (Phase 3)**: Medium - New layout structure
- **Instance Creation (Phase 4)**: Medium - Dropdown implementation
- **Integration (Phase 5)**: Low - Wiring existing pieces
- **Dashboard (Phase 6)**: None - No changes

**Total**: Medium complexity, ~8-12 focused work sessions

## Risk Assessment

### Low Risk
- Database migration (simple column addition)
- Dashboard (no changes)

### Medium Risk
- Grouping logic (needs thorough testing)
- UI layout changes (may need visual refinement)
- Identifier dropdown UX (needs to feel natural)

### Mitigation
- Write comprehensive unit tests for grouping
- Create Compose previews for all UI states
- User testing for dropdown UX flow
