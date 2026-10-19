import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import compsci201.Ignore;

/**
 * New version of BaseMarkovModel originated fall 2025. Changes from previous
 * semesters: replace WordGram with immutable List<String>. This
 * Base class is no longer abstract, but method getFollow is intended
 * to be @Override(n) in classes like SimpleMarkovModel and HashMarkovModel.
 * 
 * All versions maintain List<String> myWordSequence, but may
 * involve other state as well, e.g., HashMap in HashMarkovModel
 * 
 * Method processTraining is called from both trainDirectory (when done)
 * and trainText(when done) -- and should be specialized @Override
 * in classes that extend this class, e.g., in HashMarkovModel
 * will populate a map for O(1) getFollows
 * 
 * @author Owen Astrachan
 * @version 3.0 for Compsci 201, Fall 2025
 * @version 3.1 for Compsci 201, Fall 2026
 */

public class BaseMarkovModel {
    protected int myModelSize;
    protected Random myRandom;
    protected List<String> myWordSequence;
    protected static final int RANDOM_INIT = 1234;
    protected final static String START = "<START>";
    protected final static String END = "<END>";

    /**
     * initialize all state for this and submodels
     * @param size
     */
    public BaseMarkovModel(int size){
        myModelSize = size;
        myWordSequence = new ArrayList<>();
        myRandom = new Random();
    }

    /**
     * No param means use order-3 model
     */
    public BaseMarkovModel(){
        this(3);
    }

    /** 
     * Break text into space separated words/tokens, return
     * the list of words.
     * @param text is white-space delimited text
     * @return List<String> of separated words.
     */

    protected List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        String[] strs = text.split("\\s+");
        tokens.addAll(Arrays.asList(strs));
        return tokens;
    }

    /**
     * Update instance variable myWordSequence with 
     * words/tokens from parameter.  Add <START>* to
     * beginning of tokenized text and <END>* to end
     * as many times as the model of this MarkovModel
     * @param text is whitespace delimited text
     */
    protected void updateWordSequence(String text) {
        List<String> tokens = tokenize(text);
        List<String> padded = new ArrayList<>();
        
        for(int k=0; k < myModelSize; k++){
            padded.add(START);
        }

        padded.addAll(tokens);
        
        for(int k=0; k < myModelSize; k++){
            padded.add(END);
        }
        myWordSequence.addAll(padded);
    }

    /**
     * Add words/tokens to instance variable
     * by calling protected method and then train
     * @param text
     */
    public void trainText(String text) {
        updateWordSequence(text);
        processTraining();
    }

    /**
     * Returns number of words/tokens in this model
     * @return number of words/tokens in model
     */
    public int tokenSize(){
        return myWordSequence.size();
    }

    /**
     * Return this model's order
     * @return model's order
     */
    public int getOrder(){
        return myModelSize;
    }

    /**
     * Nothing here but print statement, specialize in subclasses
     */
    public void processTraining(){
        System.out.printf("size of final sequence = %d\n",myWordSequence.size());
    }

   /**
    * Read every file in folder specified by parameter and
    * update internal sequence with words/tokens from each file
    * @param dirName is name of folder containing text files
    * @throws IOException if reading fails
    */
    public void trainDirectory(String dirName) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(dirName))){
            List<Path> files = paths.filter(Files::isRegularFile)
                                    .collect(Collectors.toList());

            for(Path each : files) {
                String text = Files.readString(each);
                updateWordSequence(text);
            }
            processTraining();
        }       
    }

    /**
     * Return immutable List<String> containing myOrder words/tokens
     * chosen from random starting point
     * @return immutable List<String> containing myOrder strings
     */
     protected List<String> getRandomContext() {
        // 0 1 2 3 4 5 6: size = 7, 
        int index = myRandom.nextInt(myWordSequence.size()-myModelSize+1);
        List<String> seq = myWordSequence.subList(index, index+myModelSize);
        List<String> ret = List.copyOf(seq);
        return ret;
    }


    /**
     * Compute and return the number of different context (after training) for
     * this model.
     * @return number of distinct/different contexts of this model's size
     */
    public int differentContexts(){
        // 0 1 2 3 4, seq size 5, model size = 3, last sublist is (2,3,4) starts at 2
        HashSet<List<String>> set = new HashSet<>();
        for(int start=0; start <= myWordSequence.size()-myModelSize; start += 1){
            set.add(myWordSequence.subList(start,start+myModelSize));
        }
        return set.size();
    }

    /**
	 * Get a list of Strings containing all words that follow
	 * from context in the training text. Result may be an empty list.
	 * @param context is a List<String> to search for in the text
	 * @return List of words following context in training text.
	 * May be empty, but after training should not be if called
     * with a context that occurred in training, will be empty if
     * called with context not in training.
     * 
     * @apiNote Must be implemented to work after training in
     * subclasses.
	 */

    protected List<String> getFollows(List<String> context) {
        return Collections.emptyList();
    }

    /**
	 * Sets the random seed and initializes the random 
	 * number generator. Most implementing classes will
	 * produce the same randomText given the same 
	 * training text and random seed (True for Base
     * and Hash models)
	 * @param seed initial seed for java.util.Random
	 */
	public void setSeed(long seed) {
        myRandom.setSeed(seed);
    }

    /**
     * Returns an immutable copy of word sequence
     * used for training this model
     * @return the list of strings used for training
     */
    public List<String> getSequence(){
        return List.copyOf(myWordSequence);
    }

    /**
     * Creates a new, immutable context from existing context and what's
     * intended to be a word that follows that context, chosen at random. Does
     * this by essentially removing the first element of context and adding
     * new word to the end. 
     * @param context is list of Strings, intended to be current context
     * @param word is intended to be chosen from words that follow context
     * @return immutable List that represents new context
     */
    protected List<String> createNewContext(List<String> context, String word) {
        context = new ArrayList<>((context.subList(1,context.size())));
        context.add(word);
        context = List.copyOf(context);
        return context;
    }

    /**
     * Choose a random next string that follows context.
     * @param context is myOrder list of Strings 
     * @return a string that follows context, probabilistically
     */
    public String randomNextString(List<String> context){
        List<String> followList = getFollows(context);
        if (followList.size() == 0) {
            String err = String.format("\nzero size follows for %s",context);
            throw new RuntimeException(err);
        }
        int index = myRandom.nextInt(followList.size());
        String next = followList.get(index);
        return next;
    }

    /**
     * Generates random text using the order of this model
     * by first finding a random N-gram (N-1 tokens), 
     * then using that to find the next N-gram by choosing
     * next randomly from words that follow current 
     * @param size # words generated
     * @return space separated string of words
     */
    public String generate(int size) {
        List<String> context = getRandomContext();
        List<String> results = new ArrayList<>();
        for(int k=0; k < size; k++){
            if (context.get(0).equals(END)){ // early termination, found <END>           
                break;
            }
            String next = randomNextString(context);
            results.add(next);
            context = createNewContext(context, next);
        }
        return String.join(" ",results);
    }

    public static void main(String[] args) throws IOException {
        BaseMarkovModel mm = new BaseMarkovModel(2);
        String dirName = "data/shakespeare";
        int size = 500;
        mm.trainDirectory(dirName);
        String text = mm.generate(size);
        //System.out.println(text);
        System.out.printf("generated %d characters\n",text.length());
    }
}
