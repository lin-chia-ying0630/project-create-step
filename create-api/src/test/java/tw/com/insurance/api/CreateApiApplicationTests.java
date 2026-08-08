package tw.com.insurance.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CreateApiApplicationTests {
    @Test void 可建立應用程式類別() { assertDoesNotThrow(CreateApiApplication::new); }
}
