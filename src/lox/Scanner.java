package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * The Scanner class maintains the methods and fields necessary for
 * traversing the source file and creating tokens.
 * The Scanner maintains the source code, the list of tokens, and
 * some variable fields that may contain the start of the token,
 * the current character, and the current line of the source the
 * Scanner is on.
 */
public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    // initialize the reserved keywords for lex
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }


    Scanner (String source) {
        this.source = source;
    }

    // Scans the source file and returns a list of tokens
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // scans the current word and builds a new token
    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\r':
            case '\t':
                // useless characters are ignored
                break;
            case '\n':
                // increment the line the scanner is on
                line++;
                break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL: TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment consumes the remainder of the line it is on
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
            case '"':
                // call the string lexing method
                string();
                break;
            default:
                if (isDigit(c)) {
                 number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character " + c);
                }
                break;
        }
    }

    // advances the current pointer in the source
    private char advance() {
        // get current character and move current to next character
        return source.charAt(current++);
    }

    // simple wrapper to add a token to the token list
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // adds a new token to the token list field
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // matches a given character to the current character being pointed at.
    // returns true if the given character matches, false otherwise.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        // skip the next character since we are consuming it
        current++;
        return true;
    }

    // peeks at the next character in the source file. If the file
    // ends return '\0'. if not, return the next character.
    // peek() does not move the current pointer.
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // peek ahead two characters and return the character.
    // if the end of the file is hit, we return '\0'.
    // peekNext() does not move the current pointer.
    private char peekNext() {
        if (current + 1 <= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // determines if the current pointer is at the end of the source file.
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // determine if the given character is a number
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // determine if the given character is a letter
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // determine if a character is either a letter or a number
    private boolean isAlphaNumeric(char c) {
        // determine if the given character is a number
        // or a letter
        // both are allowed in identifiers
        return isAlpha(c) || isDigit(c);
    }

    // consume a string literal in the source file.
    // throw an error if the string is unclosed or if
    // the end of the file is reached before the string literal
    // is terminated. Create a new token with the string literal
    // value
    private void string() {
        while(peek() != '"' && isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated String.");
            return;
        }

        // advance the current pointer to the next character
        // skip the last " character
        advance();

        // find the string value, skip the beginning and ending " characters
        String value = source.substring(start+1, current-1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // if it has a fraction component
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the . character
            // and begin consuming the number after
            advance();

            while (isDigit(peek())) advance();
        }

        // create a new token with the whole number, no need to skip
        // any values. The number is parsed to a double.
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
}
