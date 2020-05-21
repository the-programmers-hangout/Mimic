package uk.co.markg.bertrand.listener;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MessageReaderTest {


  @ParameterizedTest
  @ValueSource(strings = {"one valid message", "another\r\nvalid\n\rmessage",
      "another   \r\n\r\n  \r\nvalid\r\nmessage", "another   \r\n\r\n  \r\nvalid\r\n     message"})
  public void positiveValidMessageTest(String message) {
    assertTrue(MessageReader.messageIsValid(message));
  }


  @ParameterizedTest
  @ValueSource(strings = {"one \r\ninvalidmessage", "one  \r\n\r\n", "nOoooOOOOOOOOOOOooooOO \r\na",
      "\r\n\r\n  t \r\n\r\n."})
  public void negativeValidMessageTest(String message) {
    assertFalse(MessageReader.messageIsValid(message));
  }
}
