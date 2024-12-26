package com.eoral.lockservice.service.impl;

import com.eoral.lockservice.TestConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultCommonUtilsTest {

    @Test
    void convertToLockFileNameShouldReturnOnlyAlphaNumericChars() {
        DefaultCommonUtils defaultCommonUtils = new DefaultCommonUtils(
                TestConstants.MAX_DURATION_BETWEEN_PRE_ACQUIRE_LOCK_AND_ACQUIRE_LOCK);
        String lockName = "é!'^+%&/()=?_#${[]}\\|*-@.:,´\";~¨ ";
        String lockFileName = defaultCommonUtils.convertToLockFileName(lockName);
        String lockFileNameWithoutExtension = StringUtils.stripEnd(lockFileName, ".json");
        Assertions.assertTrue(StringUtils.isAlphanumeric(lockFileNameWithoutExtension));
    }
}
