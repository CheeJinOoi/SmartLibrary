# SmartLibrary

SmartLibrary is a Java desktop library management system built for DSA coursework. It combines a modern Swing GUI with custom data structures and algorithms, plus realistic library workflows: login, catalog management, circulation, reservations, fines, reviews, recommendations, and analytics.

## Quick start (Windows)

Double-click **`run.bat`** in the project folder.

Or from PowerShell / Command Prompt:

```powershell
run.bat
```

The script compiles all Java files into the `out` folder and launches the app.

### Manual run

```powershell
javac -Xlint:all -d out (Get-ChildItem src\app,src\core,src\dsa,src\model,src\ui -Filter *.java | ForEach-Object { $_.FullName })
java -cp out Main
```

### Requirements

- **Java JDK 17+** (or any JDK that supports recent Java syntax)
- JDK must be on your system `PATH` (`java` and `javac` commands available)

Download: [https://adoptium.net/](https://adoptium.net/)

---

## Demo login

| Role | Member ID | Password |
|---|---|---|
| Student | `S-0001` | `password123` |
| Librarian | `L-0001` | `admin123` |

Use **L-0001** for full admin access (add/remove books, manage members, undo, DSA lab).

---

## Appearance

Click **Dark mode** / **Light mode** on the login screen or in the top header to switch themes. The choice applies immediately across all tabs.

---

## Local database files (important)

When you run SmartLibrary, it saves data as simple text files in the **project root folder**:

| File | Stores |
|---|---|
| `smartlibrary-books.db` | Book catalog (title, author, shelf, copies, ratings, etc.) |
| `smartlibrary-members.db` | Member accounts (ID, name, role, email, password, fines) |
| `smartlibrary-sequences.db` | Auto-ID counters for books and members |
| `smartlibrary-reviews.db` | Book reviews |

These are **not** SQL databases — they are plain local storage files created automatically by the app.

### Authentication & roles
- Sign in / sign out with member ID and password
- **Student** — search, borrow, return, reserve, review, pay fines, recommendations
- **Librarian** — catalog & member management, condition updates, undo, DSA lab

### Catalog
- Add, update, remove books (librarian)
- Multiple copies, shelf location, condition tracking
- Search by title, author, category, ISBN, shelf, availability
- Binary search (exact title) and insertion sort
- Ratings and reviews

### Circulation
- Borrow / return with 7-day due dates
- Up to 3 active loans per member
- Overdue fines (RM1 per started week after grace period)
- Fine payments and lost-book charges (RM50 replacement + overdue)
- Per-book FIFO reservation queue

### Smart features
- Personalized recommendations
- All-time popular & trending this month
- Recently added & recently viewed books
- Book locator (shelf + status)
- Dashboard statistics
- Due-soon and overdue notifications

### DSA structures
`ArrayList`, `HashMap`, linked list, queue, stack, BST, max heap, merge sort, binary search, graph (BFS/DFS)

---

## Project structure

```
SmartLibrary/
├── run.bat                 ← double-click to run
├── readme.md
├── out/                    ← compiled classes (auto-created)
├── smartlibrary-*.db       ← local data files (auto-created)
└── src/
    ├── app/
    │   ├── Main.java       ← entry point
    │   └── DemoDataSeeder.java
    ├── core/
    │   ├── Library.java    ← business logic
    │   ├── LibraryDatabase.java
    │   └── LibrarySmokeTest.java
    ├── model/
    │   ├── Book.java
    │   ├── Member.java
    │   └── ...
    ├── dsa/
    │   ├── BinarySearch.java
    │   ├── BookYearBST.java
    │   └── ...
    ├── ui/
    │   ├── LibraryGUI.java ← Swing view
    │   └── AppTheme.java
    └── ...
```

---

## Validation

```powershell
javac -Xlint:all -d out src\app\*.java src\core\*.java src\dsa\*.java src\model\*.java src\ui\*.java
java -cp out LibrarySmokeTest
```

Expected output: `Library smoke test passed.`

---

## License / academic use

Built as a university DSA project. Suitable for demonstration, coursework submission, and local single-user library simulation.
