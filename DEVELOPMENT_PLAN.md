# Caducity Development Plan

## Project Vision
Caducity is a multiplatform grocery expiration tracker app that helps users reduce food waste by monitoring expiration dates of their groceries across Android, Desktop, Web, and WebAssembly platforms.

---

## Phase 1: Core Product Management ✅ (Partially Complete)

### Current Status
- ✅ Basic app structure with navigation
- ✅ Dashboard screen displaying products
- ✅ Product detail screen showing instances
- ✅ Settings screen with theme configuration
- ✅ Adaptive UI for different screen sizes
- ✅ Material 3 theming with dynamic colors
- ⏳ Create product dialog (UI only, no functionality)

### Remaining Tasks
- [ ] Complete create product functionality
- [ ] Add edit product capability
- [ ] Implement delete product feature
- [ ] Add product search/filter on dashboard

---

## Phase 2: Instance Management

### Goal
Enable users to track individual instances of products (purchases with specific expiration dates).

### Features to Implement

#### 2.1 Add Instance Functionality
- Create "Add Instance" screen/dialog
- Form fields: purchase date, expiration date, quantity
- Validation for dates (expiration must be after purchase)
- Auto-calculate expiration suggestions based on product type

#### 2.2 Instance Actions
- Mark instance as consumed/removed
- Edit instance details (dates, quantity)
- Move instance to freezer (extend expiration)
- Partial consumption (reduce quantity)

#### 2.3 Instance Status Management
- Auto-categorize: Fresh, Expiring Soon, Expired
- Visual indicators with color coding
- Update status in real-time based on current date

---

## Phase 3: Data Persistence

### Goal
Implement persistent storage across platforms.

### Platform-Specific Implementation

#### 3.1 Android
- Implement Room database
- Define database schema and DAOs
- Migrate from FakeDataSource to RoomDataSource
- Handle database migrations

#### 3.2 Desktop & Web
- Evaluate storage options (SQLDelight, local storage)
- Implement chosen solution
- Ensure data consistency across sessions

#### 3.3 Data Synchronization (Future)
- Design sync architecture
- Cloud storage integration (optional)
- Conflict resolution strategy

---

## Phase 4: Notifications & Reminders

### Goal
Proactively alert users about expiring products.

### Features to Implement

#### 4.1 Local Notifications
- Daily summary of expiring items
- Customizable notification time
- Per-product reminder settings

#### 4.2 Notification Settings
- Enable/disable notifications
- Configure threshold (e.g., 3 days before expiration)
- Quiet hours/Do not disturb

#### 4.3 Platform Integration
- Android: WorkManager for background checks
- Desktop: Platform-specific notification APIs
- Web: Browser notifications (with permission)

---

## Phase 5: Enhanced User Experience

### Goal
Improve usability and add quality-of-life features.

### Features to Implement

#### 5.1 Product Templates & Categories
- Pre-defined product templates (milk, eggs, bread, etc.)
- Product categories (dairy, produce, meat, etc.)
- Default expiration periods by category
- Custom categories

#### 5.2 Smart Features
- Barcode scanner (mobile only)
- Product name autocomplete
- Learn from user patterns (frequently added items)
- Expiration prediction based on purchase history

#### 5.3 Dashboard Enhancements
- Statistics widget (items expiring this week, total saved)
- Quick actions (mark as consumed, add instance)
- Multiple view modes (grid, list, timeline)
- Sort and filter options

#### 5.4 Search & Organization
- Full-text search across products
- Filter by category, status, expiration date
- Sort by name, expiration, purchase date
- Saved filter presets

---

## Phase 6: Analytics & Insights

### Goal
Help users understand their consumption patterns and reduce waste.

### Features to Implement

#### 6.1 Statistics Dashboard
- Items saved from waste
- Most/least frequently consumed products
- Money saved estimation
- Waste reduction trends over time

#### 6.2 Reports & Visualizations
- Weekly/monthly consumption reports
- Category breakdown charts
- Expiration timeline view
- Export data (CSV, PDF)

#### 6.3 Recommendations
- Shopping list suggestions based on consumption
- Warning for frequently wasted items
- Optimal purchase quantity suggestions

---

## Phase 7: Sharing & Collaboration

### Goal
Enable household/family sharing of inventory.

### Features to Implement

#### 7.1 Multi-User Support
- Household/family groups
- Shared product inventory
- Activity log (who added/removed what)
- User roles (admin, member)

#### 7.2 Shopping Lists
- Collaborative shopping lists
- Add-to-list from consumed items
- Share lists with household members
- Check off items while shopping

---

## Phase 8: Advanced Features

### Goal
Differentiate the app with unique, valuable features.

### Features to Implement

#### 8.1 Recipe Integration
- Link products to recipes
- Track ingredient availability
- Suggest recipes based on expiring items
- Recipe planning to reduce waste

#### 8.2 Shopping Assistant
- Store location/price tracking
- Best before date tracking by store
- Purchase recommendations
- Budget tracking

#### 8.3 Sustainability Metrics
- Carbon footprint saved
- Environmental impact visualization
- Gamification badges/achievements
- Social sharing of achievements

---

## Phase 9: Platform Optimization

### Goal
Optimize for each platform's unique capabilities.

### Platform-Specific Enhancements

#### 9.1 Android
- Home screen widgets
- Quick settings tile
- Share integration
- Camera integration for barcode scanning

#### 9.2 Desktop
- System tray integration
- Keyboard shortcuts
- Multi-window support
- Drag-and-drop support

#### 9.3 Web/WASM
- Progressive Web App (PWA) features
- Offline support
- Install prompt
- Share API integration

---

## Phase 10: Polish & Release

### Goal
Prepare for public release.

### Tasks

#### 10.1 Quality Assurance
- Comprehensive testing across platforms
- Performance optimization
- Accessibility improvements
- Localization (i18n)

#### 10.2 Documentation
- User guide/help section
- FAQ
- Privacy policy
- Terms of service

#### 10.3 Release Preparation
- App store listings
- Marketing materials
- Demo videos/screenshots
- Beta testing program

#### 10.4 Post-Launch
- Monitor crash reports
- User feedback collection
- Iterate based on feedback
- Regular updates and maintenance

---

## Technical Debt & Maintenance

### Ongoing Tasks
- Keep dependencies up to date
- Refactor code for maintainability
- Improve test coverage
- Performance monitoring
- Security audits

---

## Success Metrics

### Key Performance Indicators
- User retention rate
- Daily active users
- Food waste reduction (user-reported)
- Average products tracked per user
- Notification engagement rate
- Platform adoption distribution

---

## Timeline Estimation

**Phase 1-2:** Foundation (2-3 weeks)
**Phase 3:** Persistence (2-3 weeks)
**Phase 4:** Notifications (1-2 weeks)
**Phase 5:** UX Enhancements (3-4 weeks)
**Phase 6:** Analytics (2-3 weeks)
**Phase 7:** Collaboration (3-4 weeks)
**Phase 8:** Advanced Features (4-6 weeks)
**Phase 9:** Platform Optimization (2-3 weeks)
**Phase 10:** Polish & Release (2-3 weeks)

**Total Estimated Time:** 21-31 weeks (5-8 months)

---

## Notes

- Phases can overlap where dependencies allow
- User feedback should guide feature prioritization
- Core functionality (Phases 1-4) should be stable before advanced features
- Platform parity should be maintained where possible
- Regular releases with incremental features preferred over big-bang releases
