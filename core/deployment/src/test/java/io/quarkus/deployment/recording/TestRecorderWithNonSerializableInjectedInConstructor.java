package io.quarkus.deployment.recording;

import javax.inject.Inject;

public class TestRecorderWithNonSerializableInjectedInConstructor {

    private final NonSerializable constant;

    @Inject
    public TestRecorderWithNonSerializableInjectedInConstructor(NonSerializable constant) {
        this.constant = constant;
    }

    public void retrieveConstant() {
        TestRecorder.RESULT.add(constant);
    }

}
