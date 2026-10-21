CHANGELOG
=========

2.0.0 (21.10.2026) +100 Commits & +30 Translations
------------

### Function

- Add: Support for Mastodon, Miniflux and Nextcloud-News feeds
- Add: Word-based blocking
- Add: Enable minify release
- Add: Web client's caching and timeout settings
- CompileSDK 37.1
- TargetSdk 37

### UI/UX

- Add: Weather widget
- Add: New theming system
- Add: Animated buttons and chips
- Update: Revamp all item layouts

1.9.0 (09.11.2025) +200 Commits & +30 Translations
------------

### Function

- Add: Options to export/import bookmarked articles
- Add: Google GSA bridge
- Fix: Crash on creating ConfigurationOverlayController
- Fix: Fix and improve OPML export/import logic
- Fix: Overlay's bookmark icon coloring
- Fix: Reload event from the dropdown menu
- Fix: Manual syncing
- Update: Split ArticleViewModel to ArticleViewModel, ArticleListViewModel & SortFilterViewModel
- Update: Rebase all viewmodels to be state-based
- Update: Replace deprecated test libraries
- Update: Replace FeedArticle with Article (DB)
- Update: Replace legacy FeedItem calls with the new embedded entity
- Update: Lazy composition of slide pages
- Update: Make all non-flow database calls suspend
- Remove: Double permission declaration in manifest (credits @thePrivacyFanatic)
- Remove: Unused aidl files

### UI/UX 

- Add: Permission dialog to enable draw over other apps (should fix usage on Lawnchair - credits @thePrivacyFanatic)
- Add: Bookmark button to article page
- Add: Sort/filter sheet for the launcher layout (as incomplete)
- Add: Support for predictive back gesture
- Add: Option to disable dynamic color
- Add: Option to see bookmarks in xml views
- Add: Tags filter
- Update: Revamp sort/filter sheet layout 
- Update: Separate enabled/disabled sources list
- Update: Replace source item's delete button with an enabling switch
- Update: Revamp button and chip layouts
- Update: Padding pages on showing keyboard
- Update: Revamp source edit page

1.8.0 (23.04.2025) +40 Commits & 10 Translations
------------

### Function

- Update: Revamp viewmodels and repositories applying separation of concerns
- Update: Make prefs real delegates
- Update: Restructure the project into 5 main packages
- TargetSDK 35

### UI/UX

- Add: SortFilter sheet to the articles page
- Fix: Back handling on edit and add feed pages
- Update: Replace removed MD icons with Phosphor icons
- Update: Fix background color of the navigation suite
- Update: Make UI wide screens friendly & navigation adaptive

1.7.2 (17.12.2024) +30 Commits & 5 Translations
------------

### Function

- Add: Option to open feed in WebView
- Update: Re-sync source after relevant update
- Update: Make FullTextWorker unique
- Update: PullToRefreshLazyColumn based on PullToRefreshBox
- Update: Replace deprecated usage of systemUiVisibility

### UI/UX

- Add: Parsing much more and improve existing HTML-tags
- Fix: Placeholder icons visibility on black backgrounds
- Fix: Applying dark/light system bars
- Update: Revamp pref layouts
- Update: Revamp About page
- Update: Improve UI paddings

1.7.1 (17.08.2024) -10 Commits & 1 Translations
------------

### Function

- Fix: Crashing offline reader on certain systems
- TargetSDK 34

### UI/UX

- Add: Black themes
- Update: Selection dialog layout

1.7.0 (15.08.2024) +120 Commits & +10 Translations
------------

### Function

- Fix: Make sure that DataStore is single
- Fix: Restarting app
- Fix: Over-composition of dialogs in SourcesPage
- Fix: Feed sorting by time
- Update: Use Flow for articles in overlay
- Update: Revamp NavigationManager to use args-safe NavRoute
- Update: Inject repos and client
- Update: Migrate DI from KodeIn to Koin
- Update: Use Kotlin generator in Room
- CompileSDK 35
- Kotlin 2.0
- Dependency Catalogue

### UI/UX

- Add: Bookmarks filter to OverlayPage
- Add: Pref to remove duplicate articles
- Add: Main pager with Feed, Settings & Feeds
- Add: Scroll to top button
- Add: Hint if no articles are present
- Add: Transparency & collapsable app bar
- Add: Share button as action to articles
- Fix: Applying updated Overlay theme
- Fix: Theming system
- Update: App icon
- Update: Use favorite instead of bookmark icon
- Update: Revamp preferences, articles and feed layouts
- Update: BookmarkItems use same layout as normal articles
- Update: Revamp & unify overlay layouts (xml & composable)
- Update: Overlay menu popup animator
- Remove: BookmarksPage
- Remove: Card background pref (for now)

1.6.0 (XX.XX.2023) Y Commits & Z Translations
------------

### Function
- Add: Bookmarks page
- Fix: Opening links from article page
- Fix: Editing disabled Feeds
- Update: CompileSdk 34

### UI
- Add: Dynamic-theming
- Add: Monochrome app icon
- Fix: StatusBar color
- Fix: About page shortcuts
- Update: Revamp Preferences & About pages
- Update: Article card layout
- Update: Return to top FAB color to match theme
- Update: Revamp all item components
- Update: Use Phosphor icons instead of Material
- Update: Drop Compose Material for Material3

### UX
- Add: Share & open-in-browser buttons to article view
- Add: Bookmark button to article cards
- Fix: Showing real state of Switches e.g. in EditFeedPage
