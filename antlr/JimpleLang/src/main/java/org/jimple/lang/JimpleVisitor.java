// Generated from Jimple.g4 by ANTLR 4.13.2
package org.jimple.lang;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JimpleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JimpleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JimpleParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(JimpleParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(JimpleParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringExpr(JimpleParser.StringExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code doubleExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleExpr(JimpleParser.DoubleExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code idExp}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdExp(JimpleParser.IdExpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code booleanExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanExpr(JimpleParser.BooleanExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompExpr(JimpleParser.CompExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlusMinusExpr(JimpleParser.PlusMinusExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCallExpr(JimpleParser.FuncCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesisExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesisExpr(JimpleParser.ParenthesisExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mulDivExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivExpr(JimpleParser.MulDivExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numExpr}
	 * labeled alternative in {@link JimpleParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumExpr(JimpleParser.NumExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(JimpleParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(JimpleParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#compOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompOperator(JimpleParser.CompOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#println}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrintln(JimpleParser.PrintlnContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#return}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn(JimpleParser.ReturnContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#returnVoid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnVoid(JimpleParser.ReturnVoidContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#blockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStatement(JimpleParser.BlockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(JimpleParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(JimpleParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(JimpleParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#elseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseStatement(JimpleParser.ElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JimpleParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(JimpleParser.WhileStatementContext ctx);
}