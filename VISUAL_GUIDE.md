# Visual Guide: Today's Date Highlighting in Week Calendar

## What Changed

The week calendar in product items now visually highlights "today" when products expire on that date.

## Visual Comparison

### Before (All days look the same when they have items)
```
┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│  MON    │  TUE    │  WED    │  THU    │  FRI    │  SAT    │  SUN    │
│         │         │         │         │         │         │         │
│         │ ◆ 15 ◆  │         │ ◆ 17 ◆  │         │ ◆ 19 ◆  │         │
│         │   TUE   │         │   THU   │         │   SAT   │         │
│         │         │         │         │         │         │         │
└─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘
         Section     (no        Section   (no        Section   (no
         Color       marker)     Color    marker)     Color    marker)
       Cookie4Sided  for Today  Cookie4Sided        Cookie4Sided
```

### After (Today stands out when it has items)
```
┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│  MON    │  TUE    │  WED    │  THU    │  FRI    │  SAT    │  SUN    │
│         │         │         │         │         │         │         │
│         │ ◆ 15 ◆  │         │ ★ 17 ★  │         │ ◆ 19 ◆  │         │
│         │   TUE   │         │   THU   │         │   SAT   │         │
│         │         │         │ (TODAY) │         │         │         │
└─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘
         Section     (no       PRIMARY    (no       Section   (no
         Color       marker)    COLOR     marker)    Color    marker)
       Cookie4Sided            Cookie6Sided        Cookie4Sided
```

## Visual Elements

### Shape Differences
- **Cookie4Sided (◆)**: Regular diamond-like shape with 4 rounded vertices
  - Used for: Regular days with expiring products
  
- **Cookie6Sided (★)**: Hexagonal/star-like shape with 6 rounded vertices
  - Used for: Today when it has expiring products
  - More complex shape → immediately noticeable

### Color Differences
- **Section Colors**: Different colors per section
  - Red-ish: Expired items
  - Yellow-ish: Expiring Soon items
  - Green-ish: Fresh items
  - Gray-ish: Empty items
  
- **Primary Color**: App's main accent color (usually vibrant blue/purple)
  - Overrides section color when day is today
  - Consistent with Material Design primary color role
  - Stands out across all sections

## When Highlighting Occurs

✅ **Day IS highlighted (primary color + 6-sided shape) when:**
- The day is today (current date)
- AND the day has product instances expiring

❌ **Day is NOT highlighted when:**
- The day is today BUT no products expire that day
- The day is not today (uses section color + 4-sided shape instead)
- The day has no expiring products (no shape or color)

## Example Scenarios

### Scenario 1: Milk expiring today (Thursday)
```
Product: Milk
Section: EXPIRED (assuming today is past expiration)

Week View:
  TUE      WED      THU       FRI      SAT
  ◆15◆            ★ 17 ★              ◆19◆
Section         PRIMARY            Section
Color           COLOR              Color
                ↑
              TODAY!
```

### Scenario 2: Bread expiring tomorrow (Today is Thursday)
```
Product: Bread
Section: EXPIRING_SOON

Week View:
  TUE      WED      THU       FRI      SAT
  ◆15◆            (empty)    ◆ 18 ◆   ◆19◆
Section           no         Section  Section
Color             expiry     Color    Color
                  today      (tmrw)
```

### Scenario 3: Multiple instances, one today
```
Product: Cheese
Section: FRESH

Week View:
  MON      TUE      WED       THU      FRI
  ◆12◆    ◆ 13 ◆   ★ 14 ★    ◆ 15 ◆   ◆16◆
Section  Section  PRIMARY   Section  Section
Color    Color    COLOR     Color    Color
                    ↑
                  TODAY with instance!
```

## Implementation Details

### Logic Flow
1. For each day in the week calendar:
   - Check if `weekDay.date == product.today` → `isToday`
   - Check if day has product instance → `hasItem`
   - Apply styling based on both flags:

```kotlin
// Color priority
when {
  isToday && hasItem -> Primary Color       // Highest priority
  hasItem            -> Section Color       // Medium priority  
  else               -> No color            // No styling
}

// Shape priority
when {
  isToday && hasItem -> Cookie6Sided       // Highest priority
  hasItem            -> Cookie4Sided       // Medium priority
  else               -> RectangleShape     // No shape
}
```

## Accessibility Considerations

1. **Dual Indicators**: Using both color AND shape ensures:
   - Color-blind users can distinguish today by shape
   - Users who can see color get immediate visual feedback
   
2. **Primary Color**: 
   - Material 3 primary color has sufficient contrast
   - Works across light and dark themes
   
3. **Shape Complexity**:
   - Cookie6Sided is noticeably more complex than Cookie4Sided
   - Provides visual weight/emphasis to today

## Testing Tips

When testing this feature:
1. Set system date to match a product's expiration date
2. Verify the calendar day shows primary color
3. Verify the calendar day shows 6-sided shape
4. Verify other days with items show section color + 4-sided shape
5. Test in both light and dark themes
6. Test across different product sections (Expired, Expiring Soon, Fresh)
