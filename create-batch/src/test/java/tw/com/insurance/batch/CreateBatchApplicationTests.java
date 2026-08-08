package tw.com.insurance.batch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CreateBatchApplicationTests {
    @Test void 可建立應用程式類別() { assertDoesNotThrow(CreateBatchApplication::new); }
}
