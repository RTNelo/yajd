package me.rotatingticket.yajd;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public class MockitoTestCase {
    @BeforeEach
    void processMockAnnotations() {
        MockitoAnnotations.initMocks(this);
    }

}
