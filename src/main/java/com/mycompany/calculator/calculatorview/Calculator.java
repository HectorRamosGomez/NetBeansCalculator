package com.mycompany.calculator.calculatorview;

import java.util.*;
import java.util.regex.Pattern;

public class Calculator {
    
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = new HashMap<>();
    static {
        OPERATOR_PRECEDENCE.put("+", 1);
        OPERATOR_PRECEDENCE.put("-", 1);
        OPERATOR_PRECEDENCE.put("*", 2);
        OPERATOR_PRECEDENCE.put("/", 2);
        OPERATOR_PRECEDENCE.put("MOD", 2);
        OPERATOR_PRECEDENCE.put("%", 3);
        
        //Using the procedence the program will know which operator has prefernece in order to excute the operations
    }
    
    // Variable where we can save the last result
    private double lastResult = 0;
    
    public Calculator() {
    }
    
    // This class will be used for returning the results 
    public double evaluate(String expression) {
        // Replace the symbols into mathematic operators
        expression = expression.replace("÷", "/")
                              .replace("X", "*")
                              .replace(",", ".")
                              .replace("Ans", String.valueOf(lastResult));
        
        System.out.println("Expresión después de reemplazos: " + expression); 
        
        try {
            List<String> tokens = tokenize(expression);
            System.out.println("Tokens: " + tokens); 
            
            List<String> postfix = shuntingYard(tokens);
            System.out.println("Postfix: " + postfix); 
            
            double result = evaluatePostfix(postfix);
            lastResult = result; // this will be used for saving the last result turining it into "Ans"
            return result;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); 
            e.printStackTrace(); 
            throw new IllegalArgumentException("Error al evaluar la expresión: " + e.getMessage());
        }
    }
    
    // this class tokenize the expresions into numbers, operatios and parenteheses
    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                currentToken.append(c);
            } else if (isOperator(Character.toString(c)) || c == '(' || c == ')') {
                // this "if" will happen if there id a pending numeric token first  
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(Character.toString(c));
            } else if (Character.isWhitespace(c)) {
                // Ignore noncharacter spaces
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                // This will be used if u are using the "MOD" operation
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                tokens.add(Character.toString(c));
            }
        }
        
        // Adding the last token if its exist
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        
        return tokens;
    }
    
    /**
     * Implementa el algoritmo Shunting Yard para convertir infix a postfix
     */
    private List<String> shuntingYard(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        
        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && 
                       !operatorStack.peek().equals("(") && 
                       hasHigherPrecedence(operatorStack.peek(), token)) {
                    output.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    output.add(operatorStack.pop());
                }
                if (operatorStack.isEmpty()) {
                    throw new IllegalArgumentException("Paréntesis no balanceados");
                }
                operatorStack.pop(); // Remover el "("
            } else {
                throw new IllegalArgumentException("Token no válido: " + token);
            }
        }
        
        // Vaciar el stack de operadores
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().equals("(")) {
                throw new IllegalArgumentException("Paréntesis no balanceados");
            }
            output.add(operatorStack.pop());
        }
        
        return output;
    }
    
    /**
     * Evalúa una expresión en notación postfix
     */
    private double evaluatePostfix(List<String> postfix) {
        Stack<Double> stack = new Stack<>();
        
        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Expresión inválida - faltan operandos");
                }
                double b = stack.pop();
                double a = stack.pop();
                double result = applyOperator(token, a, b);
                stack.push(result);
            }
        }
        
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Expresión inválida");
        }
        
        return stack.pop();
    }
    
    /**
     * Aplica un operador a dos operandos
     */
    private double applyOperator(String operator, double a, double b) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("División por cero");
                }
                return a / b;
            case "%":
                return a * (b / 100.0);
            case "MOD":
                return a % b;
            default:
                throw new IllegalArgumentException("Operador no válido: " + operator);
        }
    }
    
    /**
     * Verifica si un token es un número
     */
    private boolean isNumber(String token) {
        return Pattern.matches("-?\\d+(\\.\\d+)?", token);
    }
    
    /**
     * Verifica si un token es un operador
     */
    private boolean isOperator(String token) {
        return OPERATOR_PRECEDENCE.containsKey(token);
    }
    
    /**
     * Compara la precedencia de dos operadores
     */
    private boolean hasHigherPrecedence(String op1, String op2) {
        int prec1 = OPERATOR_PRECEDENCE.getOrDefault(op1, 0);
        int prec2 = OPERATOR_PRECEDENCE.getOrDefault(op2, 0);
        return prec1 > prec2;
    }
    
    /**
     * Limpia el último resultado
     */
    public void clear() {
        lastResult = 0;
    }
    
    /**
     * Obtiene el último resultado
     */
    public double getLastResult() {
        return lastResult;
    }
}