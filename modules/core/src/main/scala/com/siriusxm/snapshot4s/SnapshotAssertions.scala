/*
 * Copyright 2024 SiriusXM
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

package snapshot4s

/** Encompasses all snapshot assertions.
  *
  * @tparam R Assertion result type specific to the test framework. For example, weaver's result type is `IO[Expectations]`.
  */
trait SnapshotAssertions[R] extends AssertInlineSnapshotMacro[R] with AssertFileSnapshotMacro[R]
