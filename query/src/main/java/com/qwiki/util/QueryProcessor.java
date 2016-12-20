package com.qwiki.util;

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

import com.qwiki.util.StringIntegerList.StringInteger;

public class QueryProcessor {
	private MapFileReader hdfsReader;
	private final String lemmaPath = "wordToLemMap";
	private final String docPath = "inv-wiki-map";
	
	public final static List<String> OPERATORS = Arrays.asList("and", "or", "not");
	Pattern tokenizerPattern = Pattern.compile("[\\\"\\(\\)\\p{L}]+");
	
	public QueryProcessor() throws IOException {
		hdfsReader = new MapFileReader();
	}
	
	public QueryProcessor(MapFileReader r) throws IOException {
		hdfsReader = r;
	}
	
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
		
		/*
		 * Evaluate the expression tree with post order traversal
		 */
		public HashMap<String, IntArrayPair> evaluateExpressionTree(HashMap<String, HashMap<String, IntArrayPair>> invertedIndexMapping) {
			if (this instanceof BinaryOperatorNode) {
				BinaryOperatorNode<E> binOpNode = (BinaryOperatorNode<E>) this;
				// Traverse left
				HashMap<String, IntArrayPair> leftOperandHash = binOpNode.getLeftOperand().evaluateExpressionTree(invertedIndexMapping);
				// Traverse right
				HashMap<String, IntArrayPair> rightOperandHash = binOpNode.getRightOperand().evaluateExpressionTree(invertedIndexMapping);
				
				// Our hashmap of doc_id to frequency pairs for this binary operator is the left operand's hashmap
				// This is arbitrary (we could have made it right operand's hashmap), but now lets "merge" the left hashmap with right
				for (String docId : rightOperandHash.keySet()) {
					IntArrayPair rightValue = rightOperandHash.get(docId);
					int rightFrequency = rightValue.frequency;
					// If the two hashmaps from the operand node contain the same document, "merge" the values following the semantics of the logical operator
					// In our case AND -> multiplication, OR -> addition
					if (leftOperandHash.containsKey(docId)) {
						IntArrayPair leftValue = leftOperandHash.get(docId);
						int leftFrequency = leftValue.frequency;
						int[] mergedTokenPositions = mergeArrays(leftValue.posArray, rightValue.posArray);
						if (binOpNode.getOperatorAsString().equals("and")) {
							leftOperandHash.put(docId, new IntArrayPair(leftFrequency * rightFrequency, mergedTokenPositions));
						} else if (binOpNode.getOperatorAsString().equals("or")) {
							leftOperandHash.put(docId, new IntArrayPair(leftFrequency + rightFrequency, mergedTokenPositions));
						}
					} else {
						// Means left and right operands do not have this document_id in common
						leftOperandHash.put(docId, new IntArrayPair(rightFrequency, rightValue.posArray));
					}
				}
				return leftOperandHash;
				
			} else if (this instanceof UnaryOperatorNode) {
				UnaryOperatorNode<E> unOpNode = (UnaryOperatorNode<E>) this;
				// Just traverse child since only 1 operand
				HashMap<String, IntArrayPair> operandHash = unOpNode.getOperand().evaluateExpressionTree(invertedIndexMapping);

				for (String docId : operandHash.keySet()) {
					IntArrayPair value = operandHash.get(docId);
					int frequency = value.frequency;
					// For the not operator, negate every frequency value
					if (unOpNode.getOperatorAsString().equals("not")) 
						operandHash.put(docId, new IntArrayPair(frequency * -1, new int[] {}));
				}
				return operandHash;
				
			} else if (this instanceof OperandNode) {
				OperandNode<E> operandNode = (OperandNode<E>) this;
				String word = operandNode.getOperandAsString();
				HashMap<String, IntArrayPair> mapping = invertedIndexMapping.get(word);
				return mapping;
				
			} else if (this instanceof N_aryOperatorNode) {
				N_aryOperatorNode<E> n_aryNode = (N_aryOperatorNode<E>) this;
				String operator = n_aryNode.getOperatorAsString();

				if (operator.equals("pand")) {
					for (OperandNode<E> operand : n_aryNode.getChildren()) {
						String word = operand.getOperandAsString();
						HashMap<String, IntArrayPair> mapping = invertedIndexMapping.get(word);
						
					}
				}
			}
			return null;
		}
	}
	
	// Leaf nodes (just a container for a value)
	public class OperandNode<E> extends ExpressionNode<E> {
		
		public OperandNode(E value) {
			super(value, null, null);
		}
		
		public String getOperandAsString() {
			return value.toString();
		}
	}
	
	// Binary operators are currently the AND and OR operators
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
	
	// This is a unique class which was created to solve the phrase search problem
	// Unlike other nodes, this can have N operand node children
	public class N_aryOperatorNode<E> extends ExpressionNode<E> {
		private List<OperandNode<E>> children;
		
		public N_aryOperatorNode(E operation, List<OperandNode<E>> children) {
			// No left or right children, instead we have N children
			super(operation, null, null);
			this.children = children;
		}
		
		public String getOperatorAsString() {
			return value.toString();
		}
		
		public List<OperandNode<E>> getChildren() {
			return children;
		}
	}
	
	// A unary operator can be the NOT operator
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
		Matcher m = tokenizerPattern.matcher(inputQuery);
		// Separate parenthesis, so "(query)" --> ["(", "query", ")"]
		while (m.find()) {
			String token = inputQuery.substring(m.start(), m.end()).toLowerCase();
			// Not operation can be formated like "not(java)", so we must make exception to manually tokenize this
			if (token.startsWith("not(")) {
				tokenizedQuery.add("not");
				token = token.substring(3);
			}
			
			boolean foundEndQuotation = false;
			
			// Beginning of phrase syntax will start with " character
			if (token.startsWith("\"") && token.length() > 1) {
				tokenizedQuery.add("\"");
				token = token.substring(1);
			}
			
			// Ending of phrase syntax will end with " character
			if (token.endsWith("\"") && token.length() > 1) {
				token = token.substring(0, token.length()-1);
				foundEndQuotation = true;
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
			
			if (foundEndQuotation) {
				tokenizedQuery.add("\"");
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
			if (!OPERATORS.contains(token) && !token.equals("(") && !token.equals(")") && !token.equals("\"")) {
				words.add(token);
			}
		}
		return words;
	}
	
	
	/*
	 * Take in output from the getAllWords function, and get the corresponding frequency lists from the inverted index
	 * For example, if we have two words (operands) in our query like [apples, oranges],
	 * we will return a hash map like {"apples" -> [(doc1, 5), (doc2, 1)], "oranges" -> [(doc4, 2), (doc1, 3)]}
	 * 
	 * UPDATE!!!: Now we take in indices from document for search highlighting, and just get indices array length for the frequency value
	 */
	public HashMap<String, HashMap<String, IntArrayPair>> getStringIntegerListPairs(List<String> words) throws IOException {
		HashMap<String, HashMap<String, IntArrayPair>> allWordsHash = new HashMap<String, HashMap<String, IntArrayPair>>();
		for (String word : words) {
			allWordsHash.put(word, searchInvertedIndex(word));
		}
		return allWordsHash;
	}
	
	/*
	 * This will retrieve the the value for the word in the inverted index on HDFS
	 * It is a hashmap of doc_id -> array of indices of token in doc + frequency value
	 */
	public HashMap<String, IntArrayPair> searchInvertedIndex(String word) throws IOException {
		StringIntegerList sil = new StringIntegerList();
		sil.readFromString(hdfsReader.getValue(word));
		return (HashMap<String, IntArrayPair>) sil.getFreqMap();
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
		
		// a phrase search syntax is surrounded by quotations
		if (tokenizedQuery[startIndex].equals("\"") && tokenizedQuery[endIndex].equals("\"")) {
			List<OperandNode<String>> phraseList = new LinkedList<OperandNode<String>>();
			for (int tokenInPhraseIndex = startIndex + 1; tokenInPhraseIndex < endIndex-1; tokenInPhraseIndex++) {
				phraseList.add(new OperandNode<String>(tokenizedQuery[tokenInPhraseIndex]));
			}
			// a phrase search will be represented as a operator I made up called "positional and"
			// "positional and" behaves similar arithmetically to the binary and operator
			// except it takes into account positions and can take in N operands (the operand child list) as arguments
			return new N_aryOperatorNode<String>("pand", phraseList);
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
	
	private List<QueryData> sortDocMap(HashMap<String, IntArrayPair> map, List<String> tokens){
		List<QueryData> l = new ArrayList<QueryData>();
		for(String key : map.keySet()){
			IntArrayPair pair = map.get(key);
			System.out.println(Arrays.toString(pair.posArray));
			int[] sampleBounds = getRangeOfAreaToHighlight(pair.posArray);
			l.add(new QueryData(key, pair.frequency, sampleBounds, tokens));
		}
		Collections.sort(l);
		return l;
	}
	
	public List<QueryData> evaluateQuery(String query) throws IOException {
		String[] tokens = tokenizeQuery(query);
		ExpressionNode<String> e = generateParseTree(tokens);
		List<String> words = getAllWords(tokens);
		HashMap<String, HashMap<String, IntArrayPair>> invertedIndexSegment = getStringIntegerListPairs(words);
		HashMap<String, IntArrayPair> evaluationResult = e.evaluateExpressionTree(invertedIndexSegment);
		
		List<QueryData> topSearchResults = sortDocMap(evaluationResult, words);
		return topSearchResults;
	}
	
	// Sliding window approach to get area to highlight
	public int[] getRangeOfAreaToHighlight(int[] posArray) {
		if (posArray.length == 1 || posArray.length == 0) {
			return null;
		}
		int maxLowerIndex = 0;
		int maxUpperIndex = 0;
		
		int curLowerIndex = 0;
		int curUpperIndex = 0;
		
		int curItemsInWindow = 0;
		int maxItemsInWindow = 0;
		while (curUpperIndex < posArray.length) {
			// If the two positions in our sliding window differ by 400 or more, move lower bound up 1 and remove 1 item from window
			if ((posArray[curUpperIndex] - posArray[curLowerIndex]) > 400) {
				curLowerIndex++;
				curItemsInWindow--;
			} else {
				curItemsInWindow++;
				if (curItemsInWindow > maxItemsInWindow) {
					maxItemsInWindow = curItemsInWindow;
					maxLowerIndex = curLowerIndex;
					maxUpperIndex = curUpperIndex;
				}
				curUpperIndex++;
			}
			
		}
		return new int[] {posArray[maxLowerIndex], posArray[maxUpperIndex]};
	}
	
	// Merge two sorted arrays a1 and a2
	public static int[] mergeArrays(int[] a1, int[] a2) {
		int[] merged = new int[a1.length + a2.length];
		int i = 0, j = 0, k = 0;
		
		while (i < a1.length && j < a2.length) {
		    if (a1[i] < a2[j]){
		    	merged[k++] = a1[i++];
		    } else {
		    	merged[k++] = a2[j++];               
		    }
		}
		
		while (i < a1.length) {
			merged[k++] = a1[i++];
		}
		
		while (j < a2.length) {
		    merged[k++] = a2[j++];
		}
		return merged;
	}
	
	public static void main(String[] args) throws IOException {
		String query = "aidan";
		QueryProcessor p = new QueryProcessor();
		ExpressionNode<String> e = p.generateParseTree(p.tokenizeQuery(query));
		
		System.out.println(Arrays.toString(p.evaluateQuery(query).get(0).getSampleArea()));
		System.out.println(Arrays.toString(p.getRangeOfAreaToHighlight(new int[] {0, 3, 300, 500, 505})));
	}
}
