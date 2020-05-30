package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;  // the first character of the lexeme being built
    private int current = 0;  // the character after the last character of the lexeme being built
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    };

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        // this function, (and hence all state-reading functions within it) assumes that the state is: start == current
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlphaUnderscore(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character " + c);
                }
                break;
        }
    }

    private char advance() {
        // advance the current's state and return it's previous state
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean match(char expected) {
        // like a 'conditional advance', also a lookahead
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        // lookahead; also logically, returns an infinite string of \0 after the source string
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        // lookahead of 2; returns infinite \0 after end of source string
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();  // include the closing quote into the lexeme

        String literal = source.substring(start + 1, current - 1);
        addToken(STRING, literal);
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();  // include the '.' into the lexeme
            while (isDigit(peek())) {
                advance();
            }
        }

        double literal = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, literal);
    }

    private void identifier() {
        while (isAlphaUnderscoreNumeric(peek())) advance();

        String text = source.substring(start, current);

        TokenType tokenType = keywords.get(text);
        if (tokenType == null) {
            addToken(IDENTIFIER);
        } else {
            addToken(tokenType);
        }
    }

    private void blockComment() {
        while (true) {
            if (isAtEnd() || (peek() == '*' && peekNext() == '/')) {
                break;
            }
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment.");
        } else {
            advance();  // consume the '*'
            advance();  // consume the '/'
        }
    }

    private boolean isAlphaUnderscore(char c) {
        return (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || c == '_');
    }

    private boolean isAlphaUnderscoreNumeric(char c) {
        return isAlphaUnderscore(c) || isDigit(c);
    }
}
