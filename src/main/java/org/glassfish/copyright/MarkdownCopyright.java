/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/**
 * Support for files with markdown file syntax.
 * Comments of the form:
 * [//]: # ( comment )
 *
 * @author	Bill Shannon
 */

package org.glassfish.copyright;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MarkdownCopyright extends AbstractCopyright {
    protected String commentPrefix = "[//]: # ( ";
    protected String commentSuffix = " )";

    public MarkdownCopyright(Copyright c) {
	super(c);
    }

    /**
     * Is this a markdown file?
     */
    protected boolean supports(File file) {
	String fname = file.getName();
	if (fname.endsWith(".md") || fname.endsWith(".md.vm"))
	    return true;
	return false;
    }

    /**
     * Read the first comment block in the file.
     */
    protected String readComment(BufferedReader r) throws IOException {
	StringBuilder comment = new StringBuilder();
	String line;
	// skip blank lines at beginning of file
	while ((line = r.readLine()) != null) {
	    line = strip(line);
	    if (line.length() != 0)
		break;
	}
	if (line == null || !line.startsWith(commentPrefix))
	    return null;
	do {
	    if (line.length() == 0)
		break;		// end of comment
	    if (!line.startsWith(commentPrefix))
		break;		// end of comment
	    line = line.substring(commentPrefix.length());
	    if (line.endsWith(commentSuffix))
		line = line.substring(0,
					line.length() - commentSuffix.length());
	    comment.append(strip(line)).append('\n');
	} while ((line = r.readLine()) != null);
	int len = comment.length();
	if (len >= 2 && comment.charAt(len - 1) == '\n' &&
		comment.charAt(len - 2) == '\n')
	    comment.setLength(len - 1);
	return comment.toString();
    }

    /**
     * Skip the first comment block, replacing it with the correct copyright.
     */
    protected void replaceCopyright(BufferedReader in,
			BufferedWriter out, String comment, String lastChanged)
			throws IOException {
	String line;
	// skip blank lines at beginning of file
	while ((line = in.readLine()) != null) {
	    line = strip(line);
	    if (line.length() != 0)
		break;
	}

	if (comment != null && line != null && line.startsWith(commentPrefix)) {
	    boolean sawCopyright = false;
	    do {
		if (line.length() == 0)
		    break;		// end of comment
		if (!line.startsWith(commentPrefix))
		    break;		// end of comment
		if (!sawCopyright && line.indexOf("Copyright") >= 0) {
		    Matcher m = ypat.matcher(line);
		    if (m.find()) {
			lastChanged = addCopyrightDate(m.group(2), lastChanged);
			sawCopyright = true;
		    }
		}
	    } while ((line = in.readLine()) != null);
	}
	writeCopyright(out, lastChanged, comment);

	if (line != null) {
	    // the new copyright ends with a blank line so don't write another
	    if (line.length() > 0) {
		out.write(line);
		out.write('\n');		// line terminator
	    }
	    // have to copy the rest here so that blanks aren't skipped
	    copy(in, out, false);
	}
    }

    /**
     * Update the existing copyright statement, changing the copyright
     * year to include lastChanged.
     */
    protected void updateCopyright(BufferedReader in,
				BufferedWriter out, String lastChanged)
				throws IOException {
	String line;
	// skip blank lines at beginning of file
	while ((line = in.readLine()) != null) {
	    line = strip(line);
	    if (line.length() != 0)
		break;
	}
	if (line == null)
	    throw new IOException("NO CONTENT, repair failed");

	if (line.startsWith(commentPrefix)) {
	    boolean updated = false;
	    do {
		if (!line.startsWith(commentPrefix))
		    break;		// end of comment
		if (!updated && line.indexOf("Copyright") >= 0) {
		    Matcher m = ypat.matcher(line);
		    if (m.find()) {
			String y = addCopyrightDate(m.group(2), lastChanged);
			line = line.substring(0, m.start(2)) + y +
						line.substring(m.end(2));
			updated = true;
		    }
		}
		out.write(line);
		out.write('\n');
	    } while ((line = in.readLine()) != null);
	}
	if (line != null) {
	    if (line.length() != 0)
		out.write('\n');	// make sure there's a blank line
	    out.write(line);
	    out.write('\n');		// line terminator
	    // have to copy the rest here so that blanks aren't skipped
	    copy(in, out, false);
	}
    }

    /**
     * Convert the comment text to markdown syntax.
     */
    protected String toComment(String comment) {
	BufferedReader r = new BufferedReader(new StringReader(comment));
	StringBuilder out = new StringBuilder();
	try {
	    String line;
	    while ((line = r.readLine()) != null)
		out.append(commentPrefix).append(strip(line)).
		    append(commentSuffix).append('\n');
	    out.append("\n");
	} catch (IOException ioex) {
	    // can't happen
	} finally {
	    try {
		r.close();
	    } catch (IOException ex) { }
	}
	return out.toString();
    }
}
