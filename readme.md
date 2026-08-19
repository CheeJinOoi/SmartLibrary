This is the smart library system that implements diffrent adt on it.
ArrayList for storing books
HashMap for searching book id
 # SmartLibrary

 SmartLibrary is a Java desktop library management system that combines a modern Swing interface with practical data structures and algorithms. It supports catalog management, members, circulation, queues, history, fines, recommendations, notifications, dashboards, and local persistence.

 ## Run

 ```powershell
 javac -Xlint:all -d out src\*.java
 java -cp out Main
 ```

 On the first run, the application creates demo data containing 100 books, 20 students, and 3 teachers. Data is stored locally in:

 - `smartlibrary-books.db`
 - `smartlibrary-members.db`
 - `smartlibrary-sequences.db`

 These files are intentionally simple local storage for the academic desktop version.

 ## Main Workflows

 ### Catalog

 - Add, update, remove, and display books.
 - IDs are generated automatically, for example `B-0001`.
 - IDs are never reused after deletion.
 - Search books by title, author, category, ISBN, or availability.
 - Sort by title, author, year, popularity, or availability.
 - The ISBN lookup gives an exact book result.

 ### Members

 - Register students and teachers.
 - IDs are generated automatically as `S-0001` or `T-0001`.
 - Search members by name.
 - View a selected member's borrowing history.

 ### Circulation

 - Borrow using book ISBN, member ID, and member name.
 - Return using book ISBN, member ID, and member name.
 - Each member can have at most three active loans.
 - A book cannot be borrowed twice at the same time.
 - Returning a book validates the member identity against the active loan.
 - Fines are RM1 per started overdue week after the seven-day loan period.

 ### Waiting Queue

 When a book is unavailable, a member can join its queue. Returning the book removes and notifies the next waiting member.

 ### Recommendations

 Enter a member ID in the Dashboard and select `Recommendations`. The system recommends available and popular books based on:

 - Authors previously borrowed by the member.
 - Categories previously borrowed by the member.
 - Popularity among other members.
 - Current availability.

 Each recommendation explains why it was selected and excludes books already borrowed by that member.

 ### Notifications

 Notifications report events such as member registration, borrowing, returns, fines, waiting-list changes, and book availability. `Notifications` shows all events; `New notifications` shows only unread events.

 ### Current Holder

 Enter a book ISBN in the Dashboard's `Check holder ISBN` field to see the member ID and name holding the book, or confirm that it is available.

 ## DSA Demonstration

 | Structure or algorithm | Use | Typical complexity |
 |---|---|---|
 | `ArrayList` | Ordered in-memory book collection | Read by index: O(1), append: amortized O(1) |
 | `HashMap` | ISBN and member ID lookup | Average lookup: O(1) |
 | Linked list | Borrowing history | Append: O(n) in the current implementation |
 | Queue | Per-book waiting list | Enqueue: O(1), dequeue: O(1) |
 | Stack | Undo actions | Push/pop: O(1) |
 | Binary search | Title search after merge sorting | O(log n) search after O(n log n) sorting |
 | BST | Book lookup and ordering by year | Average O(log n), worst O(n) |
 | Max heap | Highest overdue priority | Insert/extract: O(log n), peek: O(1) |
 | Insertion sort | User-selected book ranking | O(n^2), useful for demonstrating the algorithm |

 ## Architecture

 - `Book`: catalog entity and popularity count.
 - `Member`: student or teacher account and active borrowing limit.
 - `BorrowRecord`: member, book, dates, return state, and fine.
 - `Library`: application service and coordination boundary.
 - `BorrowHistory`: linked-list history.
 - `WaitingQueue`: queue implementation.
 - `Stack` and `Action`: undo model.
 - `RecommendationSystem`: rule-based recommendations.
 - `Notification`: event messages and read state.
 - `LibraryDatabase`: local persistence adapter.
 - `LibraryGUI`: Swing desktop interface.

 ## Real-World Business Evaluation

 ### What is already realistic

 - Separate catalog, member, circulation, notification, and persistence responsibilities.
 - Stable identifiers and duplicate protection.
 - Borrowing limits and identity validation.
 - Availability, waiting queues, fines, history, and dashboard metrics.
 - An interface that maps to real librarian workflows.
 - A persistence adapter that can be replaced without rewriting the GUI.

 ### Current prototype limitations

 This is suitable as an academic prototype or small single-computer library tool, not yet as a production multi-user system. The main gaps are:

 - Local flat files instead of transactional SQL storage.
 - No authentication, roles, or audit log.
 - No concurrent multi-user access control.
 - Borrow history and notifications are currently session-oriented rather than fully persisted.
 - No barcode/RFID scanner integration.
 - No email, SMS, or push notification provider.
 - No automated backups, encryption, monitoring, or recovery strategy.
 - Swing is appropriate for a desktop prototype but not for browser or mobile access.

 ### Production upgrade path

 1. Move domain persistence to PostgreSQL using repository interfaces and transactions.
 2. Add Spring Boot REST services for books, members, loans, queues, reports, and notifications.
 3. Add authentication with roles such as librarian, administrator, teacher, student, and auditor.
 4. Persist all borrowing, return, fine, notification, and audit events.
 5. Add database constraints for unique IDs, valid loans, and queue consistency.
 6. Replace console-style event output with email, SMS, or push notification adapters.
 7. Add automated tests, API documentation, backups, metrics, and deployment configuration.
 8. Replace or complement Swing with a web or mobile frontend while keeping the service layer.

 ## Validation

 ```powershell
 javac -Xlint:all -d out src\*.java
 java -cp out LibrarySmokeTest
 ```

 The smoke test covers catalog, members, search, persistence, circulation, queues, history, heap, graph, dashboard, recommendations, notifications, fines, and undo.