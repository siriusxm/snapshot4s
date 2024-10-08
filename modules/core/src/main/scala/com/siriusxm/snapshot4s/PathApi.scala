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

private[snapshot4s] trait PathApi {

  private[snapshot4s] def read(): String
  private[snapshot4s] def write(contents: String): Unit
  private[snapshot4s] def relativeTo(baseDirectory: Path): Option[RelPath]
  private[snapshot4s] def /(path: RelPath): Path

  private[snapshot4s] def exists(): Boolean

  private[snapshot4s] def value: String
}

trait PathCompanionApi {
  def apply(path: String): Path
}

private[snapshot4s] trait RelPathApi {
  private[snapshot4s] def value: String
}

private[snapshot4s] trait RelPathCompanionApi {
  def apply(path: String): RelPath
}
