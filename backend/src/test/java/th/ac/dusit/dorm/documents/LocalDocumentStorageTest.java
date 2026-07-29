package th.ac.dusit.dorm.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import th.ac.dusit.dorm.platform.DormProperties;

class LocalDocumentStorageTest {

    @TempDir
    Path directory;

    @Test
    void canBeCreatedAsASpringBean() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(DormProperties.class,
                    () -> new DormProperties(directory, 365, 200));
            context.register(LocalDocumentStorage.class);
            context.refresh();

            assertThat(context.getBean(LocalDocumentStorage.class)).isNotNull();
        }
    }

    @Test
    void storesDocumentWithSha256AndSafeGeneratedName() throws Exception {
        var storage = new LocalDocumentStorage(directory);

        var stored = storage.store("contracts", "สัญญา.pdf", "hello".getBytes(StandardCharsets.UTF_8));

        assertThat(stored.sha256()).isEqualTo(
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        assertThat(stored.path()).startsWith("contracts/");
        assertThat(Files.readString(directory.resolve(stored.path()))).isEqualTo("hello");
    }

    @Test
    void rejectsUnsupportedStorageCategory() {
        var storage = new LocalDocumentStorage(directory);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                storage.store("../outside", "file.pdf", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid document category");
    }
}
