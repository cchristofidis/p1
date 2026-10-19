import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.*;

/**
 * Test suite for MarkovModels
 *
 * @author Emily Du
 * @author Havish Malladi
 * @author Owen Astrachan 2024, revised 2025
 */
public class MarkovGenerateTest {

    private BaseMarkovModel getModel(int order) {
        // Choose which implementation to test here
        return new HashMarkovModel(order);
        //return new HashMarkovModel(order);
    }


    /**
     * Tests correctness by checking finding certain substrings that
     * must be in the generated text and k-grams that can't be in the text.
     */
    @Test
    public void testSimpleKgram() {
        String training = "a a b b a a b b a a b b a a b b a a b b a"
				+ " a b b a a b b a a b b a a b b a a b b a a b b a a b b";
    	String[] must = {"a a b", "a b b", "b b a", "b a a", "a a b b a a b b"};
    	String[] notExist = {"a a a", "b b b", "a b a", "b a b"};


        BaseMarkovModel markov = getModel(2);
        markov.trainText(training);
        String output = "";
        while (output.length() < 15) {
            output = markov.generate(100);
        }

        for (String s : must) {
            assertTrue(output.contains(s));
        }
        for (String s : notExist) {
            assertFalse(output.contains(s));
        }
    }


    /**
     * This test checks if MarkovModel makes a correct " Ngram when the source
     * contains only one pattern of distinct letters
     */
    @Test
    public void testMapAllRepeats() {
        String description = "This test checks the text generated when all words in the source are 'abc'.";
    	int length = 100;
    	int maxOrder = 7;
    	
    	StringBuilder testStringBuilder = new StringBuilder();
    	for (int i = 0; i < length; i++) {
    		testStringBuilder.append("abc");
    		if (i < length - 1)
    			testStringBuilder.append(' ');
    	}
		String testString = testStringBuilder.toString();
		
		for (int k = 1; k <= maxOrder; k++) {
			BaseMarkovModel markov = getModel(k);
			markov.trainText(testString);
			String output = "";
            while (output.length() < 15){
                output = markov.generate(length);  // For readability
                if (output.startsWith("<END>")){
                    output = "";
                }
            }
              
			int dex = output.indexOf("<END>");
			if (dex >= 3){
                System.err.printf("order %d, text %s\n",k,output);
				output = output.substring(0,dex-1).trim();
			}
    
            HashSet<String> set = new HashSet<>();
            for(String s : output.split(" ")) set.add(s);
            assertTrue("output contains 'abc' "+output, set.contains("abc"));
            assertTrue("output contains only 'abc' "+output,set.size() == 1);
        }
    }


    /**
     * This test checks your return value of getFollows() contains
     * all characters that follow the k-gram.
     */
    @Test
    public void testGetFollows() {

        String testString = "e f g e f g f e f g g e f g h e f g x e f g y e f g z y efg x h g f e";  // Note it sterts with "efg" insteeh of "efge"
        String[][] inStrings = {
                {"e", "f", "g"},
                {"f", "g"},
                {"g", "g"},
                {"f", "g", "h", "e"},
                {"y"},
                {"h", "g", "f"}};
        String[][] out = {
                {"e", "f", "g", "h", "x", "y", "z"},
                {"e", "f", "g", "h", "x", "y", "z"},
                {"e"},
                {"f"},
                {"e", "efg"},
                {"e"}};

        @SuppressWarnings("unchecked")
        ArrayList<String>[] in = new ArrayList[inStrings.length];
        for (int i = 0; i < inStrings.length; i++) {
            in[i] = new ArrayList<>(Arrays.asList(inStrings[i]));
        }
        
        for (int i = 0; i < in.length; i++) {
            int k = in[i].size();
            BaseMarkovModel markov = getModel(k);
            markov.trainText(testString);
            List<String> expected = Arrays.asList(out[i]);
            List<String> actual = markov.getFollows(in[i]);
            checkListUnordered(expected, actual);
        }
    }

    /**
     * This test checks your return value of getFollows() contains all characters that follow the k-gram,
     * with the corresponding frequencies for duplicates.
     */
    @Test
    public void testGetFollowsRepeats() {
        String testString = "w x y w w x y x w x y x w x y y w x y y w x y y w x y z w x y z w x y z w x y z y y x w";
        String[][] inStrings = {
                {"w", "x", "y"},
                {"x", "y"},
                {"y", "y"},
                {"z"},
                {"w", "x", "y", "y"}};
        String[][] out = {
                {"w", "x", "x", "y", "y", "y", "z", "z", "z", "z"},
                {"w", "x", "x", "y", "y", "y", "z", "z", "z", "z"},
                {"w", "w", "w", "x"},
                {"w", "w", "w", "y"},
                {"w", "w", "w"}};

        @SuppressWarnings("unchecked")
        ArrayList<String>[] in = new ArrayList[inStrings.length];
        for (int i = 0; i < inStrings.length; i++) {
            in[i] = new ArrayList<>(Arrays.asList(inStrings[i]));
        }

        for (int i = 0; i < in.length; i++) {
            int k = in[i].size();
            BaseMarkovModel markov = getModel(k);
            markov.trainText(testString);
            List<String> expected = Arrays.asList(out[i]);
            List<String> actual = markov.getFollows(in[i]);
            checkListUnordered(expected, actual);
        }
    }


    /**
     * @param expected elements of correct result
     * @param actual elements of generated result from student code
     * Helper method to verify tested elements in list
     */
    private static void checkListUnordered(List<String> expected, List<String> actual) {
        HashMap<String, Integer> expectedFreq = new HashMap<>();
        HashMap<String, Integer> actualFreq = new HashMap<>();
        for (String s : expected) {
            if (!expectedFreq.containsKey(s))
                expectedFreq.put(s, 0);
            expectedFreq.put(s, expectedFreq.get(s) + 1);
        }
        for (String s : actual) {
            if (!actualFreq.containsKey(s))
                actualFreq.put(s, 0);
            actualFreq.put(s, actualFreq.get(s) + 1);
        }

        for (String s : expectedFreq.keySet()) {
            if (!actualFreq.containsKey(s)) {
                fail("\nExpected element " + s + " does not exist");
            }
            assertEquals(expectedFreq.get(s), actualFreq.get(s));
        }
        for (String s : actualFreq.keySet()) {
            if (!expectedFreq.containsKey(s)) {
                fail("\nElement " + s + " in returned list should not exist");
            }
        }
    }

}
