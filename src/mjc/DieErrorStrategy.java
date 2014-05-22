package mjc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;

public class DieErrorStrategy extends DefaultErrorStrategy {

	@Override
	public void recover(Parser recognizer, RecognitionException e) {
		System.err.format("Unknown ANTLR error: %s", e.getMessage());
		System.exit(1);
	}

	@Override
	public void reportNoViableAlternative(Parser recognizer, NoViableAltException e) throws RecognitionException {
		Errors.fatal(e.getOffendingToken(),
				String.format("no viable alternative: '%s'", e.getOffendingToken().getText()));
	}

	@Override
	public void reportInputMismatch(Parser recognizer, InputMismatchException e) throws RecognitionException {
		String cont = "%s";

		if (e.getExpectedTokens().size() > 1) {
			cont = "one of [ %s ]";
		}

		Errors.fatal(e.getOffendingToken(),
			String.format(
				"Mismatched input: got %s, expected " + cont,
				getTokenErrorDisplay(e.getOffendingToken()),
				e.getExpectedTokens().toString(recognizer.getTokenNames())));
	}

	@Override
	public void reportMissingToken(Parser recognizer) {
		Token badToken = recognizer.getCurrentToken();

		Errors.fatal(badToken,
			String.format(
				"Mismatched input: got %s, expected %s",
				getTokenErrorDisplay(badToken),
				getExpectedTokens(recognizer).toString(recognizer.getTokenNames())));
	}

	@Override
	public void reportUnwantedToken(Parser recognizer) {
		Token badToken = recognizer.getCurrentToken();

		Errors.fatal(badToken,
			String.format(
				"Mismatched input: strange token %s, expected %s",
				getTokenErrorDisplay(badToken),
				getExpectedTokens(recognizer).toString(recognizer.getTokenNames())));
	}

	@Override
	public Token singleTokenDeletion(Parser recognizer) {
		Errors.fatal(recognizer.getCurrentToken(),
			String.format(
				"Mismatched input: extraneous %s",
				getTokenErrorDisplay(recognizer.getCurrentToken())));
		return null;
	}
}
