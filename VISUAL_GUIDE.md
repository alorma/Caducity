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

### After (Today always stands out)
```
┌─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│  MON    │  TUE    │  WED    │  THU    │  FRI    │  SAT    │  SUN    │
│         │         │         │         │         │         │         │
│         │ ◆ 15 ◆  │  ◆ 16 ◆ │ ★ 17 ★  │         │ ◆ 19 ◆  │         │
│         │   TUE   │   WED   │   THU   │         │   SAT   │         │
│         │         │ (TODAY) │ (TODAY) │         │         │         │
└─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘
         Section    PRIMARY     PRIMARY    (no       Section   (no
         Color      Container    COLOR     marker)    Color    marker)
       Cookie4Sided Cookie4Sided Cookie6Sided       Cookie4Sided
                    (no item)    (has item)
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
  - Used when today has expiring products
  - Consistent with Material Design primary color role
  - Stands out across all sections
  
- **Primary Container Color**: Lighter/tinted version of primary color
  - Used when today has NO expiring products
  - Provides subtle indication that it's today
  - Less prominent than primary color

## When Highlighting Occurs

✅ **Day IS highlighted with PRIMARY + 6-sided shape when:**
- The day is today (current date)
- AND the day has product instances expiring

✅ **Day IS highlighted with PRIMARY CONTAINER + 4-sided shape when:**
- The day is today (current date)
- BUT no products expire that day

✅ **Day shows section color + 4-sided shape when:**
- The day is NOT today
- AND the day has product instances expiring

❌ **Day has NO styling when:**
- The day is NOT today
- AND no products expire that day

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
  ◆15◆            ◆ 17 ◆     ◆ 18 ◆   ◆19◆
Section          PRIMARY    Section  Section
Color            Container   Color    Color
                 (today,    (tmrw)
                 no item)
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
  isToday && hasItem  -> Primary Color          // Highest priority - today with items
  isToday && !hasItem -> Primary Container      // High priority - today without items
  hasItem             -> Section Color          // Medium priority - other days with items
  else                -> No color               // No styling
}

// Shape priority
when {
  isToday && hasItem  -> Cookie6Sided          // Highest priority - today with items
  isToday && !hasItem -> Cookie4Sided          // High priority - today without items
  hasItem             -> Cookie4Sided          // Medium priority - other days with items
  else                -> RectangleShape        // No shape
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

## Calendar Initial Week Behavior

The calendar intelligently determines which week to show first:

### Logic
```
Has items expiring on/before today?
├─ YES → Show today's week
└─ NO  → Show earliest item's week
```

### Example 1: Expired Milk
```
Today: May 17
Items: May 10 ❌, May 12 ❌
Initial view: Week of May 17 (today's week)
Reason: Items are expired, user needs to see them now
```

### Example 2: Fresh Cheese
```
Today: May 17
Items: May 25 ✓, May 30 ✓, June 5 ✓
Initial view: Week of May 25 (earliest item)
Reason: All items are future, show next expiration
```

### Example 3: Bread Expiring Today
```
Today: May 17
Items: May 17 ⚠️, May 20 ✓
Initial view: Week of May 17 (today's week)
Reason: Item expires today, immediate attention needed
```

## Testing Tips

When testing this feature:
1. **Calendar initial position**:
   - Create product with past items → verify opens on today's week
   - Create product with only future items → verify opens on earliest item's week
   - Create product with item expiring today → verify opens on today's week
2. **Today highlighting**:
   - Set system date to match a product's expiration date
   - Verify the calendar day shows primary color + 6-sided shape
   - Verify today without items shows primary container + 4-sided shape
3. **Other days**:
   - Verify other days with items show section color + 4-sided shape
   - Verify empty days have no styling
4. **Cross-platform**:
   - Test in both light and dark themes
   - Test across different product sections (Expired, Expiring Soon, Fresh)
