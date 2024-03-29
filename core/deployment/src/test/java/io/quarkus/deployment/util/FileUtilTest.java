package io.quarkus.deployment.util;

import static io.quarkus.deployment.util.FileUtil.translateToVolumePath;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FileUtilTest {

    @Test
    public void testTranslateToVolumePath() {
        // Windows-Style paths are formatted.
        assertEquals("/c/tmp/code-with-quarkus", translateToVolumePath("C:\\tmp\\code-with-quarkus", true));
        assertEquals("//c/", translateToVolumePath("C", false));
        assertEquals("//c/", translateToVolumePath("C:", false));
        assertEquals("//c/", translateToVolumePath("C:\\", false));
        assertEquals("//c/Users", translateToVolumePath("C:\\Users", false));
        assertEquals("//c/Users/Quarkus/lambdatest-1.0-SNAPSHOT-native-image-source-jar",
                translateToVolumePath("C:\\Users\\Quarkus\\lambdatest-1.0-SNAPSHOT-native-image-source-jar", false));

        // Side effect for Unix-style path.
        assertEquals("//c/Users/Quarkus", translateToVolumePath("c:/Users/Quarkus", false));

        // Side effects for fancy inputs - for the sake of documentation.
        assertEquals("something/bizarre", translateToVolumePath("something\\bizarre", false));
        assertEquals("something.bizarre", translateToVolumePath("something.bizarre", false));
    }

}
