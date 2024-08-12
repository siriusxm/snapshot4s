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

private final class SnapshotConfigUnsupportedError(config: SnapshotConfig) extends Exception({
      s"""Your project setup is not supported by snapshot4s. We encourage you to raise an issue at https://github.com/siriusxm/snapshot4s/issues/new?template=bug.md

We have detected the following configuration:
  - sourceDirectory: ${config.sourceDirectory.value}
  - resourceDirectory: ${config.resourceDirectory.value}
  - outputDirectory: ${config.outputDirectory.value}
"""
    })
