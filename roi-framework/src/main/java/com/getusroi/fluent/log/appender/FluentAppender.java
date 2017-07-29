package com.getusroi.fluent.log.appender;

import java.util.HashMap;
import java.util.Map;

import org.fluentd.logger.FluentLogger;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class FluentAppender<E> extends UnsynchronizedAppenderBase<E> {

    private static final int MSG_SIZE_LIMIT = 65535;

    private DaemonAppender<E> appender;
    private int maxQueueSize;

    private static final class FluentDaemonAppender<E> extends DaemonAppender<E> {

        private FluentLogger fluentLogger;
        private final String tag;
        private final String label;
        private final String remoteHost;
        private final int port;
        private final Layout<E> layout;

        FluentDaemonAppender(String tag, String label, String remoteHost,
                             int port, Layout<E> layout, int maxQueueSize) {
            super(maxQueueSize);
            this.tag = tag;
            this.label = label;
            this.remoteHost = remoteHost;
            this.port = port;
            this.layout = layout;
        }

        @Override
        public void execute() {
            this.fluentLogger = FluentLogger.getLogger(tag, remoteHost, port);
            super.execute();
        }

        @Override
        protected void close() {
            try {
                super.close();
            } finally {
                fluentLogger.close();
            }
        }

        @Override
        protected void append(E rawData) {
            String msg;
            if (layout != null) {
                msg = layout.doLayout(rawData);
            } else {
                msg = rawData.toString();
            }
            if (msg != null && msg.length() > MSG_SIZE_LIMIT) {
                msg = msg.substring(0, MSG_SIZE_LIMIT);
            }
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("msg", msg);
            fluentLogger.log(label, data);
        }
    }


    @Override
    public void start() {
        super.start();
        appender = new FluentDaemonAppender<E>(tag, label, remoteHost, port, layout, maxQueueSize);
    }

    @Override
    protected void append(E eventObject) {
        appender.log(eventObject);
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            appender.close();
        }
    }

    private String tag;
    private String label;
    private String remoteHost;
    private int port;
    private Layout<E> layout;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }
}