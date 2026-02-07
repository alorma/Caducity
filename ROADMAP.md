# Caducity Feature Roadmap

## Context

Caducity is a minimalist Android grocery expiration tracker built with Jetpack Compose. The core functionality is working:
- Users can create categories, products, and items
- Items track expiration dates with status indicators (Fresh, Expiring Soon, Expired, Frozen)
- Dashboard shows summary statistics and calendar visualization
- Category detail screens with product tabs and item management
- Settings for theme, notifications, and backup/restore

**Current gaps identified:**
- Item actions bottom sheet needs conditional logic (freeze vs unfreeze)
- Delete product feature is validated but not implemented in UI
- Dashboard status/date navigation handlers are empty stubs
- Language settings UI missing (though multi-language support exists)

**Goal:** Plan simple, minimalist feature additions that enhance UX without adding complexity.

---

## Feature Ideas (Organized by Priority)

### 🎯 Phase 1: Complete Core Features ✅ *Completed*

**1. Fix Item Actions Bottom Sheet** ✅ *Completed*
- **Problem**: Bottom sheet always shows same actions regardless of item state
- **Solution**: Conditional action display based on item status
  - Fresh/ExpiringSoon items: Show "Freeze" + "Consume" + "Delete"
  - Frozen items: Show "Unfreeze" + "Consume" + "Delete"
  - Expired items: Show "Consume" (with warning) + "Delete" only
- **Implementation**: Update `CategoryDetailViewModel` and side effect handler
- **Files**: `CategoryDetailViewModel.kt`, `CategoryDetailScreen.kt`

**2. Delete Product Feature** ✅ *Completed*
- **Problem**: Products cannot be deleted via UI (though validation exists)
- **Solution**: Add delete action to product tabs
  - Long-press or swipe on product tab to show delete option
  - Or add delete button to tab bar when product is selected
  - Show error snackbar if product has active items
- **Implementation**: Add delete product use case call, update UI
- **Files**: `CategoryDetailViewModel.kt`, `CategoryDetailScreen.kt`
- **Status**: Implemented with swipe-to-delete on product tabs, validation, and error feedback

**3. Dashboard Status/Date Navigation** ✅ *Completed*
- **Problem**: Clicking status cards or calendar dates does nothing (empty handlers)
- **Solution**: Create filtered view showing matching items
  - Status filter: Show all items with selected status across categories
  - Date filter: Show all items expiring on selected date
  - New screen or modal with filtered list
- **Implementation**: Create new route/screen, implement navigation handlers
- **Files**: `App.kt`, new filtered view screen, `DashboardViewModel.kt`
- **Status**: Implemented `FilteredItemsByStatusScreen` with:
  - Status-based filtering across all categories
  - Product grouping with clickable items
  - Item actions (consume/freeze/delete) via reusable bottom sheet
  - Category header navigation to detail screen
  - Success/error feedback with snackbars

---

### 🌟 Phase 2: Essential UX Improvements

**4. Edit Item**
- **Feature**: Edit existing item's identifier text or expiration date
- **Why**: Users make mistakes, dates change (e.g., item was frozen then thawed)
- **Implementation**:
  - Add "Edit" action to item bottom sheet
  - Reuse `CategoryDetailAddItemScreen` form in edit mode
  - Update item via new `UpdateItemUseCase`
- **Complexity**: Low - reuses existing form components

**5. Search/Filter on Dashboard**
- **Feature**: Search bar to filter categories or items by name
- **Why**: Helps users with many categories find items quickly
- **Implementation**:
  - Add search TextField to dashboard top bar
  - Filter categories/items by text query
  - Maybe add status filter chips
- **Complexity**: Medium - requires filtering logic in ViewModel

**5. Bulk Item Operations**
- **Feature**: Select multiple items for batch consume/freeze/delete
- **Why**: Common use case after grocery shopping or fridge cleaning
- **Implementation**:
  - Add selection mode toggle (checkbox icon in top bar)
  - Show checkboxes on items when in selection mode
  - Show action bar with bulk actions
  - Apply operations to all selected items
- **Complexity**: Medium - requires selection state management

**7. Notification Configuration**
- **Feature**: Configure when notifications appear and threshold
- **Why**: Users want control over notification timing
- **Implementation**:
  - Add settings for:
    - Notification time (morning/evening)
    - Days before expiration threshold (currently hardcoded)
    - Notification frequency (daily/weekly)
  - Update `ExpirationWorkScheduler` to use preferences
  - Use existing `ExpirationThresholds` infrastructure
- **Complexity**: Medium - requires WorkManager configuration changes

---

### 💡 Phase 3: Nice-to-Have Enhancements

**8. Shopping List Mode**
- **Feature**: Mark consumed items as "need to buy again"
- **Why**: Natural workflow - track what you consumed, remember to buy more
- **Implementation**:
  - Add "Add to shopping list" action when consuming item
  - New shopping list screen with simple checklist
  - Check off items when purchased (auto-create new item?)
- **Complexity**: High - new domain model and screen

**9. Item History/Statistics**
- **Feature**: Track consumption patterns over time
- **Why**: Help users predict consumption rates ("you use 2L milk per week")
- **Implementation**:
  - Store consumed items in history table
  - Calculate average consumption rate per product
  - Show statistics in category/product detail
  - "Suggested restock date" based on consumption pattern
- **Complexity**: High - new domain model, calculations, UI

**10. Calendar View Mode**
- **Feature**: Alternative dashboard view with full month calendar
- **Why**: Some users prefer calendar-first view to see all expiring items
- **Implementation**:
  - Add view mode toggle to dashboard (list vs calendar)
  - Show all items from all categories on single calendar
  - Tap date to see items expiring that day
  - Reuse existing `AppCalendar` component
- **Complexity**: Medium - requires aggregated data view

**11. Quick Add Widget**
- **Feature**: Android home screen widget for quick item entry
- **Why**: Reduce friction - add item without opening app
- **Implementation**:
  - Create Glance widget (Jetpack Compose for widgets)
  - Show expiring items count or quick add button
  - Deep link to add item screen or category detail
- **Complexity**: High - requires widget development

**12. Photo Attachments**
- **Feature**: Attach photo to item (receipt, product image)
- **Why**: Visual identification, remember what item looks like
- **Implementation**:
  - Add optional image field to Item entity
  - Image picker integration
  - Show thumbnail in item card
  - Store images in app storage or use URI
- **Complexity**: High - requires image handling, storage management

---

## Recommended Implementation Order

### Sprint 1: Complete Core Features ✅ *COMPLETED*
1. ✅ Fix item actions bottom sheet (freeze/unfreeze logic)
2. ✅ Add delete product feature
3. ✅ Implement dashboard status/date filtering

**Why start here:** These complete existing flows and are the most straightforward. They fix incomplete functionality rather than adding new complexity.

### Sprint 2: Essential UX
4. Edit item functionality
5. Language settings UI
6. Search on dashboard

**Why next:** These are simple quality-of-life improvements that users will immediately appreciate. They fill obvious gaps without architectural changes.

### Sprint 3: Power User Features
7. Bulk operations
8. Notification configuration
9. Calendar view mode

**Why later:** These are valuable for power users but not essential for basic functionality. They require more complex state management.

### Future Consideration (Post-MVP)
8-12. Shopping list, statistics, calendar view, widgets, photos

**Why defer:** These are substantial features that change the app's scope. They should be considered based on user feedback after core features are solid.

---

## Design Principles to Maintain

- **Minimalist UI**: Avoid cluttered screens, use bottom sheets and modals
- **Material 3**: Follow Material Design guidelines, use Expressive API
- **Adaptive**: Support different screen sizes (compact vs expanded)
- **Connected Groups**: Use ShapePosition pattern for visual cohesion
- **Side Effects**: All dialogs/snackbars/bottom sheets via side effects, never UI state
- **Clean Architecture**: Maintain domain/data/ui separation
- **Type Safety**: Use sealed classes for navigation and state

---

## Questions to Consider

1. **Search scope**: Should search include item identifiers or just category/product names?
2. **Bulk operations**: Which actions make sense in bulk? (Consume probably not, but freeze/delete yes)
3. **Calendar view**: Should it replace per-category view or be an additional mode?
4. **Notification timing**: What's a good default? Morning (8am)? Evening (6pm)?
5. **Shopping list**: Should this be a separate feature or integrated into existing flows?
6. **Statistics**: How far back should history go? Keep forever or archive old items?

---

## Next Steps

1. **Review this roadmap** - Agree on priorities and scope
2. **Plan Sprint 1 implementation** - Start with item bottom sheet fix
3. **Iterate based on feedback** - Adjust priorities as needed

---

*Last updated: 2026-02-07*
