# Issue #60 Refinement - Quick Reference

## TL;DR

**Decision by @alorma**: Implement **Interpretation 3 - Variants as Separate Entity**

Variants will be a different entity that can be created independently. When creating instances, it will propose a list of variants or allow creating a new one. **Instances can also be created without variants using standalone identifiers.**

## What You Have Now

Three comprehensive documents have been created:

### 1. ISSUE_60_VARIANT_ENTITY_PLAN.md ⭐ **CURRENT PLAN**
**Detailed implementation plan for Variant entity approach**
- Complete architecture overview
- Database schema with migration strategy
- New domain models (Variant, ProductWithVariants)
- Data access layer (VariantDao, VariantDataSource)
- Use cases for variant management
- UI components (VariantsScreen, CreateVariantDialog)
- 2-3 week implementation timeline
- Phase-by-phase breakdown

### 2. ISSUE_60_REFINEMENT.md
**Full analysis and refinement document**
- User story and technical context
- Current implementation analysis  
- Three possible interpretations of the issue
- Detailed implementation guidance with code samples
- Acceptance criteria

### 3. ISSUE_60_IMPLEMENTATION_PLAN.md
**Alternative approach (dropdown enhancement)**
- Lighter-weight solution (not chosen)
- Kept for reference

## Decision Made: Variant as Separate Entity ✅

**Chosen Approach**: Option 3 - New Variant Entity

### What This Means

**Architecture**:
```
Product (1) -----> (*) Variant (1) -----> (*) ProductInstance
           \___________________________/
              (standalone instances)
```

**Example from @alorma**:
- Product: "Drinks"
  - Variant: "Fanta" (3 instances)
  - Variant: "Coke" (4 instances)
  - Variant: "Beer" (0 instances - variant exists independently)
  - **Standalone**: "Wine" (1 instance - no variant, just identifier)

### Key Features

1. **Independent Variant Creation**: Create variants before any instances exist
2. **Variant Management UI**: Dedicated screen to manage variants
3. **Smart Instance Creation**: Select from existing variants, create new variants, OR use standalone identifiers
4. **Standalone Instances**: Instances can exist without variants (e.g., "Wine" in Drinks product)
5. **Backward Compatible**: Existing instances migrate to auto-created variants
6. **Proper Data Model**: Variants as first-class entities in database

### Implementation Effort

- **Timeline**: 2-3 weeks
- **Complexity**: High (database migration, new entities, UI screens)
- **Risk**: Medium (requires careful migration testing)
- **Value**: High (proper architecture for long-term growth)

## Next Steps

### For Developer

Follow the detailed plan in `ISSUE_60_VARIANT_ENTITY_PLAN.md`:

**Phase 1: Database & Domain (Week 1)**
- Create VariantRoomEntity and VariantDao
- Write database migration
- Update ProductInstanceRoomEntity with variantId
- Create Variant domain model
- Implement VariantDataSource

**Phase 2: Use Cases (Week 1-2)**
- Create variant management use cases
- Update AddInstanceToProductUseCase
- Update grouping logic

**Phase 3: UI Components (Week 2)**
- Create VariantsScreen
- Create CreateVariantDialog
- Update CreateInstanceBottomSheet

**Phase 4-5: Testing & Polish (Week 2-3)**
- Integration testing
- Migration testing
- Localization
- QA and refinement

See `ISSUE_60_VARIANT_ENTITY_PLAN.md` for complete details.

## Code Location Reference

**Current grouping implementation**:
- `ObtainDashboardProductsUseCase.kt` - Grouping logic
- `ProductInstanceGroup.kt` - Domain model
- `ProductsListItem.kt` - UI display
- `CreateInstanceBottomSheet.kt` - Instance creation form

**Where changes would go** (Option 1):
- `ProductDao.kt` - Add identifier query
- `ProductDataSource.kt` - Add interface method
- `RoomProductDataSource.kt` - Implement query
- `IdentifierSelectionField.kt` - New dropdown component (create this)
- `CreateInstanceBottomSheet.kt` - Use new dropdown

## Visual Mockup

### Current UI (Identifier Field)
```
┌─────────────────────────────┐
│ Identifier                  │
│ [___________________]       │ ← Simple text field
└─────────────────────────────┘
```

### New UI (Variant or Standalone Selection)
```
┌─────────────────────────────────┐
│ [Use Variant] [Standalone]     │ ← Mode toggle
│    ^^^^^^^^                     │
│                                 │
│ Variant                         │
│ [Select variant... ▼]          │ ← Dropdown with variants
│  ┌────────────────────────────┐│
│  │ Fanta (3 items)            ││ ← Existing variants
│  │ Coke (4 items)             ││
│  │ Beer (0 items)             ││
│  │ ─────────────────────      ││
│  │ + Create new variant       ││ ← Opens dialog
│  └────────────────────────────┘│
└─────────────────────────────────┘

OR (when Standalone mode selected):

┌─────────────────────────────────┐
│ [Use Variant] [Standalone]     │ ← Mode toggle
│                ^^^^^^^^^^       │
│                                 │
│ Identifier                      │
│ [e.g., Wine...            ]    │ ← Free text input
│ Won't be grouped with variants  │
└─────────────────────────────────┘
```

### Variant Management Screen (NEW)
```
┌─────────────────────────────────────┐
│ Variants for "Drinks"               │
│                                     │
│ ┌─────────────────────────────────┐│
│ │ Fanta               3 items  [×]││
│ │ Coke                4 items  [×]││
│ │ Beer                0 items  [×]││
│ └─────────────────────────────────┘│
│                                     │
│ Standalone instances:               │
│ — Wine (1 item)                     │
│                                     │
│ [+ Add Variant]                     │
└─────────────────────────────────────┘
```

## How This Helps Users

**Before**: 
- User types "strawberry", "Strawberry", "STRAWBERRY" → Creates 3 separate groups
- No way to manage variants
- Variants tied to instances (can't pre-create)
- No flexibility for one-off items

**After (Variant Entity with Standalone Support)**:
- User creates "Strawberry" variant once for organized items
- Can also add standalone items like "Wine" without creating a variant
- Variant persists even with zero instances
- Clear variant management interface
- Select from existing variants or use standalone mode
- Proper data organization with flexibility
- Can delete unused variants

## Questions?

Refer to the detailed documents:
- **Implementation details?** → `ISSUE_60_VARIANT_ENTITY_PLAN.md` (PRIMARY)
- Questions about the analysis? → `ISSUE_60_REFINEMENT.md`
- Questions about current code? → See "Code Location Reference" above

---

**Created**: 2026-01-05  
**Updated**: 2026-01-05  
**Status**: Decision made - Implementing Variant as separate entity  
**Decision by**: @alorma
