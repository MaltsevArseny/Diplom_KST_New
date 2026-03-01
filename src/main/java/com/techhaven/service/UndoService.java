package com.techhaven.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Сервис отложенного удаления с возможностью отмены (Undo).
 *
 * <p>Реализует паттерн «мягкое удаление»: действие откладывается
 * на {@link com.techhaven.config.AppConfig#UNDO_TIMEOUT_SECONDS} секунд,
 * в течение которых пользователь может отменить операцию.</p>
 *
 * <p>Singleton — используется из view-слоя для корзины, избранного и каталога.</p>
 */
public class UndoService {
    private static UndoService instance;
    private Timeline timer;
    private Runnable pendingAction;
    private Runnable onCompleteCallback;
    private Runnable onUndoCallback;
    
    private Class<?> pendingEntityType;
    private int pendingEntityId;

    private UndoService() {}

    /** Получить единственный экземпляр сервиса. */
    public static synchronized UndoService getInstance() {
        if (instance == null) {
            instance = new UndoService();
        }
        return instance;
    }

    /**
     * Запросить отложенное выполнение действия.
     *
     * @param entityType тип сущности (для идентификации pending)
     * @param entityId   ID сущности
     * @param action     действие, выполняемое по истечении таймаута
     * @param onComplete callback после фактического выполнения
     * @param onUndo     callback при отмене пользователем
     */
    public void requestAction(Class<?> entityType, int entityId, Runnable action, Runnable onComplete, Runnable onUndo) {
        forceExecute();

        this.pendingEntityType = entityType;
        this.pendingEntityId = entityId;
        this.pendingAction = action;
        this.onCompleteCallback = onComplete;
        this.onUndoCallback = onUndo;

        timer = new Timeline(new KeyFrame(Duration.seconds(com.techhaven.config.AppConfig.UNDO_TIMEOUT_SECONDS), e -> forceExecute()));
        timer.setCycleCount(1);
        timer.play();
    }

    /** Немедленно выполнить отложенное действие (если есть). */
    public void forceExecute() {
        if (timer != null) {
            timer.stop();
        }
        if (pendingAction != null) {
            pendingAction.run();
            pendingAction = null;
            pendingEntityType = null;
            pendingEntityId = -1;
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
                onCompleteCallback = null;
            }
        }
        onUndoCallback = null;
    }

    /** Отменить отложенное действие и вызвать onUndo callback. */
    public void undo() {
        if (timer != null) {
            timer.stop();
        }
        pendingAction = null;
        pendingEntityType = null;
        pendingEntityId = -1;
        onCompleteCallback = null;
        if (onUndoCallback != null) {
            Runnable cb = onUndoCallback;
            onUndoCallback = null;
            cb.run();
        }
    }

    /** Проверить, ожидает ли удаление данная сущность. */
    public boolean isPendingDeletion(Class<?> entityType, int entityId) {
        return this.pendingAction != null && 
               this.pendingEntityType == entityType && 
               this.pendingEntityId == entityId;
    }
}
