# DAPAS - Digital Accounting Process Automation System

A small Java Swing + SQLite accounting app: journal entries, ledger, and a 12-column worksheet.
New to the code? Read **GUIDE.md** first - it explains every part in simple English.

## What you need
| Item | Details |
|---|---|
| Java | JDK 17 or newer (developed and tested with JDK 21) |
| Library | `sqlite-jdbc` jar in the `lib` folder (+ optional `slf4j-api-1.7.36.jar`) - see `lib\README.txt` |
| Nothing else | no Maven/Gradle; plain `javac` |

## Run it on Windows
Open Command Prompt in the DAPAS folder, then:
```
build.bat      (compiles everything into bin\)
run.bat        (starts the app)
test.bat       (compiles and runs all tests)
```
In VS Code you can also open the folder and press Run on `src\Main.java` (the `.vscode\settings.json` is already set up).

First time: click **SIGN-UP**, create an account, then **LOG-IN**.

## Where the data lives
`C:\Users\<your name>\DAPAS\dapas.db` - always this one file, no matter which folder you start the program from.
Errors with technical details are written to `C:\Users\<your name>\DAPAS\dapas-error.log`.

### Looking inside the database
Install **DB Browser for SQLite** (free), choose *Open Database*, pick `dapas.db`. Useful queries:
```sql
SELECT * FROM accounts;
SELECT * FROM journal_entries;
SELECT * FROM journal_lines;     -- debit/credit are in CENTAVOS: 500000 = P5,000.00
```

## Your old data
If `users.txt` or `journal_*.txt` from the old version are in the folder you start the program from,
they are imported once into SQLite (passwords get hashed), then renamed to `*.imported`. Nothing is deleted.

## Your logo
Copy it to `resources\logo.png`. It appears on the welcome and login cards. (If you do not add one, nothing is shown.)

## Folder map
```
src/Main.java            starts the program
src/database/Database    opens SQLite, creates tables, starter accounts
src/model/               plain data classes (User, Account, JournalEntry, JournalLine, WorksheetRow) + Formats
src/dao/                 ALL the SQL (UserDAO, AccountDAO, JournalDAO)
src/service/             the rules (AuthService, JournalService) + LegacyImporter, AppException
src/ui/                  the screens (Welcome, Login, SignUp, Home, Journal, Ledger, Columnar) + NavBar, Theme
test/TestRunner.java     tests (no extra libraries)
```
