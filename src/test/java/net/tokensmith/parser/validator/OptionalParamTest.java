package net.tokensmith.parser.validator;

import net.tokensmith.parser.validator.exception.EmptyValueError;
import net.tokensmith.parser.validator.exception.MoreThanOneItemError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionalParamTest {

    private OptionalParam subject;

    @BeforeEach
    void setUp() {
        subject = new OptionalParam();
    }

    private List<String> makeItems() {
        List<String> items = new ArrayList<>();
        items.add("item1");
        return items;
    }

    @Test
    public void runWhenOneItemShouldBeOK() throws Exception {
        List<String> items = makeItems();

        boolean actual = subject.run(items);
        assertTrue(actual);
    }

    @Test
    public void runWhenZeroItemsShouldBeOK() throws Exception {
        List<String> items = new ArrayList<>();

        boolean actual = subject.run(items);
        assertTrue(actual);
    }

    @Test
    public void runWhenTooManyItemsShouldThrowMoreThanOneItemError() throws Exception {
        List<String> items = makeItems();
        items.add("item2");

        Assertions.assertThrows(MoreThanOneItemError.class, () -> {
            subject.run(items);
        });
    }

    @Test
    public void runWhenOneItemIsEmptyShouldThrowEmptyValueError() throws Exception {
        List<String> items = new ArrayList<>();
        items.add("");

        Assertions.assertThrows(EmptyValueError.class, () -> {
            subject.run(items);
        });
    }

}