package com.techhaven.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UndoServiceTest {

    private UndoService undoService;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        undoService = UndoService.getInstance();
        // Сбросить состояние: вызовем undo чтобы очистить pending
        undoService.undo();
    }

    @Test
    void singletonAlwaysReturnsSameInstance() {
        UndoService a = UndoService.getInstance();
        UndoService b = UndoService.getInstance();
        assertSame(a, b);
    }

    @Test
    void noPendingDeletionByDefault() {
        assertFalse(undoService.isPendingDeletion(String.class, 1));
    }

    @Test
    void isPendingDeletionReturnsFalseForWrongType() {
        assertFalse(undoService.isPendingDeletion(Integer.class, 999));
    }

    @Test
    void isPendingDeletionReturnsFalseForWrongId() {
        assertFalse(undoService.isPendingDeletion(String.class, -1));
    }

    @Test
    void forceExecuteWithNoPendingDoesNotThrow() {
        assertDoesNotThrow(() -> undoService.forceExecute());
    }

    @Test
    void undoWithNoPendingDoesNotThrow() {
        assertDoesNotThrow(() -> undoService.undo());
    }

    @Test
    void undoTwiceDoesNotThrow() {
        undoService.undo();
        assertDoesNotThrow(() -> undoService.undo());
    }

    @Test
    void forceExecuteTwiceDoesNotThrow() {
        undoService.forceExecute();
        assertDoesNotThrow(() -> undoService.forceExecute());
    }
}
