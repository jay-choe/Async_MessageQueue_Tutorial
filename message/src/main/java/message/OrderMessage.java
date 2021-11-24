package message;

public class OrderMessage {
    private final int logId;
    private final int productId;
    private final int stock;
    private final long totalPrice;
    private String errorMessage;
    private int errorRetryCount = 0;

    public OrderMessage(int logId, final int productId, final int stock, final long totalPrice) {
        this.logId = logId;
        this.productId = productId;
        this.stock = stock;
        this.totalPrice = totalPrice;
    }

    public static OrderMessage.OrderMessageBuilder builder() {
        return new OrderMessage.OrderMessageBuilder();
    }

    public int getLogId() {
        return this.logId;
    }

    public int getProductId() {
        return this.productId;
    }

    public int getStock() {
        return this.stock;
    }

    public long getTotalPrice() {
        return this.totalPrice;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorRetryCount() {
        return this.errorRetryCount;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorRetryCount(final int errorRetryCount) {
        this.errorRetryCount = errorRetryCount;
    }

    public static class OrderMessageBuilder {
        private int logId;
        private int productId;
        private int stock;
        private long totalPrice;

        OrderMessageBuilder() {
        }

        public OrderMessage.OrderMessageBuilder logId(final int logId) {
            this.logId = logId;
            return this;
        }

        public OrderMessage.OrderMessageBuilder productId(final int productId) {
            this.productId = productId;
            return this;
        }

        public OrderMessage.OrderMessageBuilder stock(final int stock) {
            this.stock = stock;
            return this;
        }

        public OrderMessage.OrderMessageBuilder totalPrice(final long totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public OrderMessage build() {
            return new OrderMessage(this.logId, this.productId, this.stock, this.totalPrice);
        }
    }
}