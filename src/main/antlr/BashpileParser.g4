parser grammar BashpileParser;
options { tokenVocab = BashpileLexer; }

program: statement+;

// statements, in descending order of complexity
statement
    : Import StringValues                       # importStatement
    | ShellLine Newline                         # shellLineStatement
    | While expression Colon indentedStatements # whileStatement
    | For OParen typedId (Comma typedId)* In StringValues CParen Colon indentedStatements
                                                # foreachFileLineLoopStatement
    | Function Id paramaters (Arrow complexType)?
                                                # functionForwardDeclarationStatement
    | Function Id paramaters tags? (Arrow complexType)?
                            Colon functionBlock # functionDeclarationStatement
    | If OParen expression CParen Colon indentedStatements (elseIfClauses)* (Else Colon indentedStatements)?
                                                # conditionalStatement
    | Switch expression Colon INDENT (Case expression Colon indentedStatements)+ DEDENT
                                                # switchStatement
    | <assoc=right> typedId (Equals expression)? Newline
                                                # variableDeclarationStatement
    | <assoc=right> (Id | listAccess) assignmentOperator expression Newline
                                                # reassignmentStatement
    | Print OParen argumentList? CParen Newline # printStatement
    | expression Newline                        # expressionStatement
    | Newline                                   # blankStmt
    ;

tags        : OBracket (StringValues*) CBracket;
// like (x: str, y: str = "Jordi")
paramaters  : OParen ( typedId (Comma typedId)* (Comma defaultedTypedId)* )? CParen
            | OParen ( defaultedTypedId (Comma defaultedTypedId)* ) CParen;
defaultedTypedId  : typedId Equals literal;
typedId     : Id Colon modifier* complexType;
complexType : types (LessThan types MoreThan)?;
modifier    : Exported | Readonly;
argumentList: expression (Comma expression)*;
elseIfClauses     : Else If OParen expression CParen Colon indentedStatements;
indentedStatements: INDENT statement+ DEDENT;
assignmentOperator: Equals | PlusEquals;

// Force the final statement to be a return.
// This is a work around for Bash not allawing the return keyword with a string.
// Bash will interpret the last line of a function (which may be a string) as the return if no return keyword.
// see https://linuxhint.com/return-string-bash-functions/ example 3
functionBlock       : INDENT statement* (returnPsudoStatement | statement) DEDENT;
returnPsudoStatement: Return expression? Newline;

// in operator precedence order, modeled on Java precedence at https://introcs.cs.princeton.edu/java/11precedence/
expression
    // level 16
    : listAccess                        # listAccessExpression
    | OParen expression CParen          # parenthesisExpression
    // level 15
    | expression op=(Increment | Decrement)   # unaryPostCrementExpression

    // level 14
    | <assoc=right> Minus? NumberValues       # numberExpression // unary minus
    | <assoc=right> Not expression            # notExpression
    | op=(Increment | Decrement) expression   # unaryPreCrementExpression

    // level 13
    | <assoc=right> expression As complexType # typecastExpression

    // level 12
    | <assoc=right> expression op=(Multiply|Divide) expression # multipyDivideCalculationExpression

    // level 11
    | <assoc=right> expression op=(Add|Minus) expression       # addSubtractCalculationExpression

    // level 10
    // lower than typecast, higher than equality operators
    | <assoc=right> unaryPrimary expression   # unaryPrimaryExpression

    // level 9
    | expression binaryPrimary expression     # binaryPrimaryExpression

    // level 4
    | expression combiningOperator expression # combiningExpression

    // other levels
    | shellString                       # shellStringExpression
    | looseShellString                  # looseShellStringExpression
    | verbatimShellString               # verbatimShellStringExpression
    | Id OParen argumentList? CParen    # functionCallExpression
    | argumentsBuiltin                  # argumentsBuiltinExpression
    | ListOf (OParen CParen | OParen expression (Comma expression)* CParen)
                                        # listOfBuiltinExpression
    | literal                           # literalExpression
    | Id                                # idExpression
    ;

literal : StringValues | NumberValues | BoolValues;
types    : Boolean | Integer | Float | String | List | Map | Reference;

// shellString, Bashpile's version of a subshell
shellString        : HashOParen shellStringContents* CParen;
looseShellString   : LHashOParen shellStringContents* CParen;
verbatimShellString: VHashOParen shellStringContents* CParen;
shellStringContents: DollarOParen shellStringContents* CParen
                   | OParen shellStringContents* CParen
                   | ShellStringText
                   | ShellStringEscapeSequence;

// full list at https://tldp.org/LDP/Bash-Beginners-Guide/html/sect_07_01.html
unaryPrimary: BashUnaryOperator | IsEmpty | NotEmpty
| Exists | DoesNotExist | RegularFileExists | DirectoryExists;

// one line means logically equal precidence (e.g. LessThan in the same as MoreThanOrEquals)
binaryPrimary: LessThan | LessThanOrEquals | MoreThan | MoreThanOrEquals | IsEqual | IsNotEqual;

combiningOperator: And | Or;

// translates to $1, $2, etc
argumentsBuiltin: Arguments OBracket (NumberValues | All | Splat) CBracket;

listAccess: Id OBracket (Minus? NumberValues | All) CBracket;
