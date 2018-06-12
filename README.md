COEN 281 - Group 2 ———
Combining Mining Techniques to Improve Business Recommendation with Yelp Database
# Yelp-Recommender-System
We will be using a hybrid methodology to improve the recommendations of businesses in the Yelp dataset. This was created for the COEN 281 Pattern Recognition and Data Mining course at Santa Clara University.

Team Lead: Julie Wasiuk
Group Members: Jason Chen, Andrew Leonard, Alan Nguyen, Cassidy Tarng
—————————————

Steps for Running our software:

  1. Download Jars from here:
  a. Stanford POSTagger - https://nlp.stanford.edu/software/tagger.html
  b. Maven Repository Jackson - https://mvnrepository.com/artifact/com.fasterxml.jackson.core
  c. Maven Repository MySQL Connector - https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.11
  d. Maven Repository SQLite JDBC - https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.21.0

  2. Download the Yelp Dataset from : https://www.yelp.com/dataset/download
  3. Set up a MySql Server by going to MySql.com https://dev.mysql.com/doc/mysql-getting-started/en/
    Download the database and set up the server so it interacts with your database.
  4. Go to DatabaseDumper.java. Change the credentials at lines 16, 17, and 18 to reflect your server.
  5. Run the main of DatabaseDumper.java. This code converts the MySql database to a Sqlite Database. It takes several hours.
  6. Copy all the files to a computer that has sqlite3 installed (/local/weka in the DC) and execute all of those newly generated sql files to read them into your sqlite database. For tables that have multiple sql files, run them in ascending order.
  7. (Optional) Create the table in the database for the Term Frequency Analysis using this command:
  CREATE TABLE BusinessKeyTerms(id varchar(255), businessID varchar(255), keyTerm varchar(255));
  8. (Optional) Run the Term Frequency Analyzer using the following commands (This takes at least a day depending on your hardware):
  javac -cp .:/local/weka/lib/* TermFrequencyAnalyzer.java
  java -cp .:/local/weka/lib/* TermFrequencyAnalyzer
  9. Create the tables in the database to hold serialized Models and NeuralNetworks by using the following commands:
  CREATE TABLE `model` (`iters` int(11) not null unique,`filepath` varchar(30) not null,`rmse` real);
  CREATE TABLE `net` (`iters` int(11) not null unique,`filepath` varchar(30) not null,`rmse` real);
  9. Go to RecommenderSystem.java. Change the credentials at line 30 to reflect your sqlite installation.
  10. Run the main in RecommenderSystem with the following optional flags:
      -t: Train. Trains a recommender system. It will train and saturate, stop execution early when error reaches a minima. The recommender systems will be serialized and stored to file.
      -r: Recommend. Recommends businesses for a given user.
