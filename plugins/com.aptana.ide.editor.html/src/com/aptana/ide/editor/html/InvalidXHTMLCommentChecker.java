package com.aptana.ide.editor.html;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.ide.core.builder.BuildContext;
import com.aptana.ide.core.builder.IProblem;
import com.aptana.ide.core.builder.Warning;
import com.aptana.ide.editor.html.lexing.HTMLTokenTypes;
import com.aptana.ide.editor.html.parsing.HTMLDocumentType;
import com.aptana.ide.editor.html.parsing.HTMLParseState;
import com.aptana.ide.lexer.Lexeme;
import com.aptana.ide.lexer.LexemeList;
import com.aptana.ide.parsing.IParseState;

public class InvalidXHTMLCommentChecker extends HTMLBuildParticipant
{

	@Override
	public void buildStarting(List<BuildContext> contexts, boolean isBatch, IProgressMonitor monitor)
	{
		for (BuildContext context : contexts)
		{
			if (!isHTMLFile(context))
				continue;
			// ok we have an html file
			IParseState parseState = context.getParseState();
			if (!(parseState instanceof HTMLParseState))
				continue;
			// Make sure it's XHTML
			HTMLParseState htmlParseState = (HTMLParseState) parseState;
			if (htmlParseState.getDocumentType() < HTMLDocumentType.XHTML_1_0_STRICT)
				continue;
			// we have an XHTML doc and we need to do the check
			List<IProblem> problems = new ArrayList<IProblem>();
			LexemeList ll = context.getLexemeList();
			for (Lexeme lexeme : ll.toArray())
			{
				if (lexeme.typeIndex == HTMLTokenTypes.COMMENT)
				{
					String text = lexeme.getText();
					text = text.substring(4); // drop '<!--'
					text = text.substring(0, text.length() - 3); // drop '-->'
					if (text.contains("--"))
						problems
								.add(new Warning(1, context.getFile().getFullPath().toPortableString(), 0, lexeme
										.getStartingOffset(), lexeme.getEndingOffset(),
										"Comments should not contain '--' in the text. Many browsers mishandle comments not ending in this exact pattern."));
				}
			}
			context.recordNewProblems(problems);
		}
	}

}
