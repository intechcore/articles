// Generated from Jimple.g4 by ANTLR 4.12.0
package org.jimple.lang;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JimpleParser}.
 */
public interface JimpleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JimpleParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(JimpleParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(JimpleParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JimpleParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JimpleParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(JimpleParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(JimpleParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doubleExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDoubleExpr(JimpleParser.DoubleExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doubleExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDoubleExpr(JimpleParser.DoubleExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code idExp}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIdExp(JimpleParser.IdExpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code idExp}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIdExp(JimpleParser.IdExpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(JimpleParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(JimpleParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPlusMinusExpr(JimpleParser.PlusMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPlusMinusExpr(JimpleParser.PlusMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFuncCallExpr(JimpleParser.FuncCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFuncCallExpr(JimpleParser.FuncCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesisExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisExpr(JimpleParser.ParenthesisExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesisExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisExpr(JimpleParser.ParenthesisExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mulDivExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMulDivExpr(JimpleParser.MulDivExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mulDivExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMulDivExpr(JimpleParser.MulDivExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNumExpr(JimpleParser.NumExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNumExpr(JimpleParser.NumExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(JimpleParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(JimpleParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(JimpleParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(JimpleParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#compOperator}.
	 * @param ctx the parse tree
	 */
	void enterCompOperator(JimpleParser.CompOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#compOperator}.
	 * @param ctx the parse tree
	 */
	void exitCompOperator(JimpleParser.CompOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#println}.
	 * @param ctx the parse tree
	 */
	void enterPrintln(JimpleParser.PrintlnContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#println}.
	 * @param ctx the parse tree
	 */
	void exitPrintln(JimpleParser.PrintlnContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#return}.
	 * @param ctx the parse tree
	 */
	void enterReturn(JimpleParser.ReturnContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#return}.
	 * @param ctx the parse tree
	 */
	void exitReturn(JimpleParser.ReturnContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(JimpleParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(JimpleParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(JimpleParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(JimpleParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(JimpleParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(JimpleParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(JimpleParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(JimpleParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JimpleParser#elseStatement}.
	 * @param ctx the parse tree
	 */
	void enterElseStatement(JimpleParser.ElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JimpleParser#elseStatement}.
	 * @param ctx the parse tree
	 */
	void exitElseStatement(JimpleParser.ElseStatementContext ctx);
}