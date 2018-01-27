// Define a grammar called XQuery
grammar XQuery;

/* Parser Rules */

ap      : 'doc("' file '")' '/' rp          # ApChildren
        | 'doc("' file '")' '//' rp         # ApDescendants
        ;

file    : WORD ('.' WORD)*
        ;

rp      : WORD                              # TagName
        | '*'                               # Children
        | '.'                               # Descendants
        | '..'                              # Parent
        | 'text()'                          # Text
        | '@' WORD                          # Attribute
        | '(' rp ')'                        # RpParentheses
        | rp '/' rp                         # RpChildren
        | rp '//' rp                        # RpDescendants
        | rp '[' filter ']'                 # RpFilter
        | rp ',' rp                         # RpConcat
        ;

filter  : rp                                # FilterRp
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

WORD    : [a-zA-Z0-9_-]+;
WS      : [ \t\r\n]+ -> skip;

