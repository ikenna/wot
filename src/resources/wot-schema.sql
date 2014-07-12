
CREATE TABLE BOOK (
  BOOKURL VARCHAR PRIMARY KEY,
  TITLE VARCHAR,
  HASHTAG VARCHAR,
  NUMTWEETS INT,
  AUTHORURL VARCHAR,
  READERS INT,
  LANG VARCHAR,
  NUMTRANS INT,
  NUMPAGES INT,
  MINPRICE INT,
  MAXPRICE INT,
  COMPLETEPERCENT INT,
  COMPLETETHRESHOLD BOOLEAN,
  CATEGORYURL VARCHAR
);



CREATE TABLE AUTHORTWEETS(
  AUTHORURL VARCHAR NOT NULL,
  TWEETTEXT VARCHAR,
  TWEETURL VARCHAR PRIMARY KEY,
  RETWEETCOUNT INT
);

CREATE TABLE AUTHOR(
  AUTHORURL VARCHAR PRIMARY KEY,
  NAME VARCHAR NOT NULL,
  TWITTERHANDLE VARCHAR,
  TWITTERURL VARCHAR,
);

CREATE TABLE BOOKTWEETS(
  TWEETURL VARCHAR PRIMARY KEY,
  BOOKURL VARCHAR NOT NULL,
  TWEETTEXT VARCHAR,
  RETWEETCOUNT INT,
  SENTIMENT VARCHAR,
  HASHTAG VARCHAR,
  ORIGINATORURL VARCHAR,
  BYAUTHOR BOOLEAN
);



