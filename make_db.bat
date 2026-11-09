@echo off
rem Create the SQLite database using the existing Database class.
rem The database will be created at C:\Users\<your-name>\DAPAS\dapas.db
javac -encoding UTF-8 -cp "bin;lib\*" -d bin make_db_helper.java
java -cp "bin;lib\*" make_db_helper
del make_db_helper.java make_db_helper.class
@echo on