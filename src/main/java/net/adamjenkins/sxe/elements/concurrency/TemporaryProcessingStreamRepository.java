/*
 * Copyright 2020 Adam Norman Jenkins.
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
 * under the License.
 */
package net.adamjenkins.sxe.elements.concurrency;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A stream repository.  Implementations of this interface must make sure that the information
 * that a processor writes to a stream is that same information that is returned to the processor when
 * it requests the opposite stream.
 * 
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public interface TemporaryProcessingStreamRepository {

    /**
     * Borrows an output stream from the repository for reading.
     *
     * @param id    The id of the parallel processor.
     * @return  The stream.
     */
    public InputStream borrowInputStream(long id);

    /**
     * Returns the input stream back to the repository once the processor has finished reading from it.
     *
     * @param id    The id of the processor.
     * @param in    The stream being returned.
     */
    public void returnInputStream(long id, InputStream in);

    /**
     * Gets the output stream that a processor will write to.
     *
     * @param id    The id of the processor.
     * @return  The stream.
     */
    public OutputStream borrowOuputStream(long id);

    /**
     * Returns an output stream once a processor has finished with it.
     *
     * @param id    The id of the processor
     * @param out   The output stream being returned.
     */
    public void returnOuputStream(long id, OutputStream out);

}
