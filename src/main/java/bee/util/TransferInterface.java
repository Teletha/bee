/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package bee.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transfer.TransferResource;

import bee.UserInterface;

public class TransferInterface implements TransferListener {

    /** The progress event interval. (ms) */
    private static final long interval = 500 * 1000 * 1000;

    /** The last progress event time. */
    private long last = 0;

    /** The actual console. */
    private UserInterface ui;

    /** The downloading items. */
    private Map<TransferResource, TransferEvent> downloading = new ConcurrentHashMap();

    /**
     * <p>
     * Injectable constructor.
     * </p>
     * 
     * @param ui A user interface to notify.
     */
    private TransferInterface(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferInitiated(TransferEvent event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferProgressed(TransferEvent event) {
        // register current downloading artifact
        downloading.put(event.getResource(), event);

        long now = System.nanoTime();

        if (interval < now - last) {
            last = now; // update last event time

            // build message
            StringBuilder message = new StringBuilder();

            for (Map.Entry<TransferResource, TransferEvent> entry : downloading.entrySet()) {
                TransferResource resource = entry.getKey();

                message.append(name(resource));
                message.append(" (");
                message.append(format(entry.getValue().getTransferredBytes(), resource.getContentLength()));
                message.append(")   ");
            }
            message.append('\r');

            // notify
            ui.info(message.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferSucceeded(TransferEvent event) {
        // unregister item
        downloading.remove(event.getResource());

        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

            if (event.getRequestType() == TransferEvent.RequestType.GET) {
                ui.info("Downloaded : " + name(resource) + " (" + len + ") from [" + resource.getRepositoryUrl() + "]");
            } else {
                ui.info("Uploaded : " + name(resource) + " (" + len + ") to [" + resource.getRepositoryUrl() + "]");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferFailed(TransferEvent event) {
        // unregister item
        downloading.remove(event.getResource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferCorrupted(TransferEvent event) {
        ui.error(event.getException());
    }

    /**
     * Compute readable resource name.
     * 
     * @param resource
     * @return
     */
    private static String name(TransferResource resource) {
        return resource.getResourceName().substring(resource.getResourceName().lastIndexOf('/') + 1);
    }

    /**
     * <p>
     * Format size.
     * </p>
     * 
     * @param current A current size.
     * @param size A total size.
     * @return
     */
    private static String format(long current, long size) {
        if (size >= 1024) {
            return toKB(current) + "/" + toKB(size) + " KB";
        } else if (size >= 0) {
            return current + "/" + size + " B";
        } else if (current >= 1024) {
            return toKB(current) + " KB";
        } else {
            return current + " B";
        }
    }

    /**
     * <p>
     * Format.
     * </p>
     * 
     * @param size
     * @return
     */
    private static long toKB(long size) {
        return (size + 1023) / 1024;
    }
}