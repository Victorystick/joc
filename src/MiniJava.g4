grammar MiniJava;

@header {
	package mjc.minijava;
}

/// keywords
BOOLEAN : 'boolean' ;
CLASS : 'class' ;
DOT : '.' ;
ELSE : 'else' ;
EQ : '==' ;
EXTENDS : 'extends' ;
FALSE : 'false' ;
GEQ : '>=' ;
GT : '>' ;
IF : 'if' ;
INT : 'int' ;
L_AND : '&&' ;
L_OR : '||' ;
LBRACK : '[' ;
LENGTH : 'length' ;
LEQ : '<=' ;
LT : '<' ;
MINUS : '-' ;
NEQ : '!=' ;
NEW : 'new' ;
NOT : '!' ;
PLUS : '+' ;
PRINT : 'System.out.println' ;
PUBLIC : 'public' ;
RBRACK : ']' ;
RETURN : 'return' ;
STATIC : 'static';
STRING : 'String' ;
THIS : 'this' ;
TIMES : '*' ;
TRUE : 'true' ;
VOID : 'void' ;
WHILE : 'while' ;

program :
	main
	classDecl*
;


main :
	CLASS identifier '{'
		PUBLIC STATIC VOID identifier '(' STRING LBRACK RBRACK identifier ')' '{'
			varDecl*
			stmt*
		'}'
	'}'
;

classDecl :
	CLASS identifier generics? extension? '{'
		varDecl*
		methodDecl*
	'}'
;

extension :
	EXTENDS identifier generics?
;

generics
	: LT generic ( ',' generic )* GT
;

generic
	: identifier ( EXTENDS identifier )?
;

varDecl
	: type generics? identifier ';'
;

methodDecl :
	PUBLIC type generics? identifier '(' ( param ( ',' param )* )? ')' '{'
		varDecl*
		stmt*
		returnStmt
	'}'
;

param
	: type generics? identifier
;

stmt
	: '{' stmt* '}'                                 # block
	| IF '(' exp ')' stmt elseStmt?                 # if
	| WHILE '(' exp ')' stmt                        # while
	| PRINT '(' exp ')' ';'                         # print
	| identifier ( LBRACK exp RBRACK )? '=' exp ';' # assignment
;

elseStmt
	: ELSE stmt
;

returnStmt
	: RETURN exp ';'
;

exps
	: ( exp ( ',' exp )* )?
;

exp
	: and ( L_OR and )*
;

and
	: comparative ( L_AND comparative )*
;

comparative
	: relational ( eqOp relational)*
;

eqOp
	: EQ
	| NEQ
;

relational
	: additative ( cmpOp additative )?
;

cmpOp
	: LT
	| LEQ
	| GT
	| GEQ
;

additative
	: multiplicative ( addOp multiplicative )*
;

addOp
	: PLUS
	| MINUS
;

multiplicative
	: unary ( TIMES unary )*
;

unary
	: unary DOT LENGTH                  # arrayLength
	| unary LBRACK exp RBRACK           # arrayIndex
	| unary DOT identifier '(' exps ')' # call
	| primary                           # prim
;

primary
	: identifier                        # id
	| integerLiteral                    # integer
	| booleanLiteral                    # boolean
	| THIS                              # this
	| NEW INT LBRACK exp RBRACK         # newArray
	| NEW identifier generics? '(' ')'  # newObject
	| '(' exp ')'                       # parenExp
	| NOT unary                         # not
;

type
	: primitive
	| identifier
;

primitive
	: INT (LBRACK RBRACK)?
	| BOOLEAN
;

identifier : IDENTIFIER ;

IDENTIFIER : LETTER (LETTER | DIGIT)* ;

integerLiteral
	: INTEGER
;

booleanLiteral : (TRUE | FALSE) ;


/// multi-character tokens

INTEGER
	: ZERO
	| NON_ZERO_DIGIT DIGIT*
;

fragment
LETTER
	: 'a'..'z'
	| 'A'..'Z'
	| '_'
;

fragment
ZERO
	: '0'
;

fragment
DIGIT
	: ZERO
	| NON_ZERO_DIGIT
;

fragment
NON_ZERO_DIGIT
	: '1'..'9'
;

WS : [ \t\r\n\u000C]+ -> skip ; // skip spaces, tabs, newlines and ascii FF

COMMENT : '/*' .*? '*/' -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

ErrorChar : . ;
