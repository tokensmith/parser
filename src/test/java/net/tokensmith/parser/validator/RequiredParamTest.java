package net.tokensmith.parser.validator;

import net.tokensmith.parser.validator.exception.EmptyValueError;
import net.tokensmith.parser.validator.exception.MoreThanOneItemError;
import net.tokensmith.parser.validator.exception.NoItemsError;
import net.tokensmith.parser.validator.exception.ParamIsNullError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequiredParamTest {

    private RequiredParam subject;

    @BeforeEach
    void setUp() {
        subject = new RequiredParam();
    }

    private List<String> makeItems() {
        List<String> items = new ArrayList<>();
        items.add("item1");
        return items;
    }

    @Test
    public void runWithOneItemShouldBeOk() throws Exception {
        List<String> items = makeItems();

        boolean actual = subject.run(items, false);
        assertTrue(actual);
    }

    @Test
    public void runWhenIsNullShouldThrowParamIsNullError() throws Exception {
        List<String> items = null;

        Assertions.assertThrows(ParamIsNullError.class, () -> {
            subject.run(items, false);
        });
    }

    @Test
    public void runWhenNoItemsShouldThrowNoItemsError() throws Exception {
        List<String> items = new ArrayList<>();

        Assertions.assertThrows(NoItemsError.class, () -> {
            subject.run(items, false);
        });
    }

    @Test
    public void runWhenOneItemAndEmptyShouldThrowEmptyValueError() throws Exception {
        List<String> items = new ArrayList<>();
        items.add("");

        Assertions.assertThrows(EmptyValueError.class, () -> {
            subject.run(items, false);
        });
    }

    @Test
    public void runWhenTwoItemsShouldThrowMoreThanOneItemError() throws Exception {
        List<String> items = makeItems();
        items.add("item2");

        Assertions.assertThrows(MoreThanOneItemError.class, () -> {
            subject.run(items, false);
        });
    }

}