package uk.co.markg.mimic.markov;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TrigramTest {

  List<String> text = List.of("the cat sat on the mat", "the mat that the cat sat on was blue",
      "the cat was white");

  @Test
  public void testTrigram() {
    Markov chain = MarkovLoader.of(Trigram.class).from(text);
    assertNotNull(chain.generate());
  }

}
