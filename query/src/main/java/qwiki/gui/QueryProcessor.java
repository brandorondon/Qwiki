package qwiki.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import qwiki.gui.StringIntegerList.StringInteger;

public class QueryProcessor {
	private MapFileReader hdfsReader = new MapFileReader();
	private final String lemmaPath = "wordToLemMap";
	private final String docPath = "inv-wiki-map";
	
	public final static List<String> OPERATORS = Arrays.asList("and", "or", "not");
	Pattern p = Pattern.compile("[\\(\\)\\p{L}]+");
	
	public class ExpressionNode<E> {
		// Value = either query string operand or the action of operation, like AND
		protected E value;
		protected ExpressionNode<E> left;
		protected ExpressionNode<E> right;
		
		public ExpressionNode(E value, ExpressionNode<E> left, ExpressionNode<E> right) {
			this.value = value;
			this.left = left;
			this.right = right;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Queue<ExpressionNode<E>> levelOfTree = new LinkedList<ExpressionNode<E>>();
			levelOfTree.add(this);
			while (!levelOfTree.isEmpty()) {
				Queue<ExpressionNode<E>> tempLevel = new LinkedList<ExpressionNode<E>>();
				int queueSize = levelOfTree.size();
				for (int i = 0; i < queueSize; i++) {
					ExpressionNode<E> currNode = levelOfTree.poll();
					if (currNode == null) {
						sb.append("\t");
					} else if (currNode instanceof OperandNode) {
						OperandNode<E> operandNode = (OperandNode<E>) currNode;
						sb.append(operandNode.getOperandAsString() + "\t");
					} else {
						if (currNode instanceof UnaryOperatorNode) {
							UnaryOperatorNode<E> unOpNode = (UnaryOperatorNode<E>) currNode;
							sb.append(unOpNode.getOperatorAsString() + "\t");
							tempLevel.add(unOpNode.getOperand());
						} else {
							BinaryOperatorNode<E> binOpNode = (BinaryOperatorNode<E>) currNode;
							sb.append(binOpNode.getOperatorAsString() + "\t");
							tempLevel.add(binOpNode.getLeftOperand());
							tempLevel.add(binOpNode.getRightOperand());
						}
					}
				}
				levelOfTree = tempLevel;
				sb.append("\n");
			}
			return sb.toString();
		}
		
		private class DocAndFreq implements Comparable{
			private String docId;
			private Integer freq;
			public DocAndFreq(String docId, Integer freq){
				this.docId = docId;
				this.freq = freq;
			}
			
			public int compareTo(Object o){
				DocAndFreq other = (DocAndFreq) o;		
				if(other.getFreq() > this.freq){
					return 1;
				} else if (other.getFreq() < this.freq){
					return -1;
				} else 
					return 0;
			}
			
			public String getDocId(){
				return this.docId;
			}
			
			public Integer getFreq(){
				return this.freq;
			}
		}
		
		private List<String> sortDocMap(HashMap<String, Integer> map){
			List<String> sortedDocs = new ArrayList<String>();
			List<DocAndFreq> l = new ArrayList<DocAndFreq>();
			for(String key : map.keySet()){
				l.add(new DocAndFreq(key, map.get(key)));
			}
			Collections.sort(l);
			for(DocAndFreq df :l){
				sortedDocs.add(df.getDocId());
			}
			return sortedDocs;
		}
		
		//TODO:
		/*
		 * Evaluate the expression tree with post order traversal
		 */
		public HashMap<String, Integer> evaluateExpressionTree(HashMap<String, HashMap<String, Integer>> invertedIndexMapping) {
			if (this instanceof BinaryOperatorNode) {
				BinaryOperatorNode<E> binOpNode = (BinaryOperatorNode<E>) this;
				// Traverse left
				HashMap<String, Integer> leftOperandHash = binOpNode.getLeftOperand().evaluateExpressionTree(invertedIndexMapping);
				// Traverse right
				HashMap<String, Integer> rightOperandHash = binOpNode.getRightOperand().evaluateExpressionTree(invertedIndexMapping);
				
				// Our hashmap of doc_id to frequency pairs for this binary operator is the left operand's hashmap
				// This is arbitrary (we could have made it right operand's hashmap), but now lets "merge" the left hashmap with right
				for (String docId : rightOperandHash.keySet()) {
					Integer rightFrequency = rightOperandHash.get(docId);
					// If the two hashmaps from the operand node contain the same document, "merge" the values following the semantics of the logical operator
					// In our case AND -> multiplication, OR -> addition
					if (leftOperandHash.containsKey(docId)) {
						Integer leftFrequency = leftOperandHash.get(docId);
						if (binOpNode.getOperatorAsString().equals("and")) {
							leftOperandHash.put(docId, (Integer) (leftFrequency * rightFrequency));
						} else if (binOpNode.getOperatorAsString().equals("or")) {
							leftOperandHash.put(docId, (Integer) (leftFrequency + rightFrequency));
						}
					} else {
						// Means left and right operands do not have this document_id in common
						leftOperandHash.put(docId, rightFrequency);
					}
				}
				return leftOperandHash;
				
			} else if (this instanceof UnaryOperatorNode) {
				UnaryOperatorNode<E> unOpNode = (UnaryOperatorNode<E>) this;
				// Just traverse child since only 1 operand
				HashMap<String, Integer> operandHash = unOpNode.getOperand().evaluateExpressionTree(invertedIndexMapping);

				for (String docId : operandHash.keySet()) {
					Integer value = operandHash.get(docId);
					// For the not operator, negate every frequency value
					if (unOpNode.getOperatorAsString().equals("not")) 
						operandHash.put(docId, value * -1);
				}
				return operandHash;
				
			} else if (this instanceof OperandNode) {
				OperandNode<E> operandNode = (OperandNode<E>) this;
				String word = operandNode.getOperandAsString();
				return invertedIndexMapping.get(word);
			}
			return null;
		}
	}

	public class OperandNode<E> extends ExpressionNode<E> {
		
		public OperandNode(E value) {
			super(value, null, null);
		}
		
		public String getOperandAsString() {
			return value.toString();
		}
	}
	
	public class BinaryOperatorNode<E> extends ExpressionNode<E> {

		public BinaryOperatorNode(E operation, ExpressionNode<E> left, ExpressionNode<E> right) {
			super(operation, left, right);
		}
		
		public String getOperatorAsString() {
			return value.toString();
		}
		
		public ExpressionNode<E> getLeftOperand() {
			return left;
		}
		
		public ExpressionNode<E> getRightOperand() {
			return right;
		}
	}
	
	public class UnaryOperatorNode<E> extends ExpressionNode<E> {

		public UnaryOperatorNode(E operation, ExpressionNode<E> operand) {
			super(operation, operand, null);
		}
		
		public String getOperatorAsString() {
			return value.toString();
		}
		
		public ExpressionNode<E> getOperand() {
			return left;
		}
	}
	
	/*
	 * Takes in a string and tokenize it 
	 * Also extracts parenthesis and makes them into tokens as well
	 */
			
	public String[] tokenizeQuery(String inputQuery) {
		LinkedList<String> tokenizedQuery = new LinkedList<String>();
		Matcher m = p.matcher(inputQuery);
		// Separate parenthesis, so "(query)" --> ["(", "query", ")"]
		while (m.find()) {
			String token = inputQuery.substring(m.start(), m.end()).toLowerCase();
			// Not operation can be formated like "not(java)", so we must make exception to manually tokenize this
			if (token.startsWith("not(")) {
				tokenizedQuery.add("not");
				token = token.substring(3);
			}
			// Manually go through each char in token and look for parens
			int startOfTokenBody = 0;
			int endOfTokenBody = token.length();
			int endParenCount = 0;
			boolean foundFirstClosingParen = false;
			for (int i = 0; i < token.length(); i++) {
				if (token.charAt(i) == '(') {
					startOfTokenBody = i+1;
					tokenizedQuery.add("(");
				} else if (token.charAt(i) == ')') {
					if (!foundFirstClosingParen) {
						endOfTokenBody = i;
						foundFirstClosingParen = true;
					}
					endParenCount++;
				}
			}
			tokenizedQuery.add(token.substring(startOfTokenBody, endOfTokenBody));
			for (int j = 0; j < endParenCount; j++) {
				tokenizedQuery.add(")");
			}
		}
		
		String[] outputArray = new String[tokenizedQuery.size()];
		int count = 0;
		for (String s : tokenizedQuery) {
			outputArray[count] = s;
			count++;
		}
		return outputArray;
	}
	
	/*
	 * Take in array of tokenized query and return list of all keywords in the query
	 * Basically, it retrieves all operands from the tokenized query (remove logical operators and parens)
	 */
	public List<String> getAllWords(String[] tokenizedQuery) {
		List<String> words = new LinkedList<String>();
		for (String token : tokenizedQuery) {
			if (!OPERATORS.contains(token) && !token.equals("(") && !token.equals(")")) {
				words.add(token);
			}
		}
		return words;
	}
	
	
	/*
	 * Take in output from the getAllWords function, and get the corresponding frequency lists from the inverted index
	 * For example, if we have two words (operands) in our query like [apples, oranges],
	 * we will return a hash map like {"apples" -> [(doc1, 5), (doc2, 1)], "oranges" -> [(doc4, 2), (doc1, 3)]}
	 */
	public HashMap<String, HashMap<String, Integer>> getStringIntegerListPairs(List<String> words) throws IOException {
		HashMap<String, HashMap<String, Integer>> allWordsHash = new HashMap<String, HashMap<String, Integer>>();
		for (String word : words) {
			allWordsHash.put(word, searchInvertedIndex(word));
		}
		return allWordsHash;
	}
	
	/* TODO:
	 * This will retrieve the the value for the word in the inverted index on HDFS
	 */
	public HashMap<String, Integer> searchInvertedIndex(String word) throws IOException {
		StringIntegerList sil = new StringIntegerList();
		sil.readFromString(hdfsReader.getValue(word, this.docPath));
		return (HashMap) sil.getFreqMap();
	}
	
	public ExpressionNode<String> generateParseTree(String[] tokenizedQuery, int startIndex, int endIndex) {
		int openingParenCount = 0;
		int closingParenCount = 0;
		int operatorIndex = -1;
		boolean tryToFindOperator = true;

		for (int i = startIndex; i <= endIndex; i++) {
			String token = tokenizedQuery[i];
			// find the "middle" operator that will be parent, this will be the least nested operator
			if (OPERATORS.contains(token)) {
				// the parent operator is the one least nested to the left
				// however, NOT will have lower precedence over OR or AND operations if operations on same level
				int depth = openingParenCount - closingParenCount;
				if (depth == 0 && tryToFindOperator) {
					operatorIndex = i; // this is index of current operator (AND, OR, NOT) at this level
					if (!tokenizedQuery[operatorIndex].equals("not")) { // if we find a AND or OR at depth = 0...
						tryToFindOperator = false;  // stop trying to find operator to make parent
					} 
				}
			}
			
			if (token.equals("(")) {
				openingParenCount++;
			} else if (token.equals(")")) {
				closingParenCount++;
			}
	
		}

		if (openingParenCount != closingParenCount) {
			// throw exception here
		} 
		
		// Means we did not find operator or there are useless parenthesis around expression
		if (operatorIndex == -1 || (openingParenCount == 1 && tokenizedQuery[startIndex].equals("(") && tokenizedQuery[endIndex].equals(")"))) {
			// if we found useless parens, recur on it
			// this means "(this or that)" --> "this or that"
			if (openingParenCount > 0) {
				return generateParseTree(tokenizedQuery, startIndex+1, endIndex-1);
			}
			// else, just return leaf operand node
			return new OperandNode<String>(tokenizedQuery[startIndex]);
		}
		
		if (tokenizedQuery[operatorIndex].equals("not")) {
			return new UnaryOperatorNode<String>("not", generateParseTree(tokenizedQuery, operatorIndex+1, endIndex));
		} else {
			return new BinaryOperatorNode<String>(tokenizedQuery[operatorIndex], 
					generateParseTree(tokenizedQuery, startIndex, operatorIndex-1),
					generateParseTree(tokenizedQuery, operatorIndex+1, endIndex));
		}
	}
	
	public ExpressionNode<String> generateParseTree(String[] tokenizedQuery) {
		return generateParseTree(tokenizedQuery, 0, tokenizedQuery.length-1);
	}
	
	
	public static void main(String[] args) {
		String query = "(oranges or grapes) and not(apples)";
		HashMap<String, Integer> applesHM = new HashMap<String, Integer>();
		applesHM.put("doc1", 5);
		applesHM.put("doc2", 1);
		HashMap<String, Integer> orangesHM = new HashMap<String, Integer>();
		orangesHM.put("doc1", 2);
		orangesHM.put("doc2", 4);
		HashMap<String, Integer> grapesHM = new HashMap<String, Integer>();
		grapesHM.put("doc1", 3);
		grapesHM.put("doc3", 1);
		grapesHM.put("doc4", 10);
		HashMap<String, HashMap<String, Integer>> invertedIndex = new HashMap<String, HashMap<String, Integer>>();
		invertedIndex.put("apples", applesHM);
		invertedIndex.put("oranges", orangesHM);
		invertedIndex.put("grapes", grapesHM);
		QueryProcessor p = new QueryProcessor();
		String[] tokens = p.tokenizeQuery(query);
		ExpressionNode<String> e = p.generateParseTree(tokens);
		HashMap<String, Integer> res = e.evaluateExpressionTree(invertedIndex);
		
		for (String key : res.keySet()) {
			System.out.println("key = " + key + " : value = " + res.get(key));
			
		}
		
	}
}
