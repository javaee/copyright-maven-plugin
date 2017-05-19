/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * Support for files with asciidoc file syntax.
 * Comments of the form:
 * /////////////////////////////////////////////////////////////////////////////
 * comment
 * /////////////////////////////////////////////////////////////////////////////
 *
 * @author	Bill Shannon
 */

package org.glassfish.copyright;

import java.io.*;
import java.util.regex.*;

public class AsciidocCopyright extends CommonCopyright {
    private String firstComment;
    private static final String commentRegex = "////+";
    private static final Pattern pat = Pattern.compile(commentRegex);
    private static final String commentDelim =
	"///////////////////////////////////////" +
	"////////////////////////////////////////";	// 79 slashes

    public AsciidocCopyright(Copyright c) {
	super(c);
	commentStart = commentDelim;
	commentEnd = commentDelim;
	commentPrefix = "    ";
    }

    /**
     * Is this an asciidoc file?
     */
    protected boolean supports(File file) {
	return file.getName().endsWith(".adoc");
    }

    /**
     * Is this the start of a comment?
     */
    protected boolean isCommentStart(String line) {
	if (line != null && line.matches(commentRegex)) {
	    firstComment = line;
	    return true;
	} else
	    return false;
    }

    /**
     * Is this the end of a comment?
     */
    protected boolean isCommentEnd(String line) {
	return line.equals(firstComment);
    }

    /**
     * Return text after end of comment.
     */
    protected String commentTrailer(String line) {
	Matcher m = pat.matcher(line);
	if (m.matches()) {
	    int end = m.end();
	    return line.substring(end).trim();
	}
	return "";
    }
}
