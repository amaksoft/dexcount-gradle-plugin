/*
 * Copyright (C) 2016 KeepSafe Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.getkeepsafe.dexcount

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class IOUtil {
    public static def drainToFile(InputStream stream, File file) {
        stream.withStream { input ->
            file.withOutputStream { output ->
                def buf = new byte[4096]
                def read
                while ((read = input.read(buf)) != -1) {
                    output.write(buf, 0, read)
                }
                output.flush()
            }
        }
    }
    public static void printToFile(
            File file,
            @ClosureParams(value = SimpleType, options = ['java.io.PrintStream']) Closure closure) {
        if (file != null) {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.withOutputStream { stream ->
                def out = new PrintStream(stream)
                closure(out)
                out.flush()
                out.close()
            }
        }
    }

}
