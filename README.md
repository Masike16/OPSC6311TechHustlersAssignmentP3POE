# Presentation video link:
15 June 2026
https://youtu.be/oblCpsUgM2A

# EasEBudget - Personal Budget Tracker Application

EasEBudget is a comprehensive personal finance management application designed to help users track their spending, manage budgets, and visualize their financial health. Built specifically with the South African market in mind, it defaults to South African Rands (ZAR) and provides a user-friendly interface for daily financial tracking. EasEBudget is a modern, efficient Android Personal Budget Tracker app built with Kotlin and Jetpack Compose. It combines the best features from YNAB, Goodbudget, and Wallet by BudgetBakers to guide users toward better financial habits while providing powerful tools for analysis.

## Features

### Core Features
- **Authentication System**: User registration with email/password validation, login functionality, and biometric authentication support
- **Transaction Management**: Add, edit, delete transactions (income and expenses) with optional receipt photo attachment
- **Category Management**: Predefined categories following the 50/30/20 rule (Bills, Needs, Wants) with custom category support
- **Budget Goals System**: Monthly total budget, min/max spending goals, savings goals with visual progress indicators
- **Reports & Analytics**: Bar charts, pie charts, and line graphs showing the spending spending patterns with budget goal reference lines
- **Gamification Elements**: Badge system (First Steps, Budget Master, Consistent Logger, Savings Hero, etc.), streak tracking, reward points

### Custom Features
- **Admin Panel**: Full admin dashboard to view all user accounts, manage passwords, activate/deactivate accounts, view platform statistics
- **Shared Accounts**: Link accounts with family/friends, approval system, role-based access control, shared budget tracking
- **Expense Prediction**: AI-powered prediction of future spending based on historical data
- **Budget Alerts**: Push notifications when approaching budget limits

### Additional Features
- **Dark Mode Support**: Material 3 theme with automatic dark mode switching. Default is matches the phone system appearance
- **Multi-language Support**: English, Afrikaans, isiZulu
- **Receipt Photo Attachment**: Capture or select receipt photos for transactions
- **Age of Money Metric**: Calculate average days between earnings and spendings
- **Biometric Authentication**: Fingerprint for quick login

## Technical Architecture

### Architecture
- **MVVM Architecture**: Clean separation of concerns with ViewModels and Repository
- **Jetpack Compose**: Modern UI toolkit for efficient, declarative UI
- **Room Database**: Local data persistence with Flow for reactive data streams
- **Hilt Dependency Injection**: Reduced boilerplate with compile-time dependency injection
- **Coroutines & Flow**: Asynchronous programming for responsive UI

### Project Structure
```
app/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── entities/ (User, Category, Transaction, BudgetGoal, Achievement, Streak, SharedAccount, Notification, AdminActionLog)
│   │   └── dao/ (AppDao.kt)
│   └── repository/ (AppRepository.kt)
├── ui/
│   ├── screens/ (AuthScreen, AdminLoginScreen, AdminDashboardScreen, DashboardScreen, TransactionsScreen, BudgetScreen, GoalsScreen, ReportsScreen, SettingsScreen, SharedAccountsScreen)
│   ├── components/ (reusable Compose components)
│   └── navigation/ (AppNavigation.kt)
├── viewmodel/ (AuthViewModel, AdminViewModel, DashboardViewModel, TransactionsViewModel, BudgetViewModel, GoalsViewModel, ReportsViewModel, SettingsViewModel, SharedAccountsViewModel)
└── utils/ (CurrencyFormatter, DateUtils, BiometricUtils, NotificationUtils, ValidationUtils, GamificationUtils, ImageUtils)
```

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Minimum SDK 21 (Android 5.0 Lollipop)

### Installation
1. Clone the repository:
```bash
git clone https://github.com/yourusername/EasEBudgetV1.git
cd EasEBudgetV1
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run the app:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Database Schema

### Entities
- **User**: User account information with authentication settings
- **Category**: Expense categories with budget limits and grouping
- **Transaction**: Income and expense records with optional receipt photos
- **BudgetGoal**: Monthly budget goals with min/max spending and savings targets
- **Achievement**: Gamification badges earned by users
- **Streak**: Streak tracking for consistent budget management
- **SharedAccount**: Shared account relationships between users
- **SharedAccountRequest**: Pending link requests for shared accounts
- **Notification**: In-app notifications for budget alerts and achievements
- **AdminActionLog**: Audit trail for admin actions

## Admin Panel

The admin panel provides comprehensive user management capabilities:
- View all registered users with account status
- View user passwords (for demo purposes)
- Change or reset user passwords
- Activate/deactivate user accounts
- Delete user accounts
- View platform-wide statistics
- Export all user data

**Admin Credentials:**
- Username: `admin`
- Password: `admin123`

## Features a Gamification System

### Badges
- **First Steps**: Add your first transaction
- **Budget Master**: Stay within budget for a month
- **Consistent Logger**: Log transactions for 7 consecutive days
- **Savings Hero**: Exceed savings goal
- **Category Pro**: Use all category features
- **Age of Money**: Reach 7/30/60/90 days age of money
- **Streak Champion**: 30-day budget check-in streak
- **Receipt Keeper**: Attach receipt to 10 transactions
- **Shared Saver**: Successfully use shared accounts
- **Goal Crusher**: Achieve 5 savings goals

### Points System
- Earn points for meeting budget goals
- Earn points for logging transactions
- Earn points for achieving badges
- Points displayed in Goals screen

## Custom Features Documentation

### Admin Panel
The admin panel allows platform administrators to:
- Monitor all user accounts
- Manage user access and security
- View platform-wide analytics
- Perform bulk operations
- Maintain audit logs

### Shared Accounts
Shared accounts enable:
- Family/family budget tracking
- Role-based access control (Primary/Linked)
- Approval system for new links
- Shared budget goals
- Activity feed showing who spent what

### Expense Prediction
AI-powered feature that:
- Analyzes historical spending patterns
- Predicts future monthly spending
- Helps users plan ahead
- Displays in Reports screen

## GitHub Actions

The project includes automated CI/CD pipeline:
- Automatic builds on push/PR
- Unit test execution
- APK generation
- Artifact upload

## Team Information behind EaseBudget

**Group Name:** Tech Hustlers
**Members:** ST10451774(Liyema Masala)
             ST10452404(Masike Jr Rasenyalo)
             ST10452409(Acazia Ammon)

## References

- YNAB. (2026). You Need A Budget. https://www.youneedabudget.com
- Goodbudget. (2026). Goodbudget - Home Budget Planner. https://www.goodbudget.com
- Wallet by BudgetBakers. (2026). Wallet - Budget & Finance. https://www.budgetbakers.com
- Adjust. (2026). Gamification in Finance Apps. https://adjust.com
- adpulse. (2026). Push Notification Strategies. https://adpulse.com
- Equifax. (2026). The 50/30/20 Budget Rule. https://www.equifax.com

## License

This project is created for educational purposes as part of the OPSC6311 course.

## Version

Current Version: 2.0
API Level: 21 (Android 5.0 Lollipop)
Target SDK: 34 (Android 14)
