package com.irelax.salesai.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberNormalizerTest {
    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void normalizesAustralianMobileNumbers() {
        assertThat(normalizer.normalize("0412 345 678")).isEqualTo("+61412345678");
        assertThat(normalizer.normalize("61412345678")).isEqualTo("+61412345678");
        assertThat(normalizer.normalize("+61 412 345 678")).isEqualTo("+61412345678");
    }

    @Test
    void rejectsBlankNumbers() {
        assertThatThrownBy(() -> normalizer.normalize(" ")).isInstanceOf(IllegalArgumentException.class);
    }
}
