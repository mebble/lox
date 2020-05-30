package jlox;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScannerTest {
    @Before
    public void setUp() {
        Lox.hadError = false;
    }

    @Test
    public void recognisesSingleAndDoubleCharacterTokens() {
        Scanner scanner = new Scanner("(+/}>==,");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
            new Token(TokenType.LEFT_PAREN, "(", null, 1),
            new Token(TokenType.PLUS, "+", null, 1),
            new Token(TokenType.SLASH, "/", null, 1),
            new Token(TokenType.RIGHT_BRACE, "}", null, 1),
            new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
            new Token(TokenType.EQUAL, "=", null, 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void recognisesTokenLineNumber() {
        Scanner scanner = new Scanner("(\n/-\n+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
            new Token(TokenType.LEFT_PAREN, "(", null, 1),
            new Token(TokenType.SLASH, "/", null, 2),
            new Token(TokenType.MINUS, "-", null, 2),
            new Token(TokenType.PLUS, "+", null, 3),
            new Token(TokenType.EOF, "", null, 3)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void ignoresWhiteSpace() {
        Scanner scanner = new Scanner("+  \r\n+\t+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
            new Token(TokenType.PLUS, "+", null, 1),
            new Token(TokenType.PLUS, "+", null, 2),
            new Token(TokenType.PLUS, "+", null, 2),
            new Token(TokenType.EOF, "", null, 2)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void ignoresComments() {
        Scanner scanner = new Scanner("+//comment1+/*-}\n+-//comment2");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.PLUS, "+", null, 2),
                new Token(TokenType.MINUS, "-", null, 2),
                new Token(TokenType.EOF, "", null, 2)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void ignoresBlockComments() {
        Scanner scanner = new Scanner("one/**comment\none+two/*haha*/\n+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.IDENTIFIER, "one", null, 1),
                new Token(TokenType.PLUS, "+", null, 3),
                new Token(TokenType.EOF, "", null, 3)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void setErrorFlagOnUnterminatedBlockComment() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("one/*untermina/*...");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.IDENTIFIER, "one", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }

    @Test
    public void setsErrorFlagOnUnexpectedCharacter() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("(+&}");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.LEFT_PAREN, "(", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.RIGHT_BRACE, "}", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }

    @Test
    public void handlesStrings() {
        Scanner scanner = new Scanner("+\"stringxxxx\"+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.STRING, "\"stringxxxx\"", "stringxxxx", 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void handlesMultilineStrings() {
        Scanner scanner = new Scanner("+\"string\nabc\nxxxx\"+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.STRING, "\"string\nabc\nxxxx\"", "string\nabc\nxxxx", 3),
                new Token(TokenType.PLUS, "+", null, 3),
                new Token(TokenType.EOF, "", null, 3)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void setsErrorFlagOnUnterminatedString() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("+\"stringxxxx");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }

    @Test
    public void handlesNumberLiterals() {
        Scanner scanner = new Scanner("+029-90");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "029", 29.0, 1),
                new Token(TokenType.MINUS, "-", null, 1),
                new Token(TokenType.NUMBER, "90", 90.0, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void handlesNumberLiteralsWithDecimals() {
        Scanner scanner = new Scanner("+0100.23+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.NUMBER, "0100.23", 100.23, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    @Ignore  // WIP
    public void errorOnNumberWithLeadingDecimal() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("+.123+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }

    @Test
    @Ignore  // WIP
    public void errorOnNumberWithTrailingDecimal() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("+123.+");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }

    @Test
    public void handlesIdentifiers() {
        Scanner scanner = new Scanner("-12__one_99+two");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.MINUS, "-", null, 1),
                new Token(TokenType.NUMBER, "12", 12.0, 1),
                new Token(TokenType.IDENTIFIER, "__one_99", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.IDENTIFIER, "two", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }

    @Test
    public void handlesKeywords() {
        Scanner scanner = new Scanner("-12ifelse if else moo+class");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.MINUS, "-", null, 1),
                new Token(TokenType.NUMBER, "12", 12.0, 1),
                new Token(TokenType.IDENTIFIER, "ifelse", null, 1),
                new Token(TokenType.IF, "if", null, 1),
                new Token(TokenType.ELSE, "else", null, 1),
                new Token(TokenType.IDENTIFIER, "moo", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.CLASS, "class", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
    }
}