package com.erjean.carbatterywarning;

import com.erjean.carbatterywarning.utils.MessageListSplitter;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageListSplitterTest {

    private static final int SIZE_LIMIT = 4 * 1024 * 1024; // 4MB

    @Test
    void hasNext_emptyList() {
        List<Message> messages = Collections.emptyList();
        MessageListSplitter splitter = new MessageListSplitter(messages);
        assertFalse(splitter.hasNext());
    }

    @Test
    void hasNext_nonEmptyList() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", "body".getBytes()));
        MessageListSplitter splitter = new MessageListSplitter(messages);
        assertTrue(splitter.hasNext());
        splitter.next(); // Consume the only message
        assertFalse(splitter.hasNext());
    }

    @Test
    void next_singleMessageWithinLimit() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", new byte[1024])); // 1KB body
        MessageListSplitter splitter = new MessageListSplitter(messages);

        assertTrue(splitter.hasNext());
        List<Message> subList = splitter.next();
        assertNotNull(subList);
        assertEquals(1, subList.size());
        assertEquals("topic", subList.get(0).getTopic());
        assertEquals(1024, subList.get(0).getBody().length);
        assertFalse(splitter.hasNext());
    }

    @Test
    void next_multipleMessagesWithinLimit() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic1", new byte[1024])); // 1KB
        messages.add(new Message("topic2", new byte[2048])); // 2KB
        MessageListSplitter splitter = new MessageListSplitter(messages);

        assertTrue(splitter.hasNext());
        List<Message> subList = splitter.next();
        assertNotNull(subList);
        assertEquals(2, subList.size());
        assertEquals("topic1", subList.get(0).getTopic());
        assertEquals("topic2", subList.get(1).getTopic());
        assertFalse(splitter.hasNext());
    }

    @Test
    void next_multipleMessagesExceedingLimit() {
        List<Message> messages = new ArrayList<>();
        // Add messages that sum up to slightly more than SIZE_LIMIT
        int messageSize = SIZE_LIMIT / 3; // Each message is less than SIZE_LIMIT
        messages.add(new Message("topic", new byte[messageSize]));
        messages.add(new Message("topic", new byte[messageSize]));
        messages.add(new Message("topic", new byte[messageSize])); // This one should be in the next batch
        messages.add(new Message("topic", new byte[messageSize]));

        MessageListSplitter splitter = new MessageListSplitter(messages);

        // First batch
        assertTrue(splitter.hasNext());
        List<Message> subList1 = splitter.next();
        assertNotNull(subList1);
        assertEquals(2, subList1.size()); // Should contain the first two messages

        // Second batch
        assertTrue(splitter.hasNext());
        List<Message> subList2 = splitter.next();
        assertNotNull(subList2);
        assertEquals(2, subList2.size()); // Should contain the remaining two messages

        assertFalse(splitter.hasNext());
    }

    @Test
    void next_singleMessageExceedingLimit() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", new byte[SIZE_LIMIT + 1])); // Exceeds limit
        messages.add(new Message("topic", new byte[1024])); // Another message

        MessageListSplitter splitter = new MessageListSplitter(messages);

        // First batch should contain only the large message
        assertTrue(splitter.hasNext());
        List<Message> subList1 = splitter.next();
        assertNotNull(subList1);
        assertEquals(1, subList1.size());
        assertEquals(SIZE_LIMIT + 1, subList1.get(0).getBody().length);

        // Second batch should contain the remaining message
        assertTrue(splitter.hasNext());
        List<Message> subList2 = splitter.next();
        assertNotNull(subList2);
        assertEquals(1, subList2.size());
        assertEquals(1024, subList2.get(0).getBody().length);

        assertFalse(splitter.hasNext());
    }

    @Test
    void next_messageWithSizeLimit() {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("topic", new byte[SIZE_LIMIT - 20 - "topic".length()])); // Exactly SIZE_LIMIT with overhead
        messages.add(new Message("topic", new byte[1024])); // Another message

        MessageListSplitter splitter = new MessageListSplitter(messages);

        // First batch should contain only the first message
        assertTrue(splitter.hasNext());
        List<Message> subList1 = splitter.next();
        assertNotNull(subList1);
        assertEquals(1, subList1.size());

        // Second batch should contain the remaining message
        assertTrue(splitter.hasNext());
        List<Message> subList2 = splitter.next();
        assertNotNull(subList2);
        assertEquals(1, subList2.size());

        assertFalse(splitter.hasNext());
    }

    @Test
    void next_messagesWithProperties() {
        List<Message> messages = new ArrayList<>();
        Message msg1 = new Message("topic", "body1".getBytes());
        msg1.putUserProperty("key1", "value1"); // Adds to size
        messages.add(msg1);

        Message msg2 = new Message("topic", "body2".getBytes());
        msg2.putUserProperty("key2", "value2"); // Adds to size
        messages.add(msg2);

        MessageListSplitter splitter = new MessageListSplitter(messages);

        assertTrue(splitter.hasNext());
        List<Message> subList = splitter.next();
        assertNotNull(subList);
        assertEquals(2, subList.size()); // Assuming total size is within limit

        assertFalse(splitter.hasNext());
    }
}
