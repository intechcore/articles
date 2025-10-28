grammar Jimple;

// root rule of grammar
program: (statement)* EOF;

// list of possible statements
statement: variableDeclaration
         | assignment
         | functionDefinition
         | functionCall
         | println
         | return
         | returnVoid
         | ifStatement
         | blockStatement
         | whileStatement
         ;

// list of possible expressions
expression: '(' expression ')'                                            #parenthesisExpr
          | left=expression op=(ASTERISK | SLASH | MOD) right=expression  #mulDivExpr
          | left=expression op=(PLUS | MINUS) right=expression            #plusMinusExpr
          | left=expression compOperator right=expression                 #compExpr
          | IDENTIFIER                                                    #idExp
          | NUMBER                                                        #numExpr
          | DOUBLE_NUMBER                                                 #doubleExpr
          | STRING_LITERAL                                                #stringExpr
          | BOOLEAN                                                       #booleanExpr
          | functionCall                                                  #funcCallExpr
          ;

// descriptions of individual expressions and statements
variableDeclaration: 'var' IDENTIFIER '=' expression ;

assignment: IDENTIFIER '=' expression ;

compOperator: op=(LESS | LESS_OR_EQUAL | EQUAL | NOT_EQUAL | GREATER | GREATER_OR_EQUAL) ;

println: 'println' expression ;

return: 'return' expression ;

returnVoid: 'return' ;

blockStatement: '{' (statement)* '}' ;

functionCall: IDENTIFIER '(' (expression (',' expression)*)? ')' ;

functionDefinition: 'fun' name=IDENTIFIER '(' (IDENTIFIER (',' IDENTIFIER)*)? ')' '{' (statement)* '}' ;

ifStatement: 'if' '(' expression ')' statement  elseStatement? ;

elseStatement: 'else' statement ;

whileStatement: WHILE SPACE* '(' expression ')' SPACE* statement ;

// list of tokens
WHILE               : 'while';
BOOLEAN             : BOOLEAN_TRUE | BOOLEAN_FALSE;
IDENTIFIER          : [a-zA-Z_] [a-zA-Z_0-9]* ;
NUMBER              : [0-9]+ ;
DOUBLE_NUMBER       : NUMBER '.' NUMBER ;
STRING_LITERAL      : '"' (~["])* '"' ;
VOID                : 'void';

ASTERISK            : '*' ;
SLASH               : '/' ;
PLUS                : '+' ;
MINUS               : '-' ;
MOD                 : '%' ;

ASSIGN              : '=' ;
EQUAL               : '==' ;
NOT_EQUAL           : '!=' ;
LESS                : '<' ;
LESS_OR_EQUAL       : '<=' ;
GREATER             : '>' ;
GREATER_OR_EQUAL    : '>=' ;

BOOLEAN_TRUE        : 'true';
BOOLEAN_FALSE        : 'false';

SPACE               : [ \r\n\t]+ -> skip;
LINE_COMMENT        : '//' ~[\n\r]* -> skip;
