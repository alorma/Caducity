# Issue #60 Refinement: Introduce Product Variants

## User Story

**As a** Caducity app user  
**I want** to organize my product instances into variants (sub-groups)  
**So that** I can better track different types of the same product (e.g., "Fruits" → "Apples", "Bananas")

## Current State Analysis

### What Already Exists

The Caducity codebase **already implements variant/grouping functionality** using the `identifier` field:

1. **Data Model** (`ProductInstance.kt`):
   - Each `ProductInstance` has an `identifier: String` field
   - Instances are grouped by this identifier throughout the system

2. **Domain Layer** (`ProductInstanceGroup.kt`):
   ```kotlin
   data class ProductInstanceGroup(
     val identifier: String,
     val instances: ImmutableList<ProductInstance>,
   )
   ```

3. **Use Cases** (grouping logic):
   - `ObtainDashboardProductsUseCase`: Groups instances by `identifier`
   - `ObtainProductDetailUseCase`: Groups instances by `identifier`
   - Both sort groups by urgency (most urgent status first)

4. **UI Layer** (`ProductsListItem.kt`):
   - Displays groups visually with identifier header
   - Shows format: `"— Strawberry (3)"` with count
   - Status bars for each group
   - Frozen count display per group

5. **Database** (`ProductInstanceRoomEntity.kt`):
   - `identifier` column is non-nullable string
   - Already persisted and queryable

### Current User Flow

1. User creates product (e.g., "Yogurt")
2. User adds instance with identifier (e.g., "Strawberry")
3. User adds more instances with same/different identifiers ("Strawberry", "Greek", "Vanilla")
4. UI groups and displays: "Yogurt" → "Strawberry (2)", "Greek (1)", "Vanilla (1)"
5. Each group shows status bars and frozen counts

## Issue Interpretation

Given that the functionality described in issue #60 already exists, there are **three possible interpretations**:

### Interpretation 1: Terminology/Documentation Clarification
- **Goal**: Rename or clarify that `identifier` represents "variant"
- **Scope**: Documentation, UI labels, code comments
- **Impact**: Low - mainly cosmetic/clarity improvements

### Interpretation 2: Enhanced Variant Management
- **Goal**: Improve variant creation/selection UX
- **Examples**:
  - Dropdown with existing identifiers when adding instances (from INSTANCE_GROUPING_PLAN.md Task 4.1)
  - Autocomplete for common variants
  - Better variant visualization
- **Impact**: Medium - UI/UX improvements

### Interpretation 3: Separate Variant Entity
- **Goal**: Create a distinct "Variant" entity separate from instances
- **Architecture**:
  - Product → Variant → Instances hierarchy
  - Variants as first-class objects with metadata
  - Explicit variant management screens
- **Impact**: High - significant architectural change

## Recommended Path: Interpretation 2 (Enhanced Variant Management)

Based on the INSTANCE_GROUPING_PLAN.md file (Phase 4), I recommend **enhancing the existing identifier/variant system** with better UX.

### Technical Context

**Affected Components**:
- UI: `CreateInstanceBottomSheet.kt` (add dropdown/autocomplete)
- Domain: `ProductDataSource.kt` (add method to fetch distinct identifiers)
- Data: `ProductDao.kt` (add query for identifiers)
- Strings: All `values*/strings.xml` files (terminology updates)

**Architecture Pattern**: Clean Architecture (MVI/MVVM)
- Data layer: Room query for distinct identifiers
- Domain layer: Interface method in ProductDataSource
- UI layer: ExposedDropdownMenuBox for identifier selection

**Dependencies**:
- Existing: ProductInstance model, ProductDataSource
- New: None (uses existing Material 3 ExposedDropdownMenuBox)

**Integration Points**:
- `CreateInstanceBottomSheet`: Replace simple TextField with dropdown/autocomplete
- `ProductDao`: Add query for distinct identifiers per product
- ViewModels: Provide identifier list to UI

## Acceptance Criteria

### Must Have
- [ ] User can see existing identifiers (variants) when adding a new instance
- [ ] Dropdown shows identifiers filtered as user types
- [ ] User can still enter a new identifier not in the list
- [ ] Empty identifier validation works correctly
- [ ] Terminology is consistent (decide: "Identifier" vs "Variant" in UI)

### Should Have
- [ ] Identifiers sorted alphabetically in dropdown
- [ ] Identifier selection feels smooth and intuitive
- [ ] String resources localized (en, es, ca)
- [ ] Existing UI grouping visualization remains unchanged

### Nice to Have
- [ ] Common variant suggestions (e.g., "Regular", "Low-fat", "Organic")
- [ ] Recently used identifiers shown first
- [ ] Identifier usage count in dropdown (e.g., "Strawberry (5 instances)")

## Implementation Guidance

### Phase 1: Data Layer

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/room/ProductDao.kt`

Add query to fetch distinct identifiers for a product:

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

**File**: `app/src/main/kotlin/com/alorma/caducity/domain/ProductDataSource.kt`

Add interface method:

```kotlin
suspend fun getProductIdentifiers(productId: String): List<String>
```

**File**: `app/src/main/kotlin/com/alorma/caducity/data/datasource/RoomProductDataSource.kt`

Implement interface method:

```kotlin
override suspend fun getProductIdentifiers(productId: String): List<String> {
  return productDao.getProductIdentifiers(productId)
}
```

### Phase 2: UI Component

**Option A: ExposedDropdownMenuBox** (Recommended - Material 3 standard)

Create new file: `app/src/main/kotlin/com/alorma/caducity/ui/screen/product/create/IdentifierSelectionField.kt`

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifierSelectionField(
  value: String,
  onValueChange: (String) -> Unit,
  existingIdentifiers: ImmutableList<String>,
  modifier: Modifier = Modifier,
  label: String = stringResource(R.string.identifier_label),
  placeholder: String = stringResource(R.string.identifier_placeholder),
) {
  var expanded by remember { mutableStateOf(false) }
  
  // Filter identifiers based on current input
  val filteredIdentifiers = remember(value, existingIdentifiers) {
    if (value.isBlank()) {
      existingIdentifiers
    } else {
      existingIdentifiers.filter { 
        it.contains(value, ignoreCase = true) 
      }.toImmutableList()
    }
  }
  
  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = modifier
  ) {
    TextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      modifier = Modifier
        .fillMaxWidth()
        .menuAnchor(),
      singleLine = true,
    )
    
    if (filteredIdentifiers.isNotEmpty()) {
      ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
      ) {
        filteredIdentifiers.forEach { identifier ->
          DropdownMenuItem(
            text = { Text(identifier) },
            onClick = {
              onValueChange(identifier)
              expanded = false
            }
          )
        }
      }
    }
  }
}
```

**Option B: Simple TextField with Autocomplete** (Lighter weight)

Add autocomplete suggestions below the TextField in CreateInstanceBottomSheet.

### Phase 3: ViewModel Integration

**File**: Modify ViewModel for product detail/create screens

Add to state:
```kotlin
data class ProductDetailState(
  // ... existing fields
  val availableIdentifiers: ImmutableList<String> = persistentListOf(),
)
```

Load identifiers when product is available:
```kotlin
viewModelScope.launch {
  val identifiers = productDataSource.getProductIdentifiers(productId)
  _state.update { it.copy(availableIdentifiers = identifiers.toImmutableList()) }
}
```

### Phase 4: Update CreateInstanceBottomSheet

**File**: `app/src/main/kotlin/com/alorma/caducity/ui/screen/product/create/CreateInstanceBottomSheet.kt`

Add parameter:
```kotlin
@Composable
fun CreateInstanceBottomSheet(
  // ... existing parameters
  availableIdentifiers: ImmutableList<String> = persistentListOf(),
) {
```

Replace TextField at lines 102-134 with:
```kotlin
IdentifierSelectionField(
  value = identifier,
  onValueChange = { identifier = it },
  existingIdentifiers = availableIdentifiers,
  modifier = Modifier.fillMaxWidth()
)
```

### Phase 5: String Resources

**Files**: `app/src/main/res/values*/strings.xml`

Add/update strings (decide on terminology):

```xml
<!-- Option A: Keep "Identifier" -->
<string name="identifier_label">Identifier</string>
<string name="identifier_placeholder">Select or enter identifier</string>
<string name="identifier_required">Identifier is required</string>

<!-- Option B: Change to "Variant" -->
<string name="variant_label">Variant</string>
<string name="variant_placeholder">Select or enter variant</string>
<string name="variant_required">Variant is required</string>
```

Translate to Spanish (es) and Catalan (ca).

## Technical Considerations

### Performance
- **Identifier query**: Simple SELECT DISTINCT - very fast
- **Filtering**: In-memory on small lists (typically < 20 items)
- **No impact**: Existing grouping logic unchanged

### Security
- **No concerns**: Identifiers are user-generated strings
- **Validation**: Already has non-blank validation

### Backwards Compatibility
- **100% compatible**: Uses existing `identifier` field
- **No migration**: No schema changes needed
- **Safe**: Additive changes only (dropdown is enhancement of TextField)

### Error Handling
- **Empty identifier**: Existing validation prevents empty submissions
- **Query failure**: Return empty list, allow free text entry
- **No identifiers exist**: Dropdown shows nothing, TextField works normally

### Testing Strategy
- **Unit tests**: Identifier query in DAO
- **ViewModel tests**: Identifier loading logic
- **UI tests**: Dropdown interaction, filtering logic
- **Screenshot tests**: Dropdown expanded/collapsed states

## Out of Scope

This refinement explicitly **excludes**:

1. **Separate Variant Entity**: No new database table or domain model
2. **Variant Metadata**: No additional properties (color, icon, etc.)
3. **Variant Management Screen**: No dedicated CRUD UI for variants
4. **Barcode Integration**: No automatic variant detection from SKU/barcode
5. **Variant Templates**: No predefined variant suggestions
6. **Bulk Operations**: No batch variant updates
7. **Variant Analytics**: No per-variant statistics
8. **Migration**: No changes to existing data

These could be **future enhancements** but are not part of this issue.

## Alternative Approaches Considered

### Alternative 1: Separate Variant Table
**Pros**: Explicit variant management, metadata support
**Cons**: High complexity, breaking change, over-engineered for current needs
**Decision**: Not recommended - YAGNI principle

### Alternative 2: Hardcoded Variant List
**Pros**: Consistent naming across users
**Cons**: Inflexible, requires app updates, not suitable for diverse use cases
**Decision**: Not recommended - user flexibility is key

### Alternative 3: AI-Powered Suggestions
**Pros**: Smart suggestions based on product name
**Cons**: Requires ML model/API, complexity, offline support issues
**Decision**: Out of scope - potential future enhancement

## Questions for Clarification

Before implementation, please confirm:

1. **Terminology**: Should UI use "Identifier" or "Variant"?
   - Current code uses "Identifier"
   - Issue #60 uses "Variant"
   - Recommendation: User-facing = "Variant", Code = keep "identifier"

2. **Scope**: Is this implementation plan aligned with your vision?
   - Enhanced dropdown/autocomplete for identifier selection?
   - Or do you want a separate variant entity?

3. **Priority**: Which acceptance criteria are must-have vs nice-to-have?

4. **Design**: Any specific UX preferences for the dropdown?
   - Material 3 ExposedDropdownMenuBox (standard)
   - Or custom autocomplete design?

## Success Metrics

- **Usability**: Users can create instances 30% faster with autocomplete
- **Consistency**: Reduced typos/variations in identifier names
- **Discoverability**: Users understand grouping/variant concept clearly
- **Adoption**: 70%+ of users use dropdown vs free text entry

## Estimated Effort

- **Small (1-2 days)**: If using ExposedDropdownMenuBox + simple integration
- **Medium (3-5 days)**: If adding custom autocomplete + extensive testing
- **Large (1-2 weeks)**: If separate variant entity (not recommended)

**Recommendation**: Start with **Small** scope, iterate based on user feedback.

---

## Summary

Issue #60 requests "product variants" functionality that **already exists conceptually** via the `identifier` field. The recommended refinement focuses on **enhancing the UX** for identifier/variant selection with a dropdown/autocomplete interface, making the existing feature more discoverable and user-friendly.

This approach provides immediate value with minimal risk and aligns with the INSTANCE_GROUPING_PLAN.md guidance while keeping the architecture clean and maintainable.

---

*Refinement created by Claude Code on 2026-01-05*
