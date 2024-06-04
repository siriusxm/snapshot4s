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

final class Path private[snapshot4s] (val osPath: os.Path) extends PathApi {

  private[snapshot4s] def read(): String = os.read(osPath)

  private[snapshot4s] def write(contents: String): Unit =
    os.write.over(osPath, contents, createFolders = true)

  private[snapshot4s] def relativeTo(baseDirectory: Path): RelPath =
    new RelPath(osPath.relativeTo(baseDirectory.osPath))

  private[snapshot4s] def /(path: RelPath): Path =
    new Path(osPath / path.osPath)

  def exists(): Boolean = os.exists(osPath)
}

object Path extends PathCompanionApi {
  def apply(path: String): Path =
    new Path(os.Path(path))
}
