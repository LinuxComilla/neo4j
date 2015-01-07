/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_2.parser

import org.parboiled.scala._
import org.parboiled.Context

trait Strings extends Base {

  protected def StringCharacters(c: Char): Rule1[String] = {
    push(new java.lang.StringBuilder) ~ zeroOrMore(EscapedChar | NormalChar(c)) ~~> (_.toString())
  }

  protected def NormalChar(c: Char) = {
    !(ch('\\') | ch(c)) ~ ANY ~:% withContext(appendToStringBuilder(_)(_))
  }

  protected def EscapedChar = {
    "\\" ~ (
        ch('\\') ~:% withContext(appendToStringBuilder(_)(_))
      | ch('\'') ~:% withContext(appendToStringBuilder(_)(_))
      | ch('"') ~:% withContext(appendToStringBuilder(_)(_))
      | ch('b') ~ appendToStringBuilder('\b')
      | ch('f') ~ appendToStringBuilder('\f')
      | ch('n') ~ appendToStringBuilder('\n')
      | ch('r') ~ appendToStringBuilder('\r')
      | ch('t') ~ appendToStringBuilder('\t')
      | Unicode ~~% withContext((code, ctx) => appendCodePointToStringBuilder(code)(ctx))
    )
  }

  protected def Unicode = rule { ch('u') ~ group(HexDigit ~ HexDigit ~ HexDigit ~ HexDigit) ~> (java.lang.Integer.parseInt(_, 16)) }
  private def HexDigit = rule { "0" - "9" | "a" - "f" | "A" - "F" }

  protected def appendToStringBuilder(c: Any): Context[Any] => Unit = ctx =>
    ctx.getValueStack.peek.asInstanceOf[java.lang.StringBuilder].append(c)
    ()

  protected def appendCodePointToStringBuilder(codePoint: java.lang.Integer): Context[Any] => Unit = ctx =>
    ctx.getValueStack.peek.asInstanceOf[java.lang.StringBuilder].appendCodePoint(codePoint)
    ()
}
