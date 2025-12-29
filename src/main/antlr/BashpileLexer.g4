lexer grammar BashpileLexer;

tokens { INDENT, DEDENT }

@lexer::header {
  import com.yuvalshavit.antlr4.DenterHelper;
  import org.bashpile.core.antlr.Lexers;
}

@lexer::members {
  private final DenterHelper denter = DenterHelper.builder()
    .nl(Newline)
    .indent(BashpileLexer.INDENT)
    .dedent(BashpileLexer.DEDENT)
    .pullToken(BashpileLexer.super::nextToken);

  @Override
  public Token nextToken() {
    return denter.nextToken();
  }

  private boolean isLinuxCommand(CharStream input) {
    return Lexers.isLinuxCommand(input);
  }
}

// keywords
Boolean  : 'boolean';
Integer  : 'integer';
Float    : 'float';
String   : 'string';
List     : 'list';
Map      : 'map';
Reference: 'reference';

Function : 'function';
Return   : 'return';
Print    : 'print';
BoolValues: 'true' | 'false';
If       : 'if';
Else     : 'else';
Switch   : 'switch';
Case     : 'case';
Pass     : 'pass';
Arguments: 'arguments';
All      : 'all';
Splat    : 'splat';
Exported : 'exported';
Readonly : 'readonly';
ListOf   : 'listOf';
While    : 'while';
For      : 'for';
In       : 'in';
Import   : 'import';

// operators, in precidence order
// opening parenthesis
OParen  : '(';
// closing parenthesis
CParen  : ')';
Increment: '++';
Decrement: '--';
Not     : 'not';
Arrow   : '->';
Multiply: '*';
Divide  : '/';
Add     : '+';
Minus   : '-';
As      : 'as';
// Bash -e
Exists  : 'exists';
DoesNotExist: 'doesNotExist';
IsEmpty : 'isEmpty';
NotEmpty: 'isNotEmpty';
// Bash -f
RegularFileExists: 'regularFileExists';
DirectoryExists: 'directoryExists';
LessThan: '<';
LessThanOrEquals: '<=';
MoreThan: '>';
MoreThanOrEquals: '>=';
IsEqual : '==';
IsNotEqual : '!=';
And     : 'and';
Or      : 'or';
Equals  : '=';
PlusEquals: '+=';
BashUnaryOperator: '-a' | '-b' | '-c' | '-d' | '-e' | '-f' | '-g' | '-h' | '-k' | '-n' | '-o' | '-p'
|  '-r' | '-s' | '-t' | '-u' | '-v' | '-w' | '-x' | '-z'
| '-G' | '-L' | '-N' | '-O' | '-R' | '-S';

// shell lines using Semantic Predicate
ShellLine   : {isLinuxCommand(_input)}? (Id Equals (NumberValues | StringValues))* Id SHELL_LINE_WORD*;

// ID and Numbers

// must start with a letter or underscore, then may have numbers
Id: ID_START ID_CONTINUE*;

NumberValues: FloatValues | IntegerValues;

// future proof for octals to start with '0' like in C
IntegerValues: NON_ZERO_DIGIT DIGIT* | '0';

FloatValues: INT_PART? FRACTION | INT_PART '.';

// newlines, whitespace and comments
Newline       : '\r'? '\n' ' '*;
Whitespace    : [ \t\f] -> skip;
EscapedNewline: '\\' '\r'? '\n' ' '* -> skip;
BashpileDoc   : '/**' .*? '*/' -> skip;
Comment       : '//' ~[\r\n\f]* -> skip;
BlockComment  : '/*' ( BlockComment | . )*? '*/' -> skip;

// small tokens

Colon   : ':';
Comma   : ',';
// opening square bracket
OBracket: '[';
// closing square bracket
CBracket: ']';

// strings

StringValues
 : '\'' ( StringEscapeSequence | ~[\\\r\n\f'] )* '\''
 | '"'  ( StringEscapeSequence | ~[\\\r\n\f"] )* '"'
 ;

StringEscapeSequence: '\\' . | '\\' Newline;

// tokens for modes

LHashOParen  : 'l#(' -> pushMode(SHELL_STRING);
VHashOParen  : 'v#(' -> pushMode(SHELL_STRING);
HashOParen   : '#(' -> pushMode(SHELL_STRING);
DollarOParen : '$(' -> pushMode(SHELL_STRING);

// modes

/** See https://github.com/sepp2k/antlr4-string-interpolation-examples/blob/master/with-duplication/StringLexer.g4 */
mode SHELL_STRING;
ShellStringDollarOParen  : '$(' -> type(DollarOParen), pushMode(SHELL_STRING);
ShellStringOParen        : '(' -> type(OParen), pushMode(SHELL_STRING);
ShellStringText          : (~[\\\f()#$]
                            // LookAhead 1 - don't match '#(' but match other '#' characters
                            | '#' {_input.LA(1) != '('}?
                            | '$' {_input.LA(1) != '('}?
                           )+;
ShellStringEscapeSequence: '\\' . | '\\' Newline;
ShellStringCParen        : ')' -> type(CParen), popMode;

// fragments

fragment SHELL_LINE_WORD: ( StringEscapeSequence | ~[\\\r\n\f] )+;

fragment ID_START   : [a-zA-Z_];
// same as Bash ID rules -- no '-' for snake-case
fragment ID_CONTINUE: [a-zA-Z0-9_];

fragment NON_ZERO_DIGIT: [1-9];
fragment DIGIT         : [0-9];

fragment INT_PART: DIGIT+;
fragment FRACTION: '.' DIGIT+;
