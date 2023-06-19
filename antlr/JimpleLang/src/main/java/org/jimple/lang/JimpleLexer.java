// Generated from Jimple.g4 by ANTLR 4.12.0
package org.jimple.lang;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class JimpleLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, IDENTIFIER=12, NUMBER=13, DOUBLE_NUMBER=14, STRING_LITERAL=15, 
		ASTERISK=16, SLASH=17, PLUS=18, MINUS=19, ASSIGN=20, EQUAL=21, NOT_EQUAL=22, 
		LESS=23, LESS_OR_EQUAL=24, GREATER=25, GREATER_OR_EQUAL=26, SPACE=27, 
		LINE_COMMENT=28;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "IDENTIFIER", "NUMBER", "DOUBLE_NUMBER", "STRING_LITERAL", 
			"ASTERISK", "SLASH", "PLUS", "MINUS", "ASSIGN", "EQUAL", "NOT_EQUAL", 
			"LESS", "LESS_OR_EQUAL", "GREATER", "GREATER_OR_EQUAL", "SPACE", "LINE_COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'var'", "'println'", "'return'", "'{'", "'}'", "','", 
			"'fun'", "'if'", "'else'", null, null, null, null, "'*'", "'/'", "'+'", 
			"'-'", "'='", "'=='", "'!='", "'<'", "'<='", "'>'", "'>='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"IDENTIFIER", "NUMBER", "DOUBLE_NUMBER", "STRING_LITERAL", "ASTERISK", 
			"SLASH", "PLUS", "MINUS", "ASSIGN", "EQUAL", "NOT_EQUAL", "LESS", "LESS_OR_EQUAL", 
			"GREATER", "GREATER_OR_EQUAL", "SPACE", "LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JimpleLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Jimple.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u001c\u00a7\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0005\u000b"+
		"e\b\u000b\n\u000b\f\u000bh\t\u000b\u0001\f\u0004\fk\b\f\u000b\f\f\fl\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0005\u000eu\b\u000e"+
		"\n\u000e\f\u000ex\t\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0004\u001a\u0097\b\u001a\u000b\u001a\f\u001a\u0098\u0001"+
		"\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005"+
		"\u001b\u00a1\b\u001b\n\u001b\f\u001b\u00a4\t\u001b\u0001\u001b\u0001\u001b"+
		"\u0000\u0000\u001c\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019"+
		"\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015"+
		"+\u0016-\u0017/\u00181\u00193\u001a5\u001b7\u001c\u0001\u0000\u0006\u0003"+
		"\u0000AZ__az\u0004\u000009AZ__az\u0001\u000009\u0001\u0000\"\"\u0003\u0000"+
		"\t\n\r\r  \u0002\u0000\n\n\r\r\u00ab\u0000\u0001\u0001\u0000\u0000\u0000"+
		"\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000"+
		"\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000"+
		"\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f"+
		"\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013"+
		"\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017"+
		"\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b"+
		"\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f"+
		"\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000"+
		"\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000"+
		"\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000"+
		"-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u00001\u0001"+
		"\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005\u0001\u0000\u0000"+
		"\u0000\u00007\u0001\u0000\u0000\u0000\u00019\u0001\u0000\u0000\u0000\u0003"+
		";\u0001\u0000\u0000\u0000\u0005=\u0001\u0000\u0000\u0000\u0007A\u0001"+
		"\u0000\u0000\u0000\tI\u0001\u0000\u0000\u0000\u000bP\u0001\u0000\u0000"+
		"\u0000\rR\u0001\u0000\u0000\u0000\u000fT\u0001\u0000\u0000\u0000\u0011"+
		"V\u0001\u0000\u0000\u0000\u0013Z\u0001\u0000\u0000\u0000\u0015]\u0001"+
		"\u0000\u0000\u0000\u0017b\u0001\u0000\u0000\u0000\u0019j\u0001\u0000\u0000"+
		"\u0000\u001bn\u0001\u0000\u0000\u0000\u001dr\u0001\u0000\u0000\u0000\u001f"+
		"{\u0001\u0000\u0000\u0000!}\u0001\u0000\u0000\u0000#\u007f\u0001\u0000"+
		"\u0000\u0000%\u0081\u0001\u0000\u0000\u0000\'\u0083\u0001\u0000\u0000"+
		"\u0000)\u0085\u0001\u0000\u0000\u0000+\u0088\u0001\u0000\u0000\u0000-"+
		"\u008b\u0001\u0000\u0000\u0000/\u008d\u0001\u0000\u0000\u00001\u0090\u0001"+
		"\u0000\u0000\u00003\u0092\u0001\u0000\u0000\u00005\u0096\u0001\u0000\u0000"+
		"\u00007\u009c\u0001\u0000\u0000\u00009:\u0005(\u0000\u0000:\u0002\u0001"+
		"\u0000\u0000\u0000;<\u0005)\u0000\u0000<\u0004\u0001\u0000\u0000\u0000"+
		"=>\u0005v\u0000\u0000>?\u0005a\u0000\u0000?@\u0005r\u0000\u0000@\u0006"+
		"\u0001\u0000\u0000\u0000AB\u0005p\u0000\u0000BC\u0005r\u0000\u0000CD\u0005"+
		"i\u0000\u0000DE\u0005n\u0000\u0000EF\u0005t\u0000\u0000FG\u0005l\u0000"+
		"\u0000GH\u0005n\u0000\u0000H\b\u0001\u0000\u0000\u0000IJ\u0005r\u0000"+
		"\u0000JK\u0005e\u0000\u0000KL\u0005t\u0000\u0000LM\u0005u\u0000\u0000"+
		"MN\u0005r\u0000\u0000NO\u0005n\u0000\u0000O\n\u0001\u0000\u0000\u0000"+
		"PQ\u0005{\u0000\u0000Q\f\u0001\u0000\u0000\u0000RS\u0005}\u0000\u0000"+
		"S\u000e\u0001\u0000\u0000\u0000TU\u0005,\u0000\u0000U\u0010\u0001\u0000"+
		"\u0000\u0000VW\u0005f\u0000\u0000WX\u0005u\u0000\u0000XY\u0005n\u0000"+
		"\u0000Y\u0012\u0001\u0000\u0000\u0000Z[\u0005i\u0000\u0000[\\\u0005f\u0000"+
		"\u0000\\\u0014\u0001\u0000\u0000\u0000]^\u0005e\u0000\u0000^_\u0005l\u0000"+
		"\u0000_`\u0005s\u0000\u0000`a\u0005e\u0000\u0000a\u0016\u0001\u0000\u0000"+
		"\u0000bf\u0007\u0000\u0000\u0000ce\u0007\u0001\u0000\u0000dc\u0001\u0000"+
		"\u0000\u0000eh\u0001\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000fg\u0001"+
		"\u0000\u0000\u0000g\u0018\u0001\u0000\u0000\u0000hf\u0001\u0000\u0000"+
		"\u0000ik\u0007\u0002\u0000\u0000ji\u0001\u0000\u0000\u0000kl\u0001\u0000"+
		"\u0000\u0000lj\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000\u0000m\u001a"+
		"\u0001\u0000\u0000\u0000no\u0003\u0019\f\u0000op\u0005.\u0000\u0000pq"+
		"\u0003\u0019\f\u0000q\u001c\u0001\u0000\u0000\u0000rv\u0005\"\u0000\u0000"+
		"su\b\u0003\u0000\u0000ts\u0001\u0000\u0000\u0000ux\u0001\u0000\u0000\u0000"+
		"vt\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wy\u0001\u0000\u0000"+
		"\u0000xv\u0001\u0000\u0000\u0000yz\u0005\"\u0000\u0000z\u001e\u0001\u0000"+
		"\u0000\u0000{|\u0005*\u0000\u0000| \u0001\u0000\u0000\u0000}~\u0005/\u0000"+
		"\u0000~\"\u0001\u0000\u0000\u0000\u007f\u0080\u0005+\u0000\u0000\u0080"+
		"$\u0001\u0000\u0000\u0000\u0081\u0082\u0005-\u0000\u0000\u0082&\u0001"+
		"\u0000\u0000\u0000\u0083\u0084\u0005=\u0000\u0000\u0084(\u0001\u0000\u0000"+
		"\u0000\u0085\u0086\u0005=\u0000\u0000\u0086\u0087\u0005=\u0000\u0000\u0087"+
		"*\u0001\u0000\u0000\u0000\u0088\u0089\u0005!\u0000\u0000\u0089\u008a\u0005"+
		"=\u0000\u0000\u008a,\u0001\u0000\u0000\u0000\u008b\u008c\u0005<\u0000"+
		"\u0000\u008c.\u0001\u0000\u0000\u0000\u008d\u008e\u0005<\u0000\u0000\u008e"+
		"\u008f\u0005=\u0000\u0000\u008f0\u0001\u0000\u0000\u0000\u0090\u0091\u0005"+
		">\u0000\u0000\u00912\u0001\u0000\u0000\u0000\u0092\u0093\u0005>\u0000"+
		"\u0000\u0093\u0094\u0005=\u0000\u0000\u00944\u0001\u0000\u0000\u0000\u0095"+
		"\u0097\u0007\u0004\u0000\u0000\u0096\u0095\u0001\u0000\u0000\u0000\u0097"+
		"\u0098\u0001\u0000\u0000\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0098"+
		"\u0099\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a"+
		"\u009b\u0006\u001a\u0000\u0000\u009b6\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0005/\u0000\u0000\u009d\u009e\u0005/\u0000\u0000\u009e\u00a2\u0001\u0000"+
		"\u0000\u0000\u009f\u00a1\b\u0005\u0000\u0000\u00a0\u009f\u0001\u0000\u0000"+
		"\u0000\u00a1\u00a4\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a5\u0001\u0000\u0000"+
		"\u0000\u00a4\u00a2\u0001\u0000\u0000\u0000\u00a5\u00a6\u0006\u001b\u0000"+
		"\u0000\u00a68\u0001\u0000\u0000\u0000\u0006\u0000flv\u0098\u00a2\u0001"+
		"\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}