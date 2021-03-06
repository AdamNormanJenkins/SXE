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
package net.adamjenkins.sxe.concurrency;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Test a locks state
 *
 * @author <a href="mailto:opensource@adamjenkins.net">Adam Norman Jenkins</a>
 */
public class LockStateAssertion {

    public void assertReadLocked(ReentrantReadWriteLock lock){
        assert lock.getReadLockCount() == 1;
    }

    public void assertWriteLocked(ReentrantReadWriteLock lock){
        assert lock.isWriteLocked();
    }

    public void assertReadUnlocked(ReentrantReadWriteLock lock){
        assert lock.getReadLockCount() == 0;
    }

    public void assertWriteUnlocked(ReentrantReadWriteLock lock){
        assert !lock.isWriteLocked();
    }

}
