package com.wonkglorg.utilitylib.config.lang.parser;

import java.util.ArrayList;
import java.util.List;

public final class BooleanConditionParser{
	
	private final List<String> tokens;
	private int pos = 0;
	
	public BooleanConditionParser(String input) {
		this.tokens = tokenize(input);
	}
	
	public boolean parse() {
		return parseOr();
	}
	
	private boolean parseOr() {
		boolean result = parseAnd();
		
		while(match("||")){
			result = result || parseAnd();
		}
		
		return result;
	}
	
	private boolean parseAnd() {
		boolean result = parseFactor();
		
		while(match("&&")){
			result = result && parseFactor();
		}
		
		return result;
	}
	
	private boolean parseFactor() {
		
		if(match("!")){
			return !parseFactor();
		}
		
		if(match("(")){
			boolean result = parseOr();
			expect(")");
			return result;
		}
		
		return parseValue();
	}
	
	private boolean parseValue() {
		
		String left = next();
		if(left == null){
			return false;
		}
		
		String op = peek();
		if(isOperator(op)){
			next();
			String right = next();
			if(right == null){
				return false;
			}
			return evaluate(left, op, right);
		}
		
		return Boolean.parseBoolean(left);
	}
	
	private boolean evaluate(String left, String op, String right) {
		
		return switch(op) {
			
			case "==" -> left.equals(right);
			case "!=" -> !left.equals(right);
			
			case ">" -> toDouble(left) > toDouble(right);
			case "<" -> toDouble(left) < toDouble(right);
			case ">=" -> toDouble(left) >= toDouble(right);
			case "<=" -> toDouble(left) <= toDouble(right);
			
			default -> false;
		};
	}
	
	private double toDouble(String value) {
		try{
			return Double.parseDouble(value);
		} catch(Exception ex){
			return 0;
		}
	}
	
	private boolean isOperator(String token) {
		return token.equals("==") || token.equals("!=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
	}
	
	private boolean match(String expected) {
		if(peek().equals(expected)){
			pos++;
			return true;
		}
		return false;
	}
	
	private void expect(String expected) {
		if(!match(expected)){
			throw new IllegalStateException("Expected '" + expected + "' but found '" + peek() + "'");
		}
	}
	
	private String peek() {
		return pos < tokens.size() ? tokens.get(pos) : "";
	}
	
	private String next() {
		return pos < tokens.size() ? tokens.get(pos++) : null;
	}
	
	private List<String> tokenize(String input) {
		return new ArrayList<>(List.of(input.replace("(", " ( ")
											.replace(")", " ) ")
											.replace("&&", " && ")
											.replace("||", " || ")
											.replace("!", " ! ")
											.trim()
											.split("\\s+")));
	}
}