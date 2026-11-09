Put these two .jar files in THIS folder (lib):

1. sqlite-jdbc-<version>.jar   - the SQLite driver (required)
   https://central.sonatype.com/artifact/org.xerial/sqlite-jdbc
   (while preparing this project the newest version I saw was 3.53.2.0; any recent version works)

2. slf4j-api-1.7.36.jar        - a tiny logging library that sqlite-jdbc can use (recommended)
   https://central.sonatype.com/artifact/org.slf4j/slf4j-api/1.7.36

If you already have a sqlite-jdbc jar from the old project, just copy it here.
Without the driver the program shows: "The SQLite driver was not found."
