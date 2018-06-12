COEN 281 - Group 2 ———
Combining Mining Techniques to Improve Business Recommendation with Yelp Database
# Yelp-Recommender-System
We will be using a hybrid methodology to improve the recommendations of businesses in the Yelp dataset. This was created for the COEN 281 Pattern Recognition and Data Mining course at Santa Clara University.

Team Lead: Julie Wasiuk
Group Members: Jason Chen, Andrew Leonard, Alan Nguyen, Cassidy Tarng
—————————————

Steps for Running our software:

  1. Download Jars from here:
  a. 
  b.
  
  2. Download the Yelp Dataset from : https://www.yelp.com/dataset/download
  3. Set up a MySql Server by going to MySql.com https://dev.mysql.com/doc/mysql-getting-started/en/
    Download the database and set up the server so it interacts with your database. 
  4. Go to DatabaseDumper.java. Change the credentials at lines 16, 17, and 18 to reflect your server.
  5. Run the main of DatabaseDumper.java. This code converts the MySql database to a Sqlite Database. It takes several hours.
  6. Copy all the files to a computer that has sqlite3 installed (/local/weka in the DC) and execute all of those newly generated sql files to read them into your sqlite database.
  7. (Optional) Create the table in the database for the Term Frequency Analysis using this command:
  CREATE TABLE BusinessKeyTerms(id varchar(255), businessID varchar(255), keyTerm varchar(255));
  8. (Optional) Run the Term Frequency Analyzer using the following commands (This takes at least a day depending on your hardware):
  javac -cp .:/local/weka/lib/* TermFrequencyAnalyzer.java
java -cp .:/local/weka/lib/* TermFrequencyAnalyzer
9. Run the main in RecommenderSystem
  
  
  
  
 

