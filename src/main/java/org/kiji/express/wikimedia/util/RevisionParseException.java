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

/**
 * An exception thrown when there is an error parsing any portion of the revision delta string.
 */
public class RevisionParseException extends RuntimeException {
  /**
   * Constructs a parse exception.
   *
   * @param message The error message.
   */
  public RevisionParseException(String message) {
    super(message);
  }

  /**
   * Constructs a parse exception.
   *
   * @param message The error message.
   * @param cause The cause of the exception.
   */
  public RevisionParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
