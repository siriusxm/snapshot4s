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

import java.io.IOException
import java.nio.file.DirectoryNotEmptyException

private[snapshot4s] trait PathWrite { path: Path =>

  private[snapshot4s] def write(contents: String): Unit = {
    PathWrite.write(path, contents)
  }
}

private object PathWrite {

  // This synchronized block is necessary for concurrent `write` calls with paths that share the same parent directory.
  // If the parent doesn't exist, one call creates it and the others raise an exception.
  def write(path: Path, contents: String): Unit = this.synchronized {
    os.write.over(path.osPath, contents, createFolders = true)
  }
}
