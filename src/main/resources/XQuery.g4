// Define a grammar called XQuery
grammar XQuery;

/* Parser Rules */

xq          : var                               # XqVariable
            | StringConstant                    # XqString
            | ap                                # XqAp
            | '(' xq ')'                        # XqParentheses
            | xq ',' xq                         # XqConcat
            | xq '/' rp                         # XqChildren
            | xq '//' rp                        # XqDescendants
            | startTag ('{')* xq ('}')* endTag  # XqEnclosedTags
            | forClause letClause? whereClause?
              returnClause                      # XqFLWR
            | letClause xq                      # XqLetClause
            | joinClause                        # XqJoinClause
            ;

var         : '$' WORD
            ;

startTag    : '<' WORD '>'                      # XqStartTag
            ;

endTag      : '</' WORD '>'                     # XqEndTag
            ;

forClause   : 'for' var 'in' xq (',' var 'in' xq)*
            ;

letClause   : 'let' var ':=' xq (',' var ':=' xq)*
            ;

returnClause: 'return' xq
            ;

joinClause  : 'join' '(' xq ',' xq ','  attrList ',' attrList ')'
            ;

attrList    : '[' attrName (',' attrName)* ']'
            ;

attrName    : WORD
            ;

whereClause : 'where' cond
            ;

cond        : xq '=' xq                         # CondEqual
            | xq 'eq' xq                        # CondEqual
            | xq '==' xq                        # CondIs
            | xq 'is' xq                        # CondIs
            | 'empty' '(' xq ')'                # CondEmpty
            | 'some' var 'in' xq (',' var 'in' xq)*
              'satisfies' cond                  # CondSome
            | '(' cond ')'                      # CondParentheses
            | cond 'and' cond                   # CondAnd
            | cond 'or' cond                    # CondOr
            | 'not' cond                        # CondNot
            ;

ap          : doc '/' rp                        # ApChildren
            | doc '//' rp                       # ApDescendants
            ;

doc         : 'doc' '("' fname '")'             # XmlDoc
            ;

fname       : WORD ('.' WORD)*                  # FileName
            ;

rp          : WORD                              # TagName
            | '*'                               # Children
            | '.'                               # Current
            | '..'                              # Parent
            | 'text()'                          # Text
            | '@' WORD                          # Attribute
            | '(' rp ')'                        # RpParentheses
            | rp '/' rp                         # RpChildren
            | rp '//' rp                        # RpDescendants
            | rp '[' filter ']'                 # RpFilter
            | rp ',' rp                         # RpConcat
            ;

filter      : rp                                # FilterRp
            | rp '=' rp                         # FilterEqual
            | rp 'eq' rp                        # FilterEqual
            | rp '==' rp                        # FilterIs
            | rp 'is' rp                        # FilterIs
            | '(' filter ')'                    # FilterParentheses
            | filter 'and' filter               # FilterAnd
            | filter 'or' filter                # FilterOr
            | 'not' filter                      # FilterNot
            ;

/* Lexer Rules */

WORD                : [a-zA-Z0-9_-]+;
WS                  : [ \t\r\n]+ -> skip;
StringConstant      : '"'[a-zA-Z0-9_ ,.!?;'"-]+'"';

