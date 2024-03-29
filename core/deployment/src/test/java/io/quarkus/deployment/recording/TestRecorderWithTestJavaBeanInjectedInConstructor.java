package io.quarkus.deployment.recording;

import javax.inject.Inject;

public class TestRecorderWithTestJavaBeanInjectedInConstructor {

    private final TestJavaBean constant;

    @Inject
    public TestRecorderWithTestJavaBeanInjectedInConstructor(TestJavaBean constant) {
        this.constant = constant;
    }

    public void retrieveConstant() {
        TestRecorder.RESULT.add(constant);
    }

}
