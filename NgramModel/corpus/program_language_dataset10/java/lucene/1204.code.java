package org.apache.lucene.queryParser.standard.parser;
public interface StandardSyntaxParserConstants {
  int EOF = 0;
  int _NUM_CHAR = 1;
  int _ESCAPED_CHAR = 2;
  int _TERM_START_CHAR = 3;
  int _TERM_CHAR = 4;
  int _WHITESPACE = 5;
  int _QUOTED_CHAR = 6;
  int AND = 8;
  int OR = 9;
  int NOT = 10;
  int PLUS = 11;
  int MINUS = 12;
  int LPAREN = 13;
  int RPAREN = 14;
  int COLON = 15;
  int CARAT = 16;
  int QUOTED = 17;
  int TERM = 18;
  int FUZZY_SLOP = 19;
  int RANGEIN_START = 20;
  int RANGEEX_START = 21;
  int NUMBER = 22;
  int RANGEIN_TO = 23;
  int RANGEIN_END = 24;
  int RANGEIN_QUOTED = 25;
  int RANGEIN_GOOP = 26;
  int RANGEEX_TO = 27;
  int RANGEEX_END = 28;
  int RANGEEX_QUOTED = 29;
  int RANGEEX_GOOP = 30;
  int Boost = 0;
  int RangeEx = 1;
  int RangeIn = 2;
  int DEFAULT = 3;
  String[] tokenImage = {
    "<EOF>",
    "<_NUM_CHAR>",
    "<_ESCAPED_CHAR>",
    "<_TERM_START_CHAR>",
    "<_TERM_CHAR>",
    "<_WHITESPACE>",
    "<_QUOTED_CHAR>",
    "<token of kind 7>",
    "<AND>",
    "<OR>",
    "<NOT>",
    "\"+\"",
    "\"-\"",
    "\"(\"",
    "\")\"",
    "\":\"",
    "\"^\"",
    "<QUOTED>",
    "<TERM>",
    "<FUZZY_SLOP>",
    "\"[\"",
    "\"{\"",
    "<NUMBER>",
    "\"TO\"",
    "\"]\"",
    "<RANGEIN_QUOTED>",
    "<RANGEIN_GOOP>",
    "\"TO\"",
    "\"}\"",
    "<RANGEEX_QUOTED>",
    "<RANGEEX_GOOP>",
  };
}
