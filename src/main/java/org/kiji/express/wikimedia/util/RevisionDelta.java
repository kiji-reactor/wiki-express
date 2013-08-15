/**
 * (c) Copyright 2013 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiji.express.wikimedia.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * The delta string in a revision entry that contains the instructions for revising a
 * piece of text.
 *
 * <p>The delta string is a sequence of tab-separated operations. Each operation has
 * two parts: an operator and an operand. The operator is the first character of the
 * instruction, and the operand is the rest of the operation. There are three operators:</p>
 *
 * <table>
 *   <tr>
 *     <th>operation</th>
 *     <th>operator</th>
 *     <th>operand</th>
 *   </tr>
 *   <tr>
 *     <td>Equal characters (no-op)</td>
 *     <td>=</td>
 *     <td>The number of characters that remained the same.</td>
 *   </tr>
 *   <tr>
 *     <td>Insert characters</td>
 *     <td>+</td>
 *     <td>The text to be added, encoded using %xx (URL encoding).</td>
 *   </tr>
 *   <tr>
 *     <td>Delete characters</td>
 *     <td>-</td>
 *     <td>The number of characters to remove.</td>
 *   </tr>
 * </table>
 *
 * <p>Note: The terminology here was borrowed from
 * name.fraser.neil.plaintext.diff_match_patch.java, which is the class we use to generate
 * diffs, hosted at http://code.google.com/p/google-diff-match-patch</p>
 */
public class RevisionDelta implements Iterable<RevisionDelta.Operation> {
  private static final char INSTRUCTION_SEPERATOR = '\t';

  /** The full ordered list of operations in the delta. */
  private final List<Operation> mOperations;

  /**
   * Parses a delta string.
   *
   * @param delta The entire delta string.
   */
  public RevisionDelta(String delta) {
    if (null == delta) {
      throw new IllegalArgumentException("delta may not be null.");
    }
    final String[] operationStrings = StringUtils.splitPreserveAllTokens(delta,
        INSTRUCTION_SEPERATOR);

    mOperations = new ArrayList<Operation>(operationStrings.length);
    for (String operationString : operationStrings) {
      mOperations.add(new Operation(operationString));
    }
  }

  /** {@inheritDoc} */
  @Override
  public Iterator<Operation> iterator() {
    return mOperations.iterator();
  }

  /**
   * A single operation within the delta.
   */
  public static final class Operation {
    /** The full operation string. */
    private final String mOperationString;

    /** The decoded text to insert if this is an INSERT operation. Populated lazily. */
    private String mDecodedText;

    /**
     * Possible values for the operator in the operation.
     */
    public static enum Operator {
      EQUAL,
      INSERT,
      DELETE,
    }

    /**
     * Constructs an operation.
     *
     * @param operation The operation string (first character is operator, the rest is operand).
     * @throws RevisionParseException If there is a error parsing the operation string.
     */
    private Operation(String operation) {
      if (null == operation) {
        throw new IllegalArgumentException("Operation string was null");
      }
      if (operation.isEmpty()) {
        throw new RevisionParseException("Failed to parse empty operation string. "
            + "Expected at least one character to be the operator.");
      }
      mOperationString = operation;
      mDecodedText = null;  // Set lazily if this is an INSERT operation.
    }

    /**
     * Gets the operator of the operation (equal, insert, delete).
     *
     * @return The operator.
     * @throws RevisionParseException If there is a error parsing the operation string.
     */
    public Operator getOperator() {
      assert !mOperationString.isEmpty();

      final char operator = mOperationString.charAt(0);
      switch (operator) {
      case '=':
        return Operator.EQUAL;
      case '+':
        return Operator.INSERT;
      case '-':
        return Operator.DELETE;
      default:
        throw new RevisionParseException(
            String.format("Unrecognized operator '%c' in operation \"%s\"",
                operator, mOperationString));
      }
    }

    /**
     * Gets the operand of the operation.
     *
     * @return For an EQUAL or DELETE operation, the number of characters.
     *     For an INSERT operation, the URL-encoded string to insert (see the getText() method).
     *
     * @see #getText()
     * @see #getNumChars()
     */
    public String getOperand() {
      assert !mOperationString.isEmpty();
      return mOperationString.substring(1);
    }

    /**
     * Gets the characters to add if this is an INSERT operation.
     *
     * @return The text to add (already decoded).
     * @throws UnsupportedOperationException If this is not an INSERT operation.
     */
    public String getText() {
      if (null == mDecodedText) {
        mDecodedText = decodeInsertChars(getOperand());
      }
      return mDecodedText;
    }

    /**
     * Gets the number of characters affected by the operation.
     *
     * @return For an EQUAL or DELETE operation, the number of characters affected. For an
     *     INSERT operation, the number of characters to insert.
     * @throws RevisionParseException If there is an error parsing the operation string.
     */
    public int getNumChars() {
      final Operator operator = getOperator();
      final String operand = getOperand();
      switch (operator) {
      case EQUAL:
      case DELETE:
        try {
          return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
          throw new RevisionParseException(
              String.format(
                  "Unable to parse operand. Expected a number, but found \"%s\" instead.",
                  operand),
              e);
        }
      case INSERT:
        return getText().length();
      default:
        throw new RuntimeException("Unknown operator: " + operator);
      }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return mOperationString;
    }

    /**
     * Decodes the %xx encoded (sort of URL encoded) operand assuming this is an INSERT operation.
     *
     * <p>The RevisionDiffer uses a slightly modified form of URL encoding. In particular, it
     * switches any '+' characters to ' ' characters. This method handles that.</p>
     *
     * @param encoded The encoded text.
     * @return The decoded text.
     * @throws RevisionParseException If the text is not encoded properly.
     */
    private String decodeInsertChars(String encoded) {
      assert Operator.INSERT == getOperator();

      try {
        // The parameter for insert ops is encoded by URLEncoder.  Decoding would change
        // any "+" in the parameter to " ".  Let's encode the '+' character properly.
        return URLDecoder.decode(encoded.replace("+", "%2B"), "UTF-8");
      } catch (UnsupportedEncodingException e) {
        // This is unlikely to happen, since most modern systems support UTF-8 encoding.
        throw new RuntimeException("Current system does not support UTF-8 encoding.");
      } catch (IllegalArgumentException e) {
        // There is some illegal escape character in the parameter.
        throw new RevisionParseException(
            "Illegal escape sequence in INSERT delta operand: " + encoded, e);
      }
    }
  }
}
