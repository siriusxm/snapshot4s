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
    def retry(count: Int): Unit = try {
      os.write.over(path.osPath, contents, createFolders = true)
    } catch {
      // This retry is necessary for concurrent `write` calls with paths that share the same parent directory.
      // If the parent doesn't exist, one call creates it and the others raise an exception.
      // Retrying the call succeeds, as the directory exists.
      //
      // Multiple retries are needed if one parent is nested within another.
      //
      // === Example ===
      // The three calls A, B, and C are made concurrently:
      //  - Call A: snapshot/inline-patch/123-456
      //  - Call B: snapshot/resource-patch/existing-file.txt
      //  - Call C: snapshot/resource-patch/nested-dir/nested-file.txt
      //
      //  1. A succeeds and creates `snapshot/inline-patch`. B and C fail.
      //  2. B and C are retried. B creates `snapshot/resource-patch`. C still fails.
      //  3. C is retried. C creates `snapshot/resource-patch/nested-dir`.
      case _: DirectoryNotEmptyException | _: IOException if count < 10 =>
        retry(count + 1)
    }
    retry(0)
  }

}
