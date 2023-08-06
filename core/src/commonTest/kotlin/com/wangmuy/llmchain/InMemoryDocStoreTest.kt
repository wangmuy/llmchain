package com.wangmuy.llmchain

import com.wangmuy.llmchain.docstore.InMemoryDocstore
import com.wangmuy.llmchain.schema.Document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryDocStoreTest {
    @Test fun testDocumentFound() {
        val dict = mapOf("foo" to Document("bar"))
        val docStore = InMemoryDocstore(dict)
        val output = docStore.search("foo")
        assertTrue(output is Document)
        assertEquals("bar", output?.pageContent)
    }

    @Test fun testDocumentNotFound() {
        val dict = mapOf("foo" to Document("bar"))
        val docStore = InMemoryDocstore(dict)
        val output = docStore.search("bar")
        assertTrue(output == null)
    }

    @Test fun testAddingDocument() {
        val dict = mapOf("foo" to Document("bar"))
        val docStore = InMemoryDocstore(dict)
        val newDict = mapOf("bar" to Document("foo"))
        docStore.add(newDict)

        val fooOutput = docStore.search("bar")
        assertTrue(fooOutput is Document)
        assertEquals("foo", fooOutput?.pageContent)

        val barOutput = docStore.search("foo")
        assertTrue(barOutput is Document)
        assertEquals("bar", barOutput?.pageContent)
    }

    @Test fun testAddingDocumentAlreadyExists() {
        val dict = mapOf("foo" to Document("bar"))
        val docStore = InMemoryDocstore(dict)
        val newDict = mapOf("foo" to Document("foo"))

        try {
            docStore.add(newDict)
        } catch (e: Exception) {
            assertTrue(e is IllegalArgumentException)
        }

        val barOutput = docStore.search("foo")
        assertTrue(barOutput is Document)
        assertEquals("bar", barOutput?.pageContent)
    }
}