package qwiki.gui;

import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessor {

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
		String query = "not (this) or ((vanilla or (chocolate and not(strawberry)) or (test and fizzbuzz) and not(hello)))";
		String query2 = "aidan or (gainor and test)";
		QueryProcessor p = new QueryProcessor();
		String[] tokens = p.tokenizeQuery(query2);
		ExpressionNode<String> e = p.generateParseTree(tokens);
		System.out.println(e.toString());
	}
	
	
}
