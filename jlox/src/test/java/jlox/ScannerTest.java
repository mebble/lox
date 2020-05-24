package jlox;

import org.junit.Before;
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
    public void handlesWhiteSpace() {
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
    public void handlesComments() {
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
    public void setsErrorFlagOnUnexpectedCharacter() {
        assertThat(Lox.hadError, is(false));

        Scanner scanner = new Scanner("(+&");
        List<Token> tokens = scanner.scanTokens();

        List<Token> expected = Arrays.asList(
                new Token(TokenType.LEFT_PAREN, "(", null, 1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Token(TokenType.EOF, "", null, 1)
        );
        assertThat(tokens, equalTo(expected));
        assertThat(Lox.hadError, is(true));
    }
}
