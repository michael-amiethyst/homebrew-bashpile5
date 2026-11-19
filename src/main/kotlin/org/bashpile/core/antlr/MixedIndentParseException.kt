package org.bashpile.core.antlr

import org.antlr.v4.runtime.misc.ParseCancellationException

class MixedIndentParseException(messageIn: String) : ParseCancellationException(messageIn)
